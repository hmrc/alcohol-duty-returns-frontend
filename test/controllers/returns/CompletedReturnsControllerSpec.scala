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

import java.time.{Clock, LocalDate}
import scala.concurrent.Future

class CompletedReturnsControllerSpec extends SpecBase {
  "CompletedReturnsController" - {
    "must return OK and the correct view for a GET" in {
      val viewModelHelper = new ViewPastReturnsHelper(createDateTimeHelper(), clock)
      val mockConnector = mock[AlcoholDutyReturnsConnector]

      val previousYear = java.time.Year.now().getValue - 1

      val janReturn = obligationDataSingleFulfilled.copy(
        fromDate = LocalDate.of(previousYear, 1, 1),
        toDate = LocalDate.of(previousYear, 1, 31),
        periodKey = s"${previousYear.toString.takeRight(2)}AA"
      )

      val sepReturn = obligationDataSingleFulfilled.copy(
        fromDate = LocalDate.of(previousYear, 9, 1),
        toDate = LocalDate.of(previousYear, 9, 28),
        periodKey = s"${previousYear.toString.takeRight(2)}AI"
      )

      when(mockConnector.obligationDetails(any())(any()))
        .thenReturn(Future.successful(Seq(janReturn, sepReturn)))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyReturnsConnector].toInstance(mockConnector),
          bind[Clock].toInstance(clock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.CompletedReturnsController.onPageLoad(previousYear).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CompletedReturnsView]

        val expectedTable = viewModelHelper.getReturnsTable(Seq(sepReturn, janReturn))(getMessages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(expectedTable, previousYear)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET on Exception" in {
      val mockConnector = mock[AlcoholDutyReturnsConnector]

      when(mockConnector.obligationDetails(any())(any()))
        .thenReturn(Future.failed(new Exception("test Exception")))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyReturnsConnector].toInstance(mockConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.CompletedReturnsController.onPageLoad(2024).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
