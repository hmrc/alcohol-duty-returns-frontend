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

package viewmodels.checkAnswers.adjustment

import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.{RepackagedDraughtProducts, Spoilt}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.all.{SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits._

class AdjustmentDutyRateSummary {
  def row(adjustmentEntry: AdjustmentEntry)(implicit messages: Messages): Option[SummaryListRow] =
    if (
      adjustmentEntry.adjustmentType.contains(RepackagedDraughtProducts) ||
      adjustmentEntry.adjustmentType.contains(Spoilt) || adjustmentEntry.sprDutyRate.isDefined
    ) {
      None
    } else {
      Some(
        SummaryListRowViewModel(
          key = "tellUsAboutMultipleSPRRate.checkYourAnswersLabel.dutyRate.label",
          value = ValueViewModel(
            HtmlContent(
              adjustmentEntry.rate.getOrElse(
                throw new IllegalStateException("Adjustment duty rate is required but not found")
              ) match {
                case r => messages("site.currency.2DP", r)
              }
            )
          )
        )
      )
    }
}
