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

package controllers.spiritsQuestions

import base.SpecBase
import connectors.UserAnswersConnector
import generators.ModelGenerators
import models.SpiritType
import models.spiritsQuestions.Whisky
import org.mockito.ArgumentMatchers.any
import pages.spiritsQuestions.{DeclareSpiritsTotalPage, OtherSpiritsProducedPage, SpiritTypePage, WhiskyPage}
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import viewmodels.checkAnswers.spiritsQuestions.CheckYourAnswersSummaryListHelper
import views.html.spiritsQuestions.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with ModelGenerators {
  "CheckYourAnswers Controller" - {
    val completedUserAnswers = emptyUserAnswers
      .set(
        SpiritTypePage,
        Set[SpiritType](SpiritType.Other)
      )
      .success
      .value
      .set(OtherSpiritsProducedPage, "Coco Pops")
      .success
      .value
      .set(DeclareSpiritsTotalPage, BigDecimal(11))
      .success
      .value
      .set(WhiskyPage, Whisky(BigDecimal(1), BigDecimal(2)))
      .success
      .value

    "must return OK and the correct view for a GET if all necessary questions are answered (Other spirits is selected)" in {
      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(completedUserAnswers))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val spiritsList =
          CheckYourAnswersSummaryListHelper.spiritsSummaryList(completedUserAnswers)(getMessages(application)).get

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(spiritsList)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if any optional questions are not required (Other spirits is not selected)" in {
      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val userAnswers = completedUserAnswers
        .remove(List(OtherSpiritsProducedPage, SpiritTypePage))
        .success
        .value
        .set(
          SpiritTypePage,
          Set[SpiritType](SpiritType.Maltspirits, SpiritType.Grainspirits)
        )
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val spiritsList =
          CheckYourAnswersSummaryListHelper.spiritsSummaryList(userAnswers)(getMessages(application)).get

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(spiritsList)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if multiple Spirit types including Other are checked" in {
      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val userAnswers = completedUserAnswers
        .remove(List(OtherSpiritsProducedPage, SpiritTypePage))
        .success
        .value
        .set(
          SpiritTypePage,
          Set[SpiritType](SpiritType.Maltspirits, SpiritType.Grainspirits, SpiritType.Other)
        )
        .success
        .value
        .set(OtherSpiritsProducedPage, "Coco Pops")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val spiritsList =
          CheckYourAnswersSummaryListHelper.spiritsSummaryList(userAnswers)(getMessages(application)).get

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(spiritsList)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET" - {
      "if no existing data is found" in {
        val application = applicationBuilder(Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(GET, controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "if one of the necessary pages has not been populated" - {
        "whisky page is not populated" in {
          val mockUserAnswersConnector = mock[UserAnswersConnector]

          when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

          val userAnswers = completedUserAnswers
            .remove(List(OtherSpiritsProducedPage, SpiritTypePage, WhiskyPage))
            .success
            .value
            .set(
              SpiritTypePage,
              Set[SpiritType](SpiritType.NeutralAgriculturalOrigin)
            )
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
            )
            .build()

          running(application) {
            val request =
              FakeRequest(GET, controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url)

            val result = route(application, request).value

            status(result)                 mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "spirit type page is not populated" in {
          val userAnswers = completedUserAnswers
            .remove(List(OtherSpiritsProducedPage, SpiritTypePage))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request =
              FakeRequest(GET, controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url)

            val result = route(application, request).value

            status(result)                 mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "Other spirits is selected but other spirits produced page is not populated" in {
          val userAnswers = completedUserAnswers
            .remove(List(OtherSpiritsProducedPage))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request =
              FakeRequest(GET, controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url)

            val result = route(application, request).value

            status(result)                 mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
    }
  }
}
