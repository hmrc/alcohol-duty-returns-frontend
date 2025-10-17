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
import models.FulfilledObligations
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import play.api.test.Helpers._
import viewmodels.returns.ViewPastReturnsHelper
import views.html.returns.ViewPastReturnsView

import java.time.Clock
import scala.concurrent.Future

class ViewPastReturnsControllerSpec extends SpecBase {

  val clockYear = 2024

  "ViewPastReturns Controller" - {
    "must return OK and the correct view for a GET" - {
      "if there are completed returns from past years" in {
        val viewModelHelper                 = new ViewPastReturnsHelper(createDateTimeHelper(), clock)
        val mockAlcoholDutyReturnsConnector = mock[AlcoholDutyReturnsConnector]

        when(mockAlcoholDutyReturnsConnector.openObligations(any())(any())) thenReturn Future.successful(
          multipleOpenObligations
        )
        when(mockAlcoholDutyReturnsConnector.fulfilledObligations(any())(any())) thenReturn Future.successful(
          fulfilledObligationData
        )

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[AlcoholDutyReturnsConnector].toInstance(mockAlcoholDutyReturnsConnector),
            bind[Clock].toInstance(clock)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.returns.routes.ViewPastReturnsController.onPageLoad.url)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[ViewPastReturnsView]

          val outstandingReturnsTable =
            viewModelHelper.getReturnsTable(multipleOpenObligations)(getMessages(application))
          val completedReturnsTable   =
            viewModelHelper.getReturnsTable(multipleFulfilledObligations)(getMessages(application))
          val pastYears               = Seq(2022)

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(outstandingReturnsTable, completedReturnsTable, clockYear, pastYears)(
            request,
            getMessages(application)
          ).toString
        }
      }

      "if there are no completed returns from past years" in {
        val viewModelHelper                 = new ViewPastReturnsHelper(createDateTimeHelper(), clock)
        val mockAlcoholDutyReturnsConnector = mock[AlcoholDutyReturnsConnector]

        when(mockAlcoholDutyReturnsConnector.openObligations(any())(any())) thenReturn Future.successful(
          multipleOpenObligations
        )
        when(mockAlcoholDutyReturnsConnector.fulfilledObligations(any())(any())) thenReturn Future.successful(
          Seq(FulfilledObligations(2024, Seq(obligationDataSingleFulfilled)))
        )

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[AlcoholDutyReturnsConnector].toInstance(mockAlcoholDutyReturnsConnector),
            bind[Clock].toInstance(clock)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.returns.routes.ViewPastReturnsController.onPageLoad.url)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[ViewPastReturnsView]

          val outstandingReturnsTable =
            viewModelHelper.getReturnsTable(multipleOpenObligations)(getMessages(application))
          val completedReturnsTable   =
            viewModelHelper.getReturnsTable(Seq(obligationDataSingleFulfilled))(getMessages(application))
          val pastYears               = Seq.empty

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(outstandingReturnsTable, completedReturnsTable, clockYear, pastYears)(
            request,
            getMessages(application)
          ).toString
        }
      }
    }

    "must redirect to Journey Recovery if there is an Exception due to the fulfilled obligations list being absent for the current year" in {
      val mockAlcoholDutyReturnsConnector = mock[AlcoholDutyReturnsConnector]

      when(mockAlcoholDutyReturnsConnector.openObligations(any())(any())) thenReturn Future.successful(
        multipleOpenObligations
      )
      when(mockAlcoholDutyReturnsConnector.fulfilledObligations(any())(any())) thenReturn Future.successful(
        Seq(FulfilledObligations(2023, Seq(obligationDataSingleFulfilled)))
      )

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyReturnsConnector].toInstance(mockAlcoholDutyReturnsConnector),
          bind[Clock].toInstance(clock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.returns.routes.ViewPastReturnsController.onPageLoad.url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery if there is an Exception due to a failed Future" in {
      val mockAlcoholDutyReturnsConnector = mock[AlcoholDutyReturnsConnector]

      when(mockAlcoholDutyReturnsConnector.openObligations(any())(any())) thenReturn Future.failed(
        new Exception("test Exception")
      )
      when(mockAlcoholDutyReturnsConnector.fulfilledObligations(any())(any())) thenReturn Future.successful(
        fulfilledObligationData
      )

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyReturnsConnector].toInstance(mockAlcoholDutyReturnsConnector),
          bind[Clock].toInstance(clock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.returns.routes.ViewPastReturnsController.onPageLoad.url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
