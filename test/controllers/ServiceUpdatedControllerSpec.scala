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
import connectors.UserAnswersConnector
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.audit.{AuditObligationData, AuditReturnStarted}
import models.{AlcoholRegimes, ObligationData, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.Helpers._
import services.AuditService
import uk.gov.hmrc.http.UpstreamErrorResponse
import views.html.ServiceUpdatedView

import java.time.Instant
import scala.concurrent.Future

class ServiceUpdatedControllerSpec extends SpecBase {
  "ServiceUpdated Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view" in new SetUp {
        val application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(
            GET,
            controllers.routes.ServiceUpdatedController.onPageLoad.url
          )

          val result = route(application, request).value

          val view = application.injector.instanceOf[ServiceUpdatedView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view()(
            request,
            getMessages(application)
          ).toString
        }
      }
    }

    "onSubmit" - {

      "must redirect to the TaskList Page when a userAnswers is successfully created for a POST" in new SetUp {
        val userAnswers = emptyUserAnswers.set(ObligationData, obligationDataSingleOpen).success.value
        when(mockUserAnswersConnector.createUserAnswers(any())(any())) thenReturn Future.successful(Right(userAnswers))

        val application = applicationBuilder()
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[AuditService].toInstance(mockAuditService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.routes.BeforeStartReturnController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
          verify(mockAuditService).audit(eqTo(expectedReturnStartedAuditEvent))(any(), any())
        }
      }

      "must redirect to the TaskList Page when a userAnswers is successfully created for a POST without sending the audit event if the obligation is not available" in new SetUp {
        val userAnswers = emptyUserAnswers.remove(ObligationData).success.value
        when(mockUserAnswersConnector.createUserAnswers(any())(any())) thenReturn Future.successful(Right(userAnswers))

        val application = applicationBuilder()
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[AuditService].toInstance(mockAuditService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.routes.BeforeStartReturnController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
        }

        verify(mockAuditService, times(0)).audit(any())(any(), any())
      }

      "must redirect to the JourneyRecovery Page when a userAnswers cannot be created for a POST" in new SetUp {
        when(mockUserAnswersConnector.createUserAnswers(any())(any())) thenReturn Future.successful(
          Left(mockUpstreamErrorResponse)
        )
        when(mockUpstreamErrorResponse.statusCode).thenReturn(INTERNAL_SERVER_ERROR)

        val application = applicationBuilder()
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[AuditService].toInstance(mockAuditService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.routes.BeforeStartReturnController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

          verify(mockAuditService, times(0)).audit(any())(any(), any())
        }
      }

      "must redirect to the journey recovery controller if the period key is not in the session for a POST" in new SetUp {
        when(mockUserAnswersConnector.createUserAnswers(any())(any())) thenReturn Future.successful(
          Right(emptyUserAnswers)
        )

        val application = applicationBuilder()
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[AuditService].toInstance(mockAuditService)
          )
          .build()

        running(application) {
          val request = play.api.test.FakeRequest(POST, controllers.routes.BeforeStartReturnController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

          verify(mockAuditService, times(0)).audit(any())(any(), any())
        }
      }
    }
  }

  class SetUp {
    val mockUserAnswersConnector       = mock[UserAnswersConnector]
    val mockAuditService: AuditService = mock[AuditService]
    val mockUpstreamErrorResponse      = mock[UpstreamErrorResponse]

    implicit val messages: Messages = getMessages(app)

    val emptyUserAnswers: UserAnswers = UserAnswers(
      returnId,
      groupId,
      internalId,
      regimes = AlcoholRegimes(Set(Beer, Cider, Wine, Spirits, OtherFermentedProduct)),
      startedTime = Instant.now(clock),
      lastUpdated = Instant.now(clock),
      validUntil = Some(Instant.now(clock))
    )

    val expectedReturnStartedAuditEvent = AuditReturnStarted(
      appaId = returnId.appaId,
      periodKey = returnId.periodKey,
      credentialId = emptyUserAnswers.internalId,
      groupId = emptyUserAnswers.groupId,
      obligationData = AuditObligationData(obligationDataSingleOpen),
      returnStartedTime = Instant.now(clock),
      returnValidUntilTime = Some(Instant.now(clock))
    )
  }
}
