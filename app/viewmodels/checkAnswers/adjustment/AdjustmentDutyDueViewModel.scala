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

import models.adjustment.AdjustmentType
import models.adjustment.AdjustmentType.RepackagedDraughtProducts
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import viewmodels.Money

case class AdjustmentDutyDueViewModel(
  adjustmentType: AdjustmentType,
  dutyToShow: BigDecimal,
  dutyDueInfo: Seq[Content]
)

class AdjustmentDutyDueViewModelFactory {
  def apply(
    adjustmentType: AdjustmentType,
    duty: BigDecimal,
    newDuty: BigDecimal,
    pureAlcoholVolume: BigDecimal,
    rate: BigDecimal,
    repackagedRate: BigDecimal,
    repackagedDuty: BigDecimal
  )(implicit messages: Messages): AdjustmentDutyDueViewModel =
    AdjustmentDutyDueViewModel(
      adjustmentType,
      dutyToShow = adjustmentType match {
        case RepackagedDraughtProducts => newDuty
        case _                         => duty
      },
      dutyDueInfo = if (adjustmentType == RepackagedDraughtProducts) {
        Seq(
          Text(messages("adjustmentDutyDue.repackaged.bulletList.1", Money.format(rate))),
          Text(messages("adjustmentDutyDue.bulletList.1", messages("site.4DP", pureAlcoholVolume))),
          Text(messages("adjustmentDutyDue.repackaged.bulletList.2", Money.format(duty))),
          Text(messages("adjustmentDutyDue.repackaged.bulletList.3", Money.format(duty))),
          Text(messages("adjustmentDutyDue.repackaged.bulletList.4", Money.format(repackagedRate))),
          Text(messages("adjustmentDutyDue.repackaged.bulletList.5", Money.format(repackagedDuty))),
          Text(messages("adjustmentDutyDue.repackaged.bulletList.6", Money.format(newDuty)))
        )
      } else {
        Seq(
          Text(messages("adjustmentDutyDue.bulletList.1", messages("site.4DP", pureAlcoholVolume))),
          Text(messages("adjustmentDutyDue.bulletList.2", Money.format(rate))),
          Text(messages("adjustmentDutyDue.bulletList.3", Money.format(duty)))
        )
      }
    )
}
