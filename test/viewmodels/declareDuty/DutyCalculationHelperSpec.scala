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

package viewmodels.declareDuty

import base.SpecBase
import models.declareDuty.{AlcoholDuty, DutyByTaxType}
import models.AlcoholRegime.Beer
import pages.declareDuty.WhatDoYouNeedToDeclarePage

class DutyCalculationHelperSpec extends SpecBase {

  "dutyDueTableViewModel" - {
    val regime = Beer
    "must return a TableViewModel with correct rows when user answers are valid" in {

      val volumesAndRates = arbitraryVolumeAndRateByTaxType(
        allRateBands.toSeq
      ).arbitrary.sample.value

      val dutiesByTaxType = volumesAndRates.map { volumeAndRate =>
        val totalDuty = volumeAndRate.dutyRate * volumeAndRate.pureAlcohol
        DutyByTaxType(
          taxType = volumeAndRate.taxType,
          totalLitres = volumeAndRate.totalLitres,
          pureAlcohol = volumeAndRate.pureAlcohol,
          dutyRate = volumeAndRate.dutyRate,
          dutyDue = totalDuty
        )
      }

      val alcoholDuty = AlcoholDuty(
        dutiesByTaxType = dutiesByTaxType,
        totalDuty = dutiesByTaxType.map(_.dutyDue).sum
      )

      val userAnswers = emptyUserAnswers
        .setByKey(WhatDoYouNeedToDeclarePage, regime, allRateBands)
        .success
        .value

      val result = new DutyCalculationHelper().dutyDueTableViewModel(alcoholDuty, userAnswers, regime)(getMessages(app))

      result.isRight mustBe true

      val tableViewModel = result.getOrElse(fail("Expected Right(TableViewModel) but got Left"))
      tableViewModel.head.size mustBe 5
      tableViewModel.rows.size mustBe allRateBands.size
    }

    "must return a Left with error message when no rate bands found" in {
      val alcoholDuty = AlcoholDuty(Seq.empty, BigDecimal(0))

      val result =
        new DutyCalculationHelper().dutyDueTableViewModel(alcoholDuty, emptyUserAnswers, regime)(getMessages(app))

      result mustBe Left("No rate bands found")
    }

    "must return a Left with error message when no matching rate band for tax type" in {
      val unmatchedRateBand = genSingleUnmatchedRateBandForRegimeWithSPR(regime).sample.value
      val unmatchedTaxType  = genVolumeAndRateByTaxTypeRateBand(unmatchedRateBand).arbitrary.sample.value
      val rateBands         = genListOfRateBandForRegimeWithSPR(regime).sample.value.toSet
      val totalDuty         = unmatchedTaxType.dutyRate * unmatchedTaxType.pureAlcohol
      val dutyByTaxType     = Seq(
        DutyByTaxType(
          taxType = unmatchedTaxType.taxType,
          totalLitres = unmatchedTaxType.totalLitres,
          pureAlcohol = unmatchedTaxType.pureAlcohol,
          dutyRate = unmatchedTaxType.dutyRate,
          dutyDue = totalDuty
        )
      )

      val userAnswers = emptyUserAnswers
        .setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands)
        .success
        .value

      val alcoholDuty = AlcoholDuty(dutyByTaxType, totalDuty)

      val result = new DutyCalculationHelper().dutyDueTableViewModel(alcoholDuty, userAnswers, regime)(getMessages(app))

      val errorMsg = result.left.getOrElse(fail("Expected Left(errorMessage) but got Right"))
      assert(errorMsg.startsWith("No rate band found for taxType:"))
    }
  }
}
