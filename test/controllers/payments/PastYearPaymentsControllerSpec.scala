/*
 * Copyright 2025 HM Revenue & Customs
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
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.Helpers._
import viewmodels.payments.ViewPastPaymentsHelper
import views.html.payments.PastYearPaymentsView

import scala.concurrent.Future

class PastYearPaymentsControllerSpec extends SpecBase {

  val year = 2024

  lazy val pastYearPaymentsRoute = routes.PastYearPaymentsController.onPageLoad(year).url

  "PastYearPayments Controller" - {
    "must return OK and the correct view for a GET" in {
      val viewModelHelper                  = new ViewPastPaymentsHelper(createDateTimeHelper(), appConfig, clock)
      val mockAlcoholDutyAccountsConnector = mock[AlcoholDutyAccountConnector]
      when(mockAlcoholDutyAccountsConnector.historicPayments(any())(any())) thenReturn Future.successful(
        historicPaymentsData
      )

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyAccountConnector].toInstance(mockAlcoholDutyAccountsConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, pastYearPaymentsRoute)
        val result  = route(application, request).value

        val view                  = application.injector.instanceOf[PastYearPaymentsView]
        val historicPaymentsTable =
          viewModelHelper.getHistoricPaymentsTable(historicPayments2024.payments)(getMessages(application))

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(year, historicPaymentsTable)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery if the year has no past payments" in {
      val mockAlcoholDutyAccountsConnector = mock[AlcoholDutyAccountConnector]
      when(mockAlcoholDutyAccountsConnector.historicPayments(any())(any())) thenReturn Future.successful(
        historicPaymentsData2
      )

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyAccountConnector].toInstance(mockAlcoholDutyAccountsConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, pastYearPaymentsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery if the year is out of range" in {
      val yearOutOfRange = 2020

      val mockAlcoholDutyAccountsConnector = mock[AlcoholDutyAccountConnector]
      when(mockAlcoholDutyAccountsConnector.historicPayments(any())(any())) thenReturn Future.successful(
        historicPaymentsData
      )

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyAccountConnector].toInstance(mockAlcoholDutyAccountsConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.PastYearPaymentsController.onPageLoad(yearOutOfRange).url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery if there is an Exception due to a failed Future" in {
      val mockAlcoholDutyAccountsConnector = mock[AlcoholDutyAccountConnector]
      when(mockAlcoholDutyAccountsConnector.historicPayments(any())(any())) thenReturn Future.failed(
        new Exception("test Exception")
      )

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyAccountConnector].toInstance(mockAlcoholDutyAccountsConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, pastYearPaymentsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
