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
import pages.spiritsQuestions.DeclareQuarterlySpiritsPage

class QuarterlySpiritsQuestionsNavigatorSpec extends SpecBase {

  val navigator = new QuarterlySpiritsQuestionsNavigator

  "QuarterlySpiritQuestionsNavigator" - {
    "in Normal mode" - {
      "must go from a page that doesn't exist in the route map to the Task List page" in {
        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          NormalMode,
          emptyUserAnswers,
          Some(false)
        ) mustBe routes.TaskListController.onPageLoad
      }

      "the Declare Quarterly Spirits page" - {
        "must go to the Declare Spirits Total page if the answer is Yes" in {
          navigator.nextPage(
            pages.spiritsQuestions.DeclareQuarterlySpiritsPage,
            NormalMode,
            emptyUserAnswers.set(pages.spiritsQuestions.DeclareQuarterlySpiritsPage, true).success.value,
            Some(false)
          ) mustBe controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode)
        }

        "must to the Task List page if the answer is No" in {
          navigator.nextPage(
            pages.spiritsQuestions.DeclareQuarterlySpiritsPage,
            NormalMode,
            emptyUserAnswers.set(pages.spiritsQuestions.DeclareQuarterlySpiritsPage, false).success.value,
            Some(false)
          ) mustBe routes.TaskListController.onPageLoad
        }

        "must go to the Journey Recovery page if there is an issue" in {
          navigator.nextPage(
            pages.spiritsQuestions.DeclareQuarterlySpiritsPage,
            NormalMode,
            emptyUserAnswers,
            Some(false)
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "the Declare Spirits Total page" - {
        "must go to the Declare Whisk(e)y page" in {
          navigator.nextPage(
            pages.spiritsQuestions.DeclareSpiritsTotalPage,
            NormalMode,
            emptyUserAnswers,
            Some(false)
          ) mustBe controllers.spiritsQuestions.routes.WhiskyController.onPageLoad(NormalMode)
        }
      }

      "the Declare Whisk(e)y page" - {
        "must go to the Spirit Type page" in {
          navigator.nextPage(
            pages.spiritsQuestions.WhiskyPage,
            NormalMode,
            emptyUserAnswers,
            Some(false)
          ) mustBe controllers.spiritsQuestions.routes.SpiritTypeController.onPageLoad(NormalMode)
        }
      }

      "the Spirit Type page" - {
        "must go to the Other Spirit Types page if Other spirits is checked" in {
          navigator.nextPage(
            pages.spiritsQuestions.SpiritTypePage,
            NormalMode,
            emptyUserAnswers
              .set(pages.spiritsQuestions.SpiritTypePage, Set[SpiritType](SpiritType.Maltspirits, SpiritType.Other))
              .success
              .value,
            Some(false)
          ) mustBe controllers.spiritsQuestions.routes.OtherSpiritsProducedController.onPageLoad(NormalMode)
        }

        "must to the CYA page if the answer if Other spirits is not checked" in {
          navigator.nextPage(
            pages.spiritsQuestions.SpiritTypePage,
            NormalMode,
            emptyUserAnswers
              .set(
                pages.spiritsQuestions.SpiritTypePage,
                Set[SpiritType](SpiritType.Maltspirits, SpiritType.Grainspirits)
              )
              .success
              .value,
            Some(false)
          ) mustBe controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad()
        }

        "must go to the Journey Recovery page if there is an issue" in {
          navigator.nextPage(
            pages.spiritsQuestions.SpiritTypePage,
            NormalMode,
            emptyUserAnswers,
            Some(false)
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }
    }

    "in Check mode" - {
      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          emptyUserAnswers,
          Some(false)
        ) mustBe controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from the Spirit Type page to the Other Spirit Types page if the answer has changed" in {
        navigator.nextPage(
          pages.spiritsQuestions.SpiritTypePage,
          CheckMode,
          emptyUserAnswers,
          Some(true)
        ) mustBe controllers.spiritsQuestions.routes.OtherSpiritsProducedController.onPageLoad(CheckMode)
      }

      "must go from the Spirit Type page to the Check your answers page if the answer has not changed" in {
        navigator.nextPage(
          pages.spiritsQuestions.SpiritTypePage,
          CheckMode,
          emptyUserAnswers,
          Some(false)
        ) mustBe controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from the Other Spirit Types page to the Check your answers page" in {
        navigator.nextPage(
          pages.spiritsQuestions.OtherSpiritsProducedPage,
          CheckMode,
          emptyUserAnswers,
          Some(false)
        ) mustBe controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from the Whisk(e) page to the Check your answers page" in {
        navigator.nextPage(
          pages.spiritsQuestions.WhiskyPage,
          CheckMode,
          emptyUserAnswers,
          Some(false)
        ) mustBe controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from the Total Spirits page to the Check your answers page" in {
        navigator.nextPage(
          pages.spiritsQuestions.DeclareSpiritsTotalPage,
          CheckMode,
          emptyUserAnswers,
          Some(false)
        ) mustBe controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from the Declare Quarterly Spirits (DQS) page to the task list when No is selected on DQS" in {
        navigator.nextPage(
          pages.spiritsQuestions.DeclareQuarterlySpiritsPage,
          CheckMode,
          emptyUserAnswers.set(DeclareQuarterlySpiritsPage, false).success.value,
          Some(false)
        ) mustBe routes.TaskListController.onPageLoad
      }

      "must go from the Declare Quarterly Spirits (DQS) page to the Declare Spirits Total page when Yes is selected on DQS" in {
        navigator.nextPage(
          pages.spiritsQuestions.DeclareQuarterlySpiritsPage,
          CheckMode,
          emptyUserAnswers.set(DeclareQuarterlySpiritsPage, true).success.value,
          Some(false)
        ) mustBe controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode)
      }

      "must go from the Declare Quarterly Spirits (DQS) page to the Journey Recovery page when there is no answer set in mongo" in {
        navigator.nextPage(
          pages.spiritsQuestions.DeclareQuarterlySpiritsPage,
          CheckMode,
          emptyUserAnswers,
          Some(false)
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }
    }
  }
}
