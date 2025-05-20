/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.adjustment

import base.SpecBase
import models.adjustment.AdjustmentType.{Overdeclaration, RepackagedDraughtProducts, Underdeclaration}
import play.api.i18n.Messages

class AdjustmentTaxTypeViewModelSpec extends SpecBase {

  implicit val messages: Messages = getMessages(app)

  "AdjustmentTaxTypeViewModel" - {
    "getTitleContent method should" - {
      "return the correct title for a user on the repackaged draught journey" in {
        val viewModel = AdjustmentTaxTypeViewModel(RepackagedDraughtProducts)

        viewModel.getTitleContent mustBe messages("adjustmentTaxType.repackaged.title")
      }
      "return the title for adjustment types that are not on the repackaged draught journey" in {
        val viewModel = AdjustmentTaxTypeViewModel(Underdeclaration)

        viewModel.getTitleContent mustBe messages("adjustmentTaxType.title")
      }
    }

    "getLabel method should" - {
      "return the correct label for a user on the repackaged draught journey" in {
        val viewModel = AdjustmentTaxTypeViewModel(RepackagedDraughtProducts)

        viewModel.getLabel mustBe messages("adjustmentTaxType.repackaged.heading")
      }
      "return the default label for adjustment types that are not on the repackaged draught journey" in {
        val viewModel = AdjustmentTaxTypeViewModel(Underdeclaration)

        viewModel.getLabel mustBe messages("adjustmentTaxType.heading")
      }
    }

    "getHint method should" - {
      "return the correct hint for a user on the under declaration journey" in {
        val viewModel = AdjustmentTaxTypeViewModel(Underdeclaration)

        viewModel.getHint mustBe messages("adjustmentTaxType.underDeclaration.hint")
      }
      "return the default hint for adjustment types that are not on the under declaration journey" in {
        val viewModel = AdjustmentTaxTypeViewModel(Overdeclaration)

        viewModel.getHint mustBe messages("adjustmentTaxType.default.hint")
      }
    }
  }
}
