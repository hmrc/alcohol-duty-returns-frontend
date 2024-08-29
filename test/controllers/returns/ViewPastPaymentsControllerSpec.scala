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

package controllers.returns

import base.SpecBase
import connectors.AlcoholDutyAccountConnector
import controllers.returns
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import play.api.test.Helpers._
import viewmodels.returns.ViewPastPaymentsViewModel
import views.html.returns.ViewPastPaymentsView

import scala.concurrent.Future

class ViewPastPaymentsControllerSpec extends SpecBase {

  "ViewPastPaymentsController Controller" - {
    "must return OK and the correct view for a GET" in {
      val viewModelHelper                  = new ViewPastPaymentsViewModel()
      val mockAlcoholDutyAccountsConnector = mock[AlcoholDutyAccountConnector]
      when(mockAlcoholDutyAccountsConnector.outstandingPayments(any())(any())) thenReturn Future.successful(
        openPaymentsData
      )
      val application                      = applicationBuilder(userAnswers = None)
        .overrides(bind[AlcoholDutyAccountConnector].toInstance(mockAlcoholDutyAccountsConnector))
        .build()
      running(application) {
        val request = FakeRequest(GET, returns.routes.ViewPastPaymentsController.onPageLoad.url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[ViewPastPaymentsView]

        val outstandingPaymentsTable =
          viewModelHelper.getOutstandingPaymentsTable(openPaymentsData.outstandingPayments)(getMessages(application))
        val unallocatedPaymentsTable =
          viewModelHelper.getUnallocatedPaymentsTable(openPaymentsData.unallocatedPayments)(getMessages(application))
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          outstandingPaymentsTable,
          unallocatedPaymentsTable,
          openPaymentsData.totalOpenPaymentsAmount
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
        val request = FakeRequest(GET, returns.routes.ViewPastPaymentsController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
