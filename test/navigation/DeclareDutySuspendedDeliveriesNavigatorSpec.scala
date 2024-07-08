/*
 * Copyright 2023 HM Revenue & Customs
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

package navigation

import base.SpecBase
import controllers._
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models._
import pages._
import pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage
import play.api.libs.json.{JsObject, Json}

class DeclareDutySuspendedDeliveriesNavigatorSpec extends SpecBase {
  val navigator = new DeclareDutySuspendedDeliveriesNavigator

  "DeclareDutySuspendedDeliveriesNavigator" - {

    "in Normal mode" - {

      "must go from the Declare duty suspended deliveries question page to Duty suspended deliveries guidance page if the answer is Yes" in {

        navigator.nextPage(
          pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage,
          NormalMode,
          emptyUserAnswers.set(pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage, true).success.value
        ) mustBe controllers.dutySuspended.routes.DutySuspendedDeliveriesGuidanceController.onPageLoad()
      }

      "must go from the Declare duty suspended deliveries question page to task list page if the answer is No" in {
        navigator.nextPage(
          pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage,
          NormalMode,
          emptyUserAnswers.set(pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage, false).success.value
        ) mustBe routes.TaskListController.onPageLoad
      }

      Seq(
        (Beer, () => controllers.dutySuspended.routes.DutySuspendedBeerController.onPageLoad(NormalMode)),
        (Cider, () => controllers.dutySuspended.routes.DutySuspendedCiderController.onPageLoad(NormalMode)),
        (Wine, () => controllers.dutySuspended.routes.DutySuspendedWineController.onPageLoad(NormalMode)),
        (Spirits, () => controllers.dutySuspended.routes.DutySuspendedSpiritsController.onPageLoad(NormalMode)),
        (
          OtherFermentedProduct,
          () => controllers.dutySuspended.routes.DutySuspendedOtherFermentedController.onPageLoad(NormalMode)
        )
      ).foreach { case (regime, expectedPage) =>
        s"must go from the Duty suspended guidance page to the correct page when the next regime is ${regime.entryName}" in {
          navigator.nextPage(
            pages.dutySuspended.DutySuspendedGuidancePage,
            NormalMode,
            emptyUserAnswers.copy(regimes = AlcoholRegimes(Set(regime)))
          ) mustBe expectedPage()
        }
      }

      Seq(
        (Cider, () => controllers.dutySuspended.routes.DutySuspendedCiderController.onPageLoad(NormalMode)),
        (Wine, () => controllers.dutySuspended.routes.DutySuspendedWineController.onPageLoad(NormalMode)),
        (Spirits, () => controllers.dutySuspended.routes.DutySuspendedSpiritsController.onPageLoad(NormalMode))
      ).foreach { case (regime, expectedPage) =>
        s"must go from the Duty suspended beer page to the correct page when the next regime is ${regime.entryName}" in {
          navigator.nextPage(
            pages.dutySuspended.DutySuspendedBeerPage,
            NormalMode,
            emptyUserAnswers.copy(regimes = AlcoholRegimes(Set(Beer, regime)))
          ) mustBe expectedPage()
        }
      }

      "must go from the Duty suspended beer page to the check your answers page if no other regime was found" in {
        navigator.nextPage(
          pages.dutySuspended.DutySuspendedBeerPage,
          NormalMode,
          userAnswersWithBeer
        ) mustBe controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad()
      }

      Seq(
        (Wine, () => controllers.dutySuspended.routes.DutySuspendedWineController.onPageLoad(NormalMode)),
        (Spirits, () => controllers.dutySuspended.routes.DutySuspendedSpiritsController.onPageLoad(NormalMode)),
        (
          OtherFermentedProduct,
          () => controllers.dutySuspended.routes.DutySuspendedOtherFermentedController.onPageLoad(NormalMode)
        )
      ).foreach { case (regime, expectedPage) =>
        s"must go from the Duty suspended cider page to the correct page when the next regime is ${regime.entryName}" in {
          navigator.nextPage(
            pages.dutySuspended.DutySuspendedCiderPage,
            NormalMode,
            emptyUserAnswers.copy(regimes = AlcoholRegimes(Set(Beer, Cider, regime)))
          ) mustBe expectedPage()
        }
      }

      "must go from the Duty suspended cider page to the Other fermented product page if no other regime was found" in {
        navigator.nextPage(
          pages.dutySuspended.DutySuspendedCiderPage,
          NormalMode,
          userAnswersWithCider
        ) mustBe controllers.dutySuspended.routes.DutySuspendedOtherFermentedController.onPageLoad(NormalMode)
      }

      Seq(
        (Spirits, () => controllers.dutySuspended.routes.DutySuspendedSpiritsController.onPageLoad(NormalMode)),
        (
          OtherFermentedProduct,
          () => controllers.dutySuspended.routes.DutySuspendedOtherFermentedController.onPageLoad(NormalMode)
        )
      ).foreach { case (regime, expectedPage) =>
        s"must go from the Duty suspended wine page to the correct page when the next regime is ${regime.entryName}" in {
          navigator.nextPage(
            pages.dutySuspended.DutySuspendedWinePage,
            NormalMode,
            emptyUserAnswers.copy(regimes = AlcoholRegimes(Set(Beer, Cider, Wine, regime)))
          ) mustBe expectedPage()
        }
      }

      "must go from the Duty suspended wine page to the Other fermented product page if no other regime was found" in {
        navigator.nextPage(
          pages.dutySuspended.DutySuspendedWinePage,
          NormalMode,
          userAnswersWithWine
        ) mustBe controllers.dutySuspended.routes.DutySuspendedOtherFermentedController.onPageLoad(NormalMode)
      }

      "must go from the Duty suspended spirits page to the correct page when the next regime is OtherFermentedProduct" in {
        navigator.nextPage(
          pages.dutySuspended.DutySuspendedSpiritsPage,
          NormalMode,
          userAnswersWithAllRegimes
        ) mustBe controllers.dutySuspended.routes.DutySuspendedOtherFermentedController.onPageLoad(NormalMode)
      }

      "must go from the Duty suspended spirits page to the check your answers page if no other regime was found" in {
        navigator.nextPage(
          pages.dutySuspended.DutySuspendedSpiritsPage,
          NormalMode,
          userAnswersWithSpirits
        ) mustBe controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad()
      }

      "must go from the Duty suspended deliveries other fermented products page to CYA page" in {
        navigator.nextPage(
          pages.dutySuspended.DutySuspendedOtherFermentedPage,
          NormalMode,
          userAnswersWithAllRegimes
        ) mustBe controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad()
      }

      "must go from the Declare duty suspended deliveries question page to journey recovery page if the answer there is not an answer" in {

        navigator.nextPage(
          pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.JourneyRecoveryController.onPageLoad(None)
      }

      "must go from the Declare duty suspended deliveries outside UK page to Declare duty suspended deliveries inside UK page" in {

        navigator.nextPage(
          pages.dutySuspended.DeclareDutySuspendedDeliveriesOutsideUkPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.dutySuspended.routes.DutySuspendedDeliveriesController.onPageLoad(NormalMode)
      }

      "must go from the Declare duty suspended deliveries inside UK page to Declare duty suspended received page" in {

        navigator.nextPage(
          pages.dutySuspended.DutySuspendedDeliveriesPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.dutySuspended.routes.DeclareDutySuspendedReceivedController.onPageLoad(NormalMode)
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad()
      }

      "must go from the Declare DSD question page to the task list when the answer is No" in {
        navigator.nextPage(
          DeclareDutySuspendedDeliveriesQuestionPage,
          CheckMode,
          emptyUserAnswers
            .set(
              DeclareDutySuspendedDeliveriesQuestionPage,
              false
            )
            .success
            .value
        ) mustBe routes.TaskListController.onPageLoad
      }

      "must go from Declare DSD Question page to Guidance page in Check Mode if answer is Yes" in {
        navigator.nextPage(
          pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage,
          CheckMode,
          emptyUserAnswers.set(pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage, true).success.value
        ) mustBe controllers.dutySuspended.routes.DutySuspendedDeliveriesGuidanceController.onPageLoad()
      }

      "must go from DSD Beer page to CYA page in Check Mode" in {
        navigator.nextPage(
          pages.dutySuspended.DutySuspendedBeerPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad()
      }

      "must go from DSD Cider page to CYA page in Check Mode" in {
        navigator.nextPage(
          pages.dutySuspended.DutySuspendedCiderPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad()
      }

      "must go from DSD Wine page to CYA page in Check Mode" in {
        navigator.nextPage(
          pages.dutySuspended.DutySuspendedWinePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad()
      }

      "must go from DSD Spirits page to CYA page in Check Mode" in {
        navigator.nextPage(
          pages.dutySuspended.DutySuspendedSpiritsPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad()
      }

      "must go from DSD Other Fermented products page to CYA page in Check Mode" in {
        navigator.nextPage(
          pages.dutySuspended.DutySuspendedOtherFermentedPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad()
      }
    }
  }
}
