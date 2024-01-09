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
import pages._
import models._

class ProductEntryNavigatorSpec extends SpecBase {

  val navigator = new ProductEntryNavigator

  "ProductEntryNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }

      "must go from the Alcohol to declare to Product Entry Guidance page if the answer is Yes" in {

        navigator.nextPage(
          pages.productEntry.DeclareAlcoholDutyQuestionPage,
          NormalMode,
          UserAnswers("id").set(pages.productEntry.DeclareAlcoholDutyQuestionPage, true).success.value
        ) mustBe controllers.productEntry.routes.ProductEntryGuidanceController.onPageLoad()
      }

      "must go from the Alcohol to declare to task list page if the answer is No" in {

        navigator.nextPage(
          pages.productEntry.DeclareAlcoholDutyQuestionPage,
          NormalMode,
          UserAnswers("id").set(pages.productEntry.DeclareAlcoholDutyQuestionPage, false).success.value
        ) mustBe routes.IndexController.onPageLoad
      }

      "must go from Product name page to Alcohol by volume page" in {

        navigator.nextPage(
          pages.productEntry.ProductNamePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.productEntry.routes.AlcoholByVolumeQuestionController.onPageLoad(NormalMode)
      }

      "must go from Alcohol by volume page to Draught relief question page" in {

        navigator.nextPage(
          pages.productEntry.AlcoholByVolumeQuestionPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.productEntry.routes.DraughtReliefQuestionController.onPageLoad(NormalMode)
      }

      "must go from the Draught relief question page to Small producer relief question page" in {

        navigator.nextPage(
          pages.productEntry.DraughtReliefQuestionPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.productEntry.routes.SmallProducerReliefQuestionController.onPageLoad(NormalMode)
      }

      "must go from the Declare SPR Duty Rate question page to the Product Volume page" in {

        navigator.nextPage(
          pages.productEntry.DeclareSmallProducerReliefDutyRatePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.productEntry.routes.ProductVolumeController.onPageLoad(NormalMode)
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
