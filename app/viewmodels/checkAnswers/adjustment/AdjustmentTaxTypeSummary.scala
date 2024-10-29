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
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import viewmodels.declareDuty.RateBandHelper.rateBandRecap

object AdjustmentTaxTypeSummary {

  def row(adjustmentEntry: AdjustmentEntry)(implicit messages: Messages): Option[SummaryListRow] = {
    val adjustmentType      = adjustmentEntry.adjustmentType.getOrElse(
      throw new RuntimeException("Couldn't fetch adjustment type value from user answers")
    )
    val (label, hiddenText) = if (adjustmentType.equals(RepackagedDraughtProducts)) {
      ("adjustmentTaxType.repackaged.checkYourAnswersLabel", "adjustmentTaxType.repackaged.change.hidden")
    } else {
      ("adjustmentTaxType.checkYourAnswersLabel", "adjustmentTaxType.change.hidden")
    }
    adjustmentEntry.rateBand.map { rateBand =>
      SummaryListRowViewModel(
        key = label,
        value = ValueViewModel(rateBandRecap(rateBand)),
        actions = Seq(
          ActionItemViewModel("site.change", routes.AdjustmentTaxTypeController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages(hiddenText))
        )
      )
    }
  }
}
