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
import controllers._
import pages._
import models._
import models.adjustment.AdjustmentEntry

class AdjustmentNavigatorSpec extends SpecBase {

  val navigator = new AdjustmentNavigator

  "AdjustmentNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }

      "must go from the Adjustment Question Page to Adjustment Type page if the answer is Yes" in {

        navigator.nextPage(
          pages.adjustment.DeclareAdjustmentQuestionPage,
          NormalMode,
          UserAnswers("id").set(pages.adjustment.DeclareAdjustmentQuestionPage, true).success.value
        ) mustBe controllers.adjustment.routes.AdjustmentTypeController.onPageLoad(NormalMode)
      }

      "must go from the Alcohol By Volume Page to Adjustment Tax Type page" in {

        navigator.nextPage(
          pages.adjustment.AlcoholByVolumePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.adjustment.routes.AdjustmentTaxTypeController.onPageLoad(NormalMode)
      }
    }
    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.CheckYourAnswersController.onPageLoad
      }

    }
  }

}
