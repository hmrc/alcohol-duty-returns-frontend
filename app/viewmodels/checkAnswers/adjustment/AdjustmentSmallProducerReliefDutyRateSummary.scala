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
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.Money
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

class AdjustmentSmallProducerReliefDutyRateSummary {
  def row(adjustmentEntry: AdjustmentEntry)(implicit messages: Messages): Option[SummaryListRow] =
    (adjustmentEntry.sprDutyRate, adjustmentEntry.repackagedSprDutyRate) match {
      case (_, Some(repackagedSprDutyRate)) =>
        Some(
          SummaryListRowViewModel(
            key = "adjustmentSmallProducerReliefDutyRate.checkYourAnswersLabel",
            value = ValueViewModel(Money.format(repackagedSprDutyRate)),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                routes.AdjustmentSmallProducerReliefDutyRateController.onPageLoad(CheckMode).url
              ).withVisuallyHiddenText(messages("adjustmentSmallProducerReliefDutyRate.change.hidden"))
            )
          )
        )
      case (Some(sprDutyRate), None)        =>
        Some(
          SummaryListRowViewModel(
            key = "adjustmentSmallProducerReliefDutyRate.checkYourAnswersLabel",
            value = ValueViewModel(Money.format(sprDutyRate)),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                routes.AdjustmentVolumeWithSPRController.onPageLoad(CheckMode).url
              ).withVisuallyHiddenText(messages("adjustmentSmallProducerReliefDutyRate.change.hidden"))
            )
          )
        )
      case _                                =>
        None
    }
}
