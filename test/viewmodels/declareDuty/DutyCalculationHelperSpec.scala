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
import viewmodels.TableRowActionViewModel

class DutyCalculationHelperSpec extends SpecBase {

  "dutyDueTableViewModel" - {
    val regime = Beer
    "must return a TableViewModel with correct rows when user answers are valid" in {
      val volumesAndRates =
        Seq(volumeAndRateByTaxType5, volumeAndRateByTaxType1, volumeAndRateByTaxType2, volumeAndRateByTaxType3)

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

      val expectedHeader =
        List("Description", "Litres of pure alcohol (LPA)", "Duty rate (per litre)", "Duty value", "Action")

      val expectedRows = List(
        List("Non-draught beer between 1% and 3% ABV (tax type code 123)", "4.1100", "£1.86", "£7.64"),
        List("Draught beer between 2% and 3% ABV (tax type code 124)", "2.5000", "£1.26", "£3.15"),
        List("Non-draught beer between 3% and 4% ABV (tax type code 125 SPR)", "3.5000", "£1.46", "£5.11"),
        List("Draught beer between 4% and 5% ABV (tax type code 126 SPR)", "4.5000", "£1.66", "£7.47")
      )

      val expectedActions = Seq(
        TableRowActionViewModel(
          label = "Change",
          href = controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime),
          visuallyHiddenText = Some("non-draught beer between 1% and 3% ABV (tax type code 123)")
        ),
        TableRowActionViewModel(
          label = "Change",
          href = controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime),
          visuallyHiddenText = Some("draught beer between 2% and 3% ABV (tax type code 124)")
        ),
        TableRowActionViewModel(
          label = "Change",
          href = controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime),
          visuallyHiddenText = Some("non-draught beer between 3% and 4% ABV (tax type code 125 SPR)")
        ),
        TableRowActionViewModel(
          label = "Change",
          href = controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime),
          visuallyHiddenText = Some("draught beer between 4% and 5% ABV (tax type code 126 SPR)")
        )
      )

      val result = DutyCalculationHelper.dutyDueTableViewModel(alcoholDuty, userAnswers, regime)(getMessages(app))

      result.isRight mustBe true

      val tableViewModel = result.getOrElse(fail("Expected Right(TableViewModel) but got Left"))

      tableViewModel.head.map(_.content.asHtml.toString)              mustBe expectedHeader
      tableViewModel.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedRows
      tableViewModel.rows.map(_.actions.head)                         mustBe expectedActions
    }

    "must return a Left with error message when no rate bands found" in {
      val alcoholDuty = AlcoholDuty(Seq.empty, BigDecimal(0))

      val result = DutyCalculationHelper.dutyDueTableViewModel(alcoholDuty, emptyUserAnswers, regime)(getMessages(app))

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

      val result = DutyCalculationHelper.dutyDueTableViewModel(alcoholDuty, userAnswers, regime)(getMessages(app))

      val errorMsg = result.left.getOrElse(fail("Expected Left(errorMessage) but got Right"))
      assert(errorMsg.startsWith("No rate band found for taxType:"))
    }
  }
}
