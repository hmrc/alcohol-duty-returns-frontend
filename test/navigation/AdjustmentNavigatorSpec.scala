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
import models.{NormalMode, UserAnswers}
import pages.Page

class AdjustmentNavigatorSpec extends SpecBase {

  val navigator = new AdjustmentNavigator()

  "AdjustmentNavigator" - {

    "in Normal mode" - {

      "must go from Declare Adjustment Question Page to Adjustment Type Page" in {

        navigator.normalRoutes(pages.adjustment.DeclareAdjustmentQuestionPage)(emptyUserAnswers) mustBe
          controllers.adjustment.routes.AdjustmentTypeController.onPageLoad(NormalMode)
      }

      "must go from AdjustmentTypePage to WhenDidYouPayDutyController" in {

        navigator.normalRoutes(pages.adjustment.AdjustmentTypePage)(emptyUserAnswers) mustBe
          controllers.adjustment.routes.WhenDidYouPayDutyController.onPageLoad(NormalMode)
      }

      "must go from WhenDidYouPayDutyPage to AlcoholByVolumeController" in {

        navigator.normalRoutes(pages.adjustment.WhenDidYouPayDutyPage)(emptyUserAnswers) mustBe
          controllers.adjustment.routes.AlcoholByVolumeController.onPageLoad(NormalMode)
      }

      "must go to IndexController from any page not covered by normalRoutes" in {
        case object UnknownPage extends Page
        navigator.normalRoutes(UnknownPage)(emptyUserAnswers) mustBe routes.IndexController.onPageLoad
      }
    }

    "in Check mode" - {

      "must go to CheckYourAnswersController from any page" in {

        navigator.checkRouteMap(pages.adjustment.WhenDidYouPayDutyPage)(emptyUserAnswers) mustBe
          routes.CheckYourAnswersController.onPageLoad
      }
    }
  }

}
