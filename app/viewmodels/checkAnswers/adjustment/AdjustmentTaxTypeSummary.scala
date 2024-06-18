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

package viewmodels.checkAnswers.adjustment

import controllers.adjustment.routes
import models.adjustment.AdjustmentEntry
import models.CheckMode
import models.adjustment.AdjustmentType.RepackagedDraughtProducts
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.adjustment.AdjustmentTypeHelper.getAdjustmentTypeValue
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AdjustmentTaxTypeSummary {

  def row(adjustmentEntry: AdjustmentEntry)(implicit messages: Messages): Option[SummaryListRow] = {
    val adjustmentType = getAdjustmentTypeValue(adjustmentEntry)
    val label          = if (adjustmentType.equals(RepackagedDraughtProducts.toString)) {
      "adjustmentTaxType.repackaged.checkYourAnswersLabel"
    } else { "adjustmentTaxType.checkYourAnswersLabel" }
    adjustmentEntry.rateBand.map { rateBand =>
      SummaryListRowViewModel(
        key = label,
        value = ValueViewModel(rateBand.taxType),
        actions = Seq(
          ActionItemViewModel("site.change", routes.AdjustmentTaxTypeController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("adjustmentTaxType.change.hidden"))
        )
      )
    }
  }
}
