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
import connectors.AlcoholDutyReturnsConnector
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import play.api.test.Helpers._
import viewmodels.returns.ViewPastReturnsHelper
import views.html.returns.CompletedReturnsView

import scala.concurrent.Future

class CompletedReturnsControllerSpec extends SpecBase {

  val year = 2024

  lazy val completedReturnsRoute = routes.CompletedReturnsController.onPageLoad(year).url

  "CompletedReturnsController" - {
    "must return OK and the correct view for a GET" in {
      val viewModelHelper = new ViewPastReturnsHelper(createDateTimeHelper(), clock)
      val mockConnector   = mock[AlcoholDutyReturnsConnector]

      when(mockConnector.fulfilledObligations(any())(any()))
        .thenReturn(Future.successful(fulfilledObligationData))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyReturnsConnector].toInstance(mockConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, completedReturnsRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[CompletedReturnsView]

        val expectedTable = viewModelHelper.getReturnsTable(multipleFulfilledObligations)(getMessages(application))

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(expectedTable, year)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery if the year has no completed returns" in {
      val mockConnector = mock[AlcoholDutyReturnsConnector]

      when(mockConnector.fulfilledObligations(any())(any()))
        .thenReturn(Future.successful(fulfilledObligationData))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyReturnsConnector].toInstance(mockConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.CompletedReturnsController.onPageLoad(2023).url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery if the year is out of range" in {
      val mockConnector = mock[AlcoholDutyReturnsConnector]

      when(mockConnector.fulfilledObligations(any())(any()))
        .thenReturn(Future.successful(fulfilledObligationData))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyReturnsConnector].toInstance(mockConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.CompletedReturnsController.onPageLoad(2020).url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery if there is an Exception due to a failed Future" in {
      val mockConnector = mock[AlcoholDutyReturnsConnector]

      when(mockConnector.fulfilledObligations(any())(any()))
        .thenReturn(Future.failed(new Exception("test Exception")))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyReturnsConnector].toInstance(mockConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, completedReturnsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
