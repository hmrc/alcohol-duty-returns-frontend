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

import viewmodels.checkAnswers.adjustment.AdjustmentDutyDueViewModelFactory
import base.SpecBase
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.{Overdeclaration, RepackagedDraughtProducts, Spoilt}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import viewmodels.Money

class AdjustmentDutyDueModelSpec extends SpecBase {

  "AdjustmentDutyDueModel" - {

    val dutyDue           = BigDecimal(34.2)
    val rate              = BigDecimal(9.27)
    val pureAlcoholVolume = BigDecimal(3.69)
    val repackagedRate    = BigDecimal(10)
    val repackagedDuty    = BigDecimal(33.2)
    val newDuty           = BigDecimal(1)

    "must return a Duty amount of dutyDue when AdjustmentType is Overdeclaration" in new SetUp() {
      val dutyDueModel = new AdjustmentDutyDueViewModelFactory()(
        Overdeclaration,
        dutyDue,
        newDuty,
        pureAlcoholVolume,
        rate,
        repackagedRate,
        repackagedDuty
      )

      dutyDueModel.dutyToShow mustBe dutyDue
    }

    "must return a Duty amount of dutyDue when AdjustmentType is Spoilt" in new SetUp() {
      val dutyDueModel = new AdjustmentDutyDueViewModelFactory()(
        Spoilt,
        dutyDue,
        newDuty,
        pureAlcoholVolume,
        rate,
        repackagedRate,
        repackagedDuty
      )

      dutyDueModel.dutyToShow mustBe dutyDue
    }

    "must return a Duty amount of newDuty when AdjustmentType is RepackagedDraughtProducts" in new SetUp() {
      val dutyDueModel = new AdjustmentDutyDueViewModelFactory()(
        RepackagedDraughtProducts,
        dutyDue,
        newDuty,
        pureAlcoholVolume,
        rate,
        repackagedRate,
        repackagedDuty
      )

      dutyDueModel.dutyToShow mustBe newDuty
    }

    "if adjustmentType is RepackagedDraughtProducts show correct values in list" in new SetUp() {
      val dutyDueModel = new AdjustmentDutyDueViewModelFactory()(
        RepackagedDraughtProducts,
        dutyDue,
        newDuty,
        pureAlcoholVolume,
        rate,
        repackagedRate,
        repackagedDuty
      )

      dutyDueModel.dutyDueInfo mustBe
        Seq(
          Text(messages("adjustmentDutyDue.repackaged.bulletList.1", Money.format(rate))),
          Text(messages("adjustmentDutyDue.bulletList.1", messages("site.4DP", pureAlcoholVolume))),
          Text(messages("adjustmentDutyDue.repackaged.bulletList.2", Money.format(dutyDue))),
          Text(messages("adjustmentDutyDue.repackaged.bulletList.3", Money.format(dutyDue))),
          Text(messages("adjustmentDutyDue.repackaged.bulletList.4", Money.format(repackagedRate))),
          Text(messages("adjustmentDutyDue.repackaged.bulletList.5", Money.format(repackagedDuty))),
          Text(messages("adjustmentDutyDue.repackaged.bulletList.6", Money.format(newDuty)))
        )
    }

    "if adjustmentType is Overdeclaration show correct values in list" in new SetUp() {
      val dutyDueModel = new AdjustmentDutyDueViewModelFactory()(
        Overdeclaration,
        dutyDue,
        newDuty,
        pureAlcoholVolume,
        rate,
        repackagedRate,
        repackagedDuty
      )

      dutyDueModel.dutyDueInfo mustBe
        Seq(
          Text(messages("adjustmentDutyDue.bulletList.1", messages("site.4DP", pureAlcoholVolume))),
          Text(messages("adjustmentDutyDue.bulletList.2", Money.format(rate))),
          Text(messages("adjustmentDutyDue.bulletList.3", Money.format(dutyDue)))
        )
    }

    "if adjustmentType is Spoilt show correct values in list" in new SetUp() {
      val dutyDueModel = new AdjustmentDutyDueViewModelFactory()(
        Spoilt,
        dutyDue,
        newDuty,
        pureAlcoholVolume,
        rate,
        repackagedRate,
        repackagedDuty
      )

      dutyDueModel.dutyDueInfo mustBe
        Seq(
          Text(messages("adjustmentDutyDue.bulletList.1", messages("site.4DP", pureAlcoholVolume))),
          Text(messages("adjustmentDutyDue.bulletList.2", Money.format(rate))),
          Text(messages("adjustmentDutyDue.bulletList.3", Money.format(dutyDue)))
        )
    }
  }

  class SetUp() {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)
  }
}
