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
import models.adjustment.AdjustmentType.{Overdeclaration, RepackagedDraughtProducts, Spoilt}

class AdjustmentDutyDueModelSpec extends SpecBase {
  val dutyDue = BigDecimal(34.2)
  val newDuty = BigDecimal(1)
  "AdjustmentDutyDueModel" - {
    "must return a Duty amount of dutyDue when AdjustmentType is Overdeclaration" in {
      val dutyDueModel = new AdjustmentDutyDueViewModelFactory()(Overdeclaration, dutyDue, newDuty)

      dutyDueModel.dutyToShow mustBe dutyDue
    }

    "must return a Duty amount of dutyDue when AdjustmentType is Spoilt" in {
      val dutyDueModel = new AdjustmentDutyDueViewModelFactory()(Spoilt, dutyDue, newDuty)

      dutyDueModel.dutyToShow mustBe dutyDue
    }

    "must return a Duty amount of newDuty when AdjustmentType is RepackagedDraughtProducts" in {
      val dutyDueModel = new AdjustmentDutyDueViewModelFactory()(RepackagedDraughtProducts, dutyDue, newDuty)

      dutyDueModel.dutyToShow mustBe newDuty
    }
  }
}
