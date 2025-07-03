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
import models.adjustment.AdjustmentType.{RepackagedDraughtProducts, Spoilt, Underdeclaration}
import play.api.i18n.Messages

class AdjustmentReturnPeriodViewModelSpec extends SpecBase {

  val url: String                 = appConfig.exciseEnquiriesUrl
  implicit val messages: Messages = getMessages(app)

  "AdjustmentReturnPeriodViewModel getHeading method must" - {
    "return the correct heading for an Under Declaration" in {
      val viewModel = AdjustmentReturnPeriodViewModel(Underdeclaration, url)

      viewModel.getHeading mustBe messages("adjustmentReturnPeriod.under-declaration.title")
    }
    "return the default heading for adjustment types that are not Spoilt or Under Declaration" in {
      val viewModel = AdjustmentReturnPeriodViewModel(RepackagedDraughtProducts, url)

      viewModel.getHeading mustBe messages("adjustmentReturnPeriod.default.title")
    }
    "throw an exception if called on Spoilt" in {
      val viewModel = AdjustmentReturnPeriodViewModel(Spoilt, url)

      assertThrows[IllegalArgumentException](viewModel.getHeading)
    }
  }

}
