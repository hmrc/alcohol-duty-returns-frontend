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
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.{AlcoholRegimes, ReturnPeriod, UserAnswers}
import models.audit.AuditContinueReturn
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.http.Status.LOCKED
import play.api.test.Helpers._
import play.api.inject.bind
import services.AuditService
import uk.gov.hmrc.http.{HttpResponse, UpstreamErrorResponse}
import viewmodels.WarningTextViewModel
import viewmodels.returns.ReturnPeriodViewModel
import viewmodels.returns.ReturnPeriodViewModel.viewDateFormatter
import views.html.BeforeStartReturnView

import java.time.{Clock, Instant, LocalDate}
import scala.concurrent.Future

class BeforeStartReturnControllerSpec extends SpecBase {
  "BeforeStartReturn Controller" - {
    val mockCacheConnector             = mock[CacheConnector]
    val mockAuditService: AuditService = mock[AuditService]

    val emptyUserAnswers: UserAnswers = UserAnswers(
      returnId,
      groupId,
      internalId,
      regimes = AlcoholRegimes(Set(Beer, Cider, Wine, Spirits, OtherFermentedProduct)),
      lastUpdated = Instant.now(clock),
      validUntil = Some(Instant.now(clock))
    )

    val expectedAuditEvent = AuditContinueReturn(
      appaId = returnId.appaId,
      periodKey = returnId.periodKey,
      credentialId = emptyUserAnswers.internalId,
      groupId = emptyUserAnswers.groupId,
      returnContinueTime = Instant.now(clock),
      returnStartedTime = Instant.now(clock),
      returnValidUntilTime = Some(Instant.now(clock))
    )

    val currentDate = LocalDate.now(clock)
    val viewModel   = WarningTextViewModel(returnPeriod, currentDate)

    "must redirect to the TaskList Page if UserAnswers already exist for a GET with audit event" in {
      when(mockCacheConnector.get(any(), any())(any())) thenReturn Future.successful(Right(emptyUserAnswers))

      val application = applicationBuilder()
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector),
          bind[AuditService].toInstance(mockAuditService),
          bind(classOf[Clock]).toInstance(clock)
        )
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.routes.BeforeStartReturnController.onPageLoad(emptyUserAnswers.returnId.periodKey).url
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockAuditService).audit(eqTo(expectedAuditEvent))(any(), any())
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
      }
    }

    "must return OK and the correct view for a GET if the userAnswer does not exist yet" in {
      val mockUpstreamErrorResponse = mock[UpstreamErrorResponse]
      when(mockUpstreamErrorResponse.statusCode).thenReturn(NOT_FOUND)
      when(mockCacheConnector.get(any(), any())(any())) thenReturn Future.successful(Left(mockUpstreamErrorResponse))

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
        contentAsString(result) mustEqual view(returnPeriodViewModel, viewModel)(
          request,
          getMessages(application)
        ).toString
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

    "must redirect to the return locked controller if the return is locked" in {
      val mockUpstreamErrorResponse = mock[UpstreamErrorResponse]
      when(mockUpstreamErrorResponse.statusCode).thenReturn(LOCKED)

      when(mockCacheConnector.get(any(), any())(any())) thenReturn Future(
        Left(mockUpstreamErrorResponse)
      )

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
        redirectLocation(result).value mustEqual controllers.routes.ReturnLockedController.onPageLoad().url
      }
    }

    "must redirect to journey recovery controller if the cache connector return a bad request" in {
      val mockUpstreamErrorResponse = mock[UpstreamErrorResponse]
      when(mockUpstreamErrorResponse.statusCode).thenReturn(BAD_REQUEST)

      when(mockCacheConnector.get(any(), any())(any())) thenReturn Future(
        Left(mockUpstreamErrorResponse)
      )

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
