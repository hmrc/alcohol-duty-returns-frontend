/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.payments

import base.SpecBase
import connectors.AlcoholDutyAccountConnector
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import play.api.test.Helpers._
import viewmodels.payments.ViewPastPaymentsViewModel
import views.html.payments.ViewPastPaymentsView

import java.time.LocalDate
import scala.concurrent.Future

class ViewPastPaymentsControllerSpec extends SpecBase {

  "ViewPastPaymentsController Controller" - {
    "must return OK and the correct view for a GET" in {
      val viewModelHelper                  = new ViewPastPaymentsViewModel(createDateTimeHelper())
      val mockAlcoholDutyAccountsConnector = mock[AlcoholDutyAccountConnector]
      when(mockAlcoholDutyAccountsConnector.outstandingPayments(any())(any())) thenReturn Future.successful(
        openPaymentsData
      )
      when(mockAlcoholDutyAccountsConnector.historicPayments(any(), any())(any())) thenReturn Future.successful(
        historicPayments
      )
      val application                      = applicationBuilder(userAnswers = None)
        .overrides(bind[AlcoholDutyAccountConnector].toInstance(mockAlcoholDutyAccountsConnector))
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.payments.routes.ViewPastPaymentsController.onPageLoad.url)
        val result  = route(application, request).value

        val view                          = application.injector.instanceOf[ViewPastPaymentsView]
        val sortedOutstandingPaymentsData =
          openPaymentsData.outstandingPayments.sortBy(_.dueDate)(Ordering[LocalDate].reverse)
        val outstandingPaymentsTable      =
          viewModelHelper.getOutstandingPaymentsTable(sortedOutstandingPaymentsData)(getMessages(application))
        val unallocatedPaymentsTable      =
          viewModelHelper.getUnallocatedPaymentsTable(openPaymentsData.unallocatedPayments)(getMessages(application))
        val historicPaymentsTable         =
          viewModelHelper.getHistoricPaymentsTable(historicPayments.payments)(getMessages(application))
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          outstandingPaymentsTable,
          unallocatedPaymentsTable,
          openPaymentsData.totalOpenPaymentsAmount,
          historicPaymentsTable,
          2024
        )(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET on Exception" in {
      val mockAlcoholDutyAccountsConnector = mock[AlcoholDutyAccountConnector]
      val application                      = applicationBuilder(userAnswers = None).build()
      running(application) {
        when(mockAlcoholDutyAccountsConnector.outstandingPayments(any())(any())) thenReturn Future.failed(
          new Exception("test Exception")
        )
        val request = FakeRequest(GET, controllers.payments.routes.ViewPastPaymentsController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
