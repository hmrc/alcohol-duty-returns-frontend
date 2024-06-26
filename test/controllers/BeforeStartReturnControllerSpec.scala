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

package controllers

import base.SpecBase
import connectors.CacheConnector
import models.ReturnPeriod
import org.mockito.ArgumentMatchers.any
import play.api.test.Helpers._
import play.api.inject.bind
import uk.gov.hmrc.http.HttpResponse
import viewmodels.checkAnswers.returns.ReturnPeriodViewModel
import views.html.BeforeStartReturnView

import scala.concurrent.Future

class BeforeStartReturnControllerSpec extends SpecBase {
  "BeforeStartReturn Controller" - {
    val mockCacheConnector = mock[CacheConnector]

    "must redirect to the TaskList Page if UserAnswers already exist for a GET" in {
      when(mockCacheConnector.get(any(), any())(any())) thenReturn Future.successful(Some(emptyUserAnswers))

      val application = applicationBuilder()
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.routes.BeforeStartReturnController.onPageLoad(emptyUserAnswers.returnId.periodKey).url
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
      }
    }

    "must return OK and the correct view for a GET if the userAnswer does not exist yet" in {
      when(mockCacheConnector.get(any(), any())(any())) thenReturn Future.successful(None)

      val application = applicationBuilder()
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.routes.BeforeStartReturnController.onPageLoad(emptyUserAnswers.returnId.periodKey).url
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[BeforeStartReturnView]

        val returnPeriodViewModel =
          ReturnPeriodViewModel(ReturnPeriod.fromPeriodKey(emptyUserAnswers.returnId.periodKey).get)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(returnPeriodViewModel)(request, getMessages(application)).toString
      }
    }

    "must redirect to the journey recovery controller if a bad period key is supplied" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.BeforeStartReturnController.onPageLoad(badPeriodKey).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the TaskList Page when a userAnswers is successfully created for a POST" in {
      val httpResponse = mock[HttpResponse]
      when(mockCacheConnector.createUserAnswers(any())(any())) thenReturn Future.successful(httpResponse)
      when(httpResponse.status).thenReturn(CREATED)

      val application = applicationBuilder()
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.routes.BeforeStartReturnController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
      }
    }

    "must redirect to the JourneyRecovery Page when a userAnswers cannot be created for a POST" in {
      val httpResponse = mock[HttpResponse]
      when(mockCacheConnector.createUserAnswers(any())(any())) thenReturn Future.successful(httpResponse)
      when(httpResponse.status).thenReturn(INTERNAL_SERVER_ERROR)
      when(httpResponse.body).thenReturn("Computer said No!")

      val application = applicationBuilder()
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.routes.BeforeStartReturnController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the journey recovery controller if the period key is not in the session for a POST" in {
      val httpResponse = mock[HttpResponse]
      when(mockCacheConnector.createUserAnswers(any())(any())) thenReturn Future.successful(httpResponse)
      when(httpResponse.status).thenReturn(CREATED)

      val application = applicationBuilder()
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = play.api.test.FakeRequest(POST, controllers.routes.BeforeStartReturnController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
