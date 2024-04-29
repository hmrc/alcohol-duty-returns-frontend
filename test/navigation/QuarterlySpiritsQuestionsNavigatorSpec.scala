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

class QuarterlySpiritsQuestionsNavigatorSpec extends SpecBase {

  val navigator = new QuarterlySpiritsQuestionsNavigator

  "QuarterlySpiritQuestionsNavigator" - {

    "in Normal mode" - {

      "must go from the Quarterly Spirits to declare to Spirits Total page if the answer is Yes" in {

        navigator.nextPage(
          pages.spiritsQuestions.DeclareQuarterlySpiritsPage,
          NormalMode,
          UserAnswers("id").set(pages.spiritsQuestions.DeclareQuarterlySpiritsPage, true).success.value
        ) mustBe controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode)
      }

      "must go from the Quarterly Spirits to declare to task list page if the answer is No" in {

        navigator.nextPage(
          pages.spiritsQuestions.DeclareQuarterlySpiritsPage,
          NormalMode,
          UserAnswers("id").set(pages.spiritsQuestions.DeclareQuarterlySpiritsPage, false).success.value
        ) mustBe routes.TaskListController.onPageLoad
      }

      "must go from the Quarterly Spirits to declare to task list page if there is an issue" in {

        navigator.nextPage(
          pages.spiritsQuestions.DeclareQuarterlySpiritsPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from the Declare Spirits Total page to Declare Scotch whisky page" in {

        navigator.nextPage(
          pages.spiritsQuestions.DeclareSpiritsTotalPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.spiritsQuestions.routes.WhiskyController.onPageLoad(NormalMode)
      }

      "must go from the Declare whiskey page to Spirit type page" in {

        navigator.nextPage(
          pages.spiritsQuestions.WhiskyPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.spiritsQuestions.routes.SpiritTypeController.onPageLoad(NormalMode)
      }

    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
