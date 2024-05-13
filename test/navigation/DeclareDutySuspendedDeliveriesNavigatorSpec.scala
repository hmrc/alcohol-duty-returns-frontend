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
import models._
import pages._
import pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage

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

      "must go from the duty suspended deliveries beer page to Duty suspended deliveries cider page" in {

        navigator.nextPage(
          pages.dutySuspended.DutySuspendedBeerPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.dutySuspended.routes.DutySuspendedCiderController.onPageLoad(NormalMode)
      }

      "must go from the duty suspended deliveries cider page to Duty suspended deliveries wine page" in {

        navigator.nextPage(
          pages.dutySuspended.DutySuspendedCiderPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.dutySuspended.routes.DutySuspendedWineController.onPageLoad(NormalMode)
      }

      "must go from the duty suspended deliveries wine page to Duty suspended deliveries spirits page" in {

        navigator.nextPage(
          pages.dutySuspended.DutySuspendedWinePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.dutySuspended.routes.DutySuspendedSpiritsController.onPageLoad(NormalMode)
      }

      "must go from the duty suspended deliveries spirits page to Duty suspended deliveries other fermented products page" in {

        navigator.nextPage(
          pages.dutySuspended.DutySuspendedSpiritsPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe controllers.dutySuspended.routes.DutySuspendedOtherFermentedController.onPageLoad(NormalMode)
      }
      "must go from the duty suspended deliveries other fermented products page to CYA page" in {

        navigator.nextPage(
          pages.dutySuspended.DutySuspendedOtherFermentedPage,
          NormalMode,
          emptyUserAnswers
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
