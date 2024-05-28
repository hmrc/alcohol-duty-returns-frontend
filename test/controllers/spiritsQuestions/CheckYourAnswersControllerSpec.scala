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
import connectors.CacheConnector
import generators.ModelGenerators
import models.UnitsOfMeasure.Tonnes
import models.spiritsQuestions.{AlcoholUsed, EthyleneGasOrMolassesUsed, GrainsUsed, OtherIngredientsUsed, OtherMaltedGrains, Whisky}
import models.SpiritType
import org.mockito.ArgumentMatchers.any
import pages.spiritsQuestions.{AlcoholUsedPage, DeclareSpiritsTotalPage, EthyleneGasOrMolassesUsedPage, GrainsUsedPage, OtherIngredientsUsedPage, OtherMaltedGrainsPage, OtherSpiritsProducedPage, SpiritTypePage, WhiskyPage}
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
      .set(GrainsUsedPage, GrainsUsed(BigDecimal(1), BigDecimal(2), BigDecimal(3), BigDecimal(4), BigDecimal(5), true))
      .success
      .value
      .set(AlcoholUsedPage, AlcoholUsed(BigDecimal(1), BigDecimal(2), BigDecimal(3), BigDecimal(4)))
      .success
      .value
      .set(WhiskyPage, Whisky(BigDecimal(1), BigDecimal(2)))
      .success
      .value
      .set(OtherMaltedGrainsPage, OtherMaltedGrains("test other malted grains", BigDecimal(1)))
      .success
      .value
      .set(EthyleneGasOrMolassesUsedPage, EthyleneGasOrMolassesUsed(BigDecimal(1), BigDecimal(2), true))
      .success
      .value
      .set(OtherIngredientsUsedPage, OtherIngredientsUsed("test other ingredients", Tonnes, BigDecimal(1)))
      .success
      .value

    "must return OK and the correct view for a GET if all necessary questions are answered" in {
      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(completedUserAnswers))
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val spiritsList          =
          CheckYourAnswersSummaryListHelper.spiritsSummaryList(completedUserAnswers)(messages(application)).get
        val alcoholList          =
          CheckYourAnswersSummaryListHelper.alcoholUsedSummaryList(completedUserAnswers)(messages(application)).get
        val grainsList           =
          CheckYourAnswersSummaryListHelper.grainsUsedSummaryList(completedUserAnswers)(messages(application)).get
        val otherIngredientsList = CheckYourAnswersSummaryListHelper
          .otherIngredientsUsedSummaryList(completedUserAnswers)(messages(application))
          .get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(spiritsList, alcoholList, grainsList, otherIngredientsList)(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if any optional questions are not answered" in {
      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val userAnswers = completedUserAnswers
        .remove(List(OtherMaltedGrainsPage, OtherSpiritsProducedPage, OtherIngredientsUsedPage, SpiritTypePage))
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
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val spiritsList          =
          CheckYourAnswersSummaryListHelper.spiritsSummaryList(userAnswers)(messages(application)).get
        val alcoholList          =
          CheckYourAnswersSummaryListHelper.alcoholUsedSummaryList(userAnswers)(messages(application)).get
        val grainsList           =
          CheckYourAnswersSummaryListHelper.grainsUsedSummaryList(userAnswers)(messages(application)).get
        val otherIngredientsList = CheckYourAnswersSummaryListHelper
          .otherIngredientsUsedSummaryList(userAnswers)(messages(application))
          .get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(spiritsList, alcoholList, grainsList, otherIngredientsList)(
          request,
          messages(application)
        ).toString
      }

    }
    "must return OK and the correct view for a GET if multiple Spirit types including Other are checked" in {
      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val userAnswers = completedUserAnswers
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
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val spiritsList          =
          CheckYourAnswersSummaryListHelper.spiritsSummaryList(userAnswers)(messages(application)).get
        val alcoholList          =
          CheckYourAnswersSummaryListHelper.alcoholUsedSummaryList(userAnswers)(messages(application)).get
        val grainsList           =
          CheckYourAnswersSummaryListHelper.grainsUsedSummaryList(userAnswers)(messages(application)).get
        val otherIngredientsList = CheckYourAnswersSummaryListHelper
          .otherIngredientsUsedSummaryList(userAnswers)(messages(application))
          .get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(spiritsList, alcoholList, grainsList, otherIngredientsList)(
          request,
          messages(application)
        ).toString
      }

    }
    "must redirect to Journey Recovery for a GET" - {

      "if no existing data is found" in {

        val application = applicationBuilder(Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(
              GET,
              controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
