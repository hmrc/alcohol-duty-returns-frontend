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
import models.{AlcoholRegimes, ErrorModel, ObligationData, ReturnPeriod, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.Helpers._
import services.BeforeStartReturnService
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.UserAnswersAuditHelper
import viewmodels.{BeforeStartReturnViewModel, ReturnPeriodViewModel, ReturnPeriodViewModelFactory}
import views.html.BeforeStartReturnView

import java.time.{Clock, Instant, LocalDate}
import scala.concurrent.Future

class BeforeStartReturnControllerSpec extends SpecBase {
  "BeforeStartReturn Controller" - {

    "onPageLoad" - {

      "if UserAnswers already exist" - {

        "must redirect to the TaskList Page with audit event if subscription and obligation status are valid and regimes match the API" in new SetUp {
          when(mockUserAnswersConnector.get(any(), any())(any())) thenReturn Future.successful(Right(emptyUserAnswers))
          when(mockBeforeStartReturnService.handleExistingUserAnswers(any())(any())) thenReturn Future.successful(
            Right((): Unit)
          )

          val application = applicationBuilder()
            .overrides(
              bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
              bind[BeforeStartReturnService].toInstance(mockBeforeStartReturnService),
              bind[UserAnswersAuditHelper].toInstance(mockUserAnswersAuditHelper),
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

            verify(mockUserAnswersAuditHelper).auditContinueReturn(
              eqTo(emptyUserAnswers),
              eqTo(periodKey),
              eqTo(appaId),
              eqTo(internalId),
              eqTo(groupId)
            )(any())
            redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
          }
        }

        "must redirect to the Service Updated Page if subscription and obligation status are valid but regimes do not match the API" in new SetUp {
          when(mockUserAnswersConnector.get(any(), any())(any())) thenReturn Future.successful(Right(emptyUserAnswers))
          when(mockBeforeStartReturnService.handleExistingUserAnswers(any())(any())) thenReturn Future.successful(
            Left(ErrorModel(CONFLICT, "Alcohol regimes in existing user answers do not match those from API"))
          )

          val application = applicationBuilder()
            .overrides(
              bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
              bind[BeforeStartReturnService].toInstance(mockBeforeStartReturnService),
              bind[Clock].toInstance(clock)
            )
            .build()

          running(application) {
            val request = FakeRequest(
              GET,
              controllers.routes.BeforeStartReturnController.onPageLoad(emptyUserAnswers.returnId.periodKey).url
            )

            val result = route(application, request).value

            status(result)                 mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.ServiceUpdatedController.onPageLoad.url
          }
        }

        "must redirect to the journey recovery controller if subscription or obligation status is no longer valid or another error occurred" in new SetUp {
          when(mockUserAnswersConnector.get(any(), any())(any())) thenReturn Future.successful(Right(emptyUserAnswers))
          when(mockBeforeStartReturnService.handleExistingUserAnswers(any())(any())) thenReturn Future.successful(
            Left(ErrorModel(INTERNAL_SERVER_ERROR, "No open obligation found."))
          )

          val application = applicationBuilder()
            .overrides(
              bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
              bind[BeforeStartReturnService].toInstance(mockBeforeStartReturnService),
              bind[Clock].toInstance(clock)
            )
            .build()

          running(application) {
            val request = FakeRequest(
              GET,
              controllers.routes.BeforeStartReturnController.onPageLoad(emptyUserAnswers.returnId.periodKey).url
            )

            val result = route(application, request).value

            status(result)                 mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }

      "must return OK and the correct view for a GET if the userAnswer does not exist yet" in new SetUp {
        when(mockUpstreamErrorResponse.statusCode).thenReturn(NOT_FOUND)
        when(mockUserAnswersConnector.get(any(), any())(any())) thenReturn Future.successful(
          Left(mockUpstreamErrorResponse)
        )

        when(mockReturnPeriodViewModelFactory(any)(any)).thenReturn(returnPeriodViewModel)

        val application = applicationBuilder()
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[ReturnPeriodViewModelFactory].toInstance(mockReturnPeriodViewModelFactory),
            bind[Clock].toInstance(clock)
          )
          .build()

        running(application) {
          val request = FakeRequest(
            GET,
            controllers.routes.BeforeStartReturnController.onPageLoad(periodKeyToTest).url
          )

          when(mockReturnPeriodViewModelFactory(any)(any)).thenReturn(returnPeriodViewModel)

          val result = route(application, request).value

          val view = application.injector.instanceOf[BeforeStartReturnView]

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(returnPeriodViewModel, beforeStartReturnViewModel)(
            request,
            getMessages(application)
          ).toString
        }

        verify(mockReturnPeriodViewModelFactory, times(1)).apply(returnPeriodToTest)(getMessages(application))
      }

      "must redirect to the journey recovery controller if a bad period key is supplied" in new SetUp {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[Clock].toInstance(clock)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.routes.BeforeStartReturnController.onPageLoad(badPeriodKey).url)

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to the return locked controller if the return is locked" in new SetUp {
        when(mockUpstreamErrorResponse.statusCode).thenReturn(LOCKED)

        when(mockUserAnswersConnector.get(any(), any())(any())) thenReturn Future(
          Left(mockUpstreamErrorResponse)
        )

        val application = applicationBuilder()
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[Clock].toInstance(clock)
          )
          .build()

        running(application) {
          val request = FakeRequest(
            GET,
            controllers.routes.BeforeStartReturnController.onPageLoad(periodKeyToTest).url
          )

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnLockedController.onPageLoad().url
        }
      }

      "must redirect to journey recovery controller if the user answers connector return a bad request" in new SetUp {
        when(mockUpstreamErrorResponse.statusCode).thenReturn(BAD_REQUEST)

        when(mockUserAnswersConnector.get(any(), any())(any())) thenReturn Future(
          Left(mockUpstreamErrorResponse)
        )

        val application = applicationBuilder()
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[Clock].toInstance(clock)
          )
          .build()

        running(application) {
          val request = FakeRequest(
            GET,
            controllers.routes.BeforeStartReturnController.onPageLoad(periodKeyToTest).url
          )

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
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
            bind[UserAnswersAuditHelper].toInstance(mockUserAnswersAuditHelper),
            bind[Clock].toInstance(clock)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.routes.BeforeStartReturnController.onSubmit().url)

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
          verify(mockUserAnswersAuditHelper).auditReturnStarted(eqTo(userAnswers))(any())
        }
      }

      "must redirect to the JourneyRecovery Page when a userAnswers cannot be created for a POST" in new SetUp {
        when(mockUserAnswersConnector.createUserAnswers(any())(any())) thenReturn Future.successful(
          Left(mockUpstreamErrorResponse)
        )
        when(mockUpstreamErrorResponse.statusCode).thenReturn(INTERNAL_SERVER_ERROR)

        val application = applicationBuilder()
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[UserAnswersAuditHelper].toInstance(mockUserAnswersAuditHelper),
            bind[Clock].toInstance(clock)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.routes.BeforeStartReturnController.onSubmit().url)

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

          verify(mockUserAnswersAuditHelper, times(0)).auditReturnStarted(any())(any())
        }
      }

      "must redirect to the journey recovery controller if the period key is not in the session for a POST" in new SetUp {
        when(mockUserAnswersConnector.createUserAnswers(any())(any())) thenReturn Future.successful(
          Right(emptyUserAnswers)
        )

        val application = applicationBuilder()
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[UserAnswersAuditHelper].toInstance(mockUserAnswersAuditHelper),
            bind[Clock].toInstance(clock)
          )
          .build()

        running(application) {
          val request = play.api.test.FakeRequest(POST, controllers.routes.BeforeStartReturnController.onSubmit().url)

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

          verify(mockUserAnswersAuditHelper, times(0)).auditReturnStarted(any())(any())
        }
      }
    }
  }

  class SetUp {
    val mockUserAnswersConnector         = mock[UserAnswersConnector]
    val mockBeforeStartReturnService     = mock[BeforeStartReturnService]
    val mockUserAnswersAuditHelper       = mock[UserAnswersAuditHelper]
    val mockUpstreamErrorResponse        = mock[UpstreamErrorResponse]
    val mockReturnPeriodViewModelFactory = mock[ReturnPeriodViewModelFactory]

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

    val currentDate    = LocalDate.now(clock)
    val dateTimeHelper = createDateTimeHelper()

    val periodKeyToTest = periodKeyJun
    val returnPeriodToTest = ReturnPeriod.fromPeriodKeyOrThrow(periodKeyToTest)

    val periodFromDate = returnPeriodToTest.periodFromDate()
    val periodToDate = returnPeriodToTest.periodToDate()
    val periodDueDate = returnPeriodToTest.periodDueDate()

    val beforeStartReturnViewModel = BeforeStartReturnViewModel(periodDueDate, dateTimeHelper.formatDateMonthYear(periodDueDate), LocalDate.now(clock))
    val returnPeriodViewModel      =     ReturnPeriodViewModel(
      dateTimeHelper.formatDateMonthYear(periodFromDate),
      dateTimeHelper.formatDateMonthYear(periodToDate),
      dateTimeHelper.formatDateMonthYear(periodDueDate)
    )
  }
}
