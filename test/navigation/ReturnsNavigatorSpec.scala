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

package navigation

import base.SpecBase
import controllers.routes
import models.NormalMode
import pages.Page
import pages.returns.DeclareAlcoholDutyQuestionPage

class ReturnsNavigatorSpec extends SpecBase {

  val navigator = new ReturnsNavigator

  "ReturnsNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers) mustBe routes.IndexController.onPageLoad
      }

      "must go from a page that doesn't exist in the route map to Index with regime" in {
        case object UnknownPage extends Page
        val regime = regimeGen.sample.value
        navigator.nextPageWithRegime(
          UnknownPage,
          NormalMode,
          emptyUserAnswers,
          regime
        ) mustBe routes.IndexController.onPageLoad
      }

      "must go from the Alcohol to declare to task list page if the answer is Yes" in {

        navigator.nextPage(
          DeclareAlcoholDutyQuestionPage,
          NormalMode,
          emptyUserAnswers.set(pages.returns.DeclareAlcoholDutyQuestionPage, true).success.value
        ) mustBe routes.TaskListController.onPageLoad
      }

      "must go from the Alcohol to declare to task list page if the answer is No" in {

        navigator.nextPage(
          pages.returns.DeclareAlcoholDutyQuestionPage,
          NormalMode,
          emptyUserAnswers.set(pages.returns.DeclareAlcoholDutyQuestionPage, false).success.value
        ) mustBe routes.TaskListController.onPageLoad
      }
    }
  }
}
