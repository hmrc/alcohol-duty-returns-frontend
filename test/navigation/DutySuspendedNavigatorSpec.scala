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
import pages.dutySuspendedNew._

class DutySuspendedNavigatorSpec extends SpecBase {

  val navigator = new DutySuspendedNavigator
  val regime    = regimeGen.sample.value

  "DutySuspendedNavigator" - {

    "in Normal mode" - {

      "nextPage" - {
        "must go from a page that doesn't exist in the route map to the Task List page" in {
          case object UnknownPage extends Page
          navigator.nextPage(
            UnknownPage,
            NormalMode,
            emptyUserAnswers,
            Some(false)
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the Declare duty suspense question page to Duty suspended alcohol types page if the answer is Yes" in {
          navigator.nextPage(
            DeclareDutySuspenseQuestionPage,
            NormalMode,
            emptyUserAnswers.set(DeclareDutySuspenseQuestionPage, true).success.value,
            Some(false)
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
          // TODO: update route when new page is created
        }

        "must go from the Declare duty suspense question page to the Declare quantity page if the user has only 1 approval" in {
          navigator.nextPage(
            DeclareDutySuspenseQuestionPage,
            NormalMode,
            emptyUserAnswers
              .copy(regimes = AlcoholRegimes(Set(Beer)))
              .set(DeclareDutySuspenseQuestionPage, true)
              .success
              .value,
            Some(false)
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
          // TODO: update route when new page is created
        }

        "must go from the Declare duty suspense question page to task list page if the answer is No" in {
          navigator.nextPage(
            DeclareDutySuspenseQuestionPage,
            NormalMode,
            emptyUserAnswers.set(DeclareDutySuspenseQuestionPage, false).success.value,
            Some(false)
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from the Declare duty suspense question page to journey recovery page if the answer is missing" in {
          navigator.nextPage(
            DeclareDutySuspenseQuestionPage,
            NormalMode,
            emptyUserAnswers,
            Some(false)
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "nextPageWithRegime" - {
        "must go from a page that doesn't exist in the route map to the Task List page" in {
          case object UnknownPage extends Page
          navigator.nextPageWithRegime(
            UnknownPage,
            NormalMode,
            emptyUserAnswers,
            regime
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
          // TODO: update route to task list when nextPageWithRegime is implemented properly
        }
      }
    }

    "in Check mode" - {
      "nextPage" - {
        "must go from a page that doesn't exist in the edit route map to the Task List page" in {
          case object UnknownPage extends Page
          navigator.nextPage(
            UnknownPage,
            CheckMode,
            emptyUserAnswers,
            Some(false)
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go from Declare duty suspense question page to Duty suspended alcohol types page if the answer is Yes" in {
          navigator.nextPage(
            DeclareDutySuspenseQuestionPage,
            CheckMode,
            emptyUserAnswers.set(DeclareDutySuspenseQuestionPage, true).success.value,
            Some(false)
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
          // TODO: update route when new page is created
        }

        "must go from the Declare duty suspense question page to the Declare quantity page if the user has only 1 approval" in {
          navigator.nextPage(
            DeclareDutySuspenseQuestionPage,
            CheckMode,
            emptyUserAnswers
              .copy(regimes = AlcoholRegimes(Set(Beer)))
              .set(DeclareDutySuspenseQuestionPage, true)
              .success
              .value,
            Some(false)
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
          // TODO: update route when new page is created
        }

        "must go from the Declare duty suspense question page to the task list page if the answer is No" in {
          navigator.nextPage(
            DeclareDutySuspenseQuestionPage,
            CheckMode,
            emptyUserAnswers.set(DeclareDutySuspenseQuestionPage, false).success.value,
            Some(false)
          ) mustBe routes.TaskListController.onPageLoad
        }
      }

      "nextPageWithRegime" - {
        "must go from a page that doesn't exist in the route map to the Task List page" in {
          case object UnknownPage extends Page
          navigator.nextPageWithRegime(
            UnknownPage,
            CheckMode,
            emptyUserAnswers,
            regime
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
          // TODO: update route to task list when nextPageWithRegime is implemented properly
        }

        Seq(Beer, Cider, Wine, Spirits, OtherFermentedProduct).foreach { regime =>
          s"must go from the Declare quantity page to the CYA page when the regime is ${regime.entryName}" in {
            // TODO
          }
        }
      }
    }
  }
}
