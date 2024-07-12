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

package models.adjustment

import base.SpecBase
import cats.data.NonEmptySeq
import generators.ModelGenerators
import models.adjustment.AdjustmentType.{RepackagedDraughtProducts, Spoilt}
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, RangeDetailsByRegime, RateBand, RateType}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.YearMonth

class AdjustmentEntrySpec extends SpecBase with MockitoSugar with ScalaCheckPropertyChecks with ModelGenerators {
  val rateBand        = RateBand(
    "310",
    "some band",
    RateType.DraughtRelief,
    Some(BigDecimal(10.99)),
    Set(
      RangeDetailsByRegime(
        AlcoholRegime.Beer,
        NonEmptySeq.one(
          ABVRange(
            AlcoholType.Beer,
            AlcoholByVolume(0.1),
            AlcoholByVolume(5.8)
          )
        )
      )
    )
  )
  val adjustmentEntry = AdjustmentEntry(
    adjustmentType = Some(Spoilt),
    period = Some(YearMonth.of(24, 1)),
    rateBand = Some(rateBand),
    pureAlcoholVolume = Some(BigDecimal(1)),
    totalLitresVolume = Some(BigDecimal(1)),
    duty = Some(BigDecimal(1))
  )
  "AdjustmentEntry" - {

    "when taxType doesn't have a rate and spr relief is applied" in {
      val sprDutyRate     = Some(BigDecimal(1))
      val adjustmentEntry = AdjustmentEntry(sprDutyRate = sprDutyRate)

      adjustmentEntry.rate shouldBe sprDutyRate
    }

    "when taxType doesn't have a rate and spr relief is not applied" in {
      val adjustmentEntry = AdjustmentEntry()
      adjustmentEntry.rate shouldBe None
    }

    "when taxType has a rate and spr relief is not applied" in {
      val taxRate         = Some(BigDecimal(10.99))
      val adjustmentEntry = AdjustmentEntry(rateBand = Some(rateBand))

      adjustmentEntry.rate shouldBe taxRate
    }

    "when both spr relief is true and taxType has a rate" in {
      val sprDutyRate     = Some(BigDecimal(1))
      val adjustmentEntry = AdjustmentEntry(rateBand = Some(rateBand), sprDutyRate = sprDutyRate)

      adjustmentEntry.rate shouldBe None
    }

    "when repackagedRateband has a rate and spr relief is not applied" in {
      val taxRate         = Some(BigDecimal(10.99))
      val adjustmentEntry = AdjustmentEntry(repackagedRateBand = Some(rateBand))

      adjustmentEntry.repackagedRate shouldBe taxRate
    }

    "when both repackagedSprRelief is true and repackagedRateband has a rate" in {
      val sprDutyRate     = Some(BigDecimal(1))
      val adjustmentEntry = AdjustmentEntry(repackagedRateBand = Some(rateBand), repackagedSprDutyRate = sprDutyRate)

      adjustmentEntry.repackagedRate shouldBe None
    }
    "when repackagedRateband doesn't have a rate and spr relief is applied" in {
      val repackagedSprDutyRate = Some(BigDecimal(1))
      val adjustmentEntry       = AdjustmentEntry(
        repackagedRateBand = Some(rateBand.copy(rate = None)),
        repackagedSprDutyRate = repackagedSprDutyRate
      )

      adjustmentEntry.repackagedRate shouldBe repackagedSprDutyRate
    }
    "isComplete should return true when all required fields are defined and valid" in {

      adjustmentEntry.isComplete shouldBe true
    }
    "isComplete should return false when adjustment type is not defined" in {
      val incompleteAdjustmentEntry = adjustmentEntry.copy(adjustmentType = None)

      incompleteAdjustmentEntry.isComplete shouldBe false
    }
    "isComplete should return true for spr case fields " in {
      val sprAdjustmentEntry = adjustmentEntry.copy(
        adjustmentType = Some(RepackagedDraughtProducts),
        rateBand = Some(rateBand.copy(rate = None)),
        sprDutyRate = Some(BigDecimal(1)),
        repackagedRateBand = Some(rateBand)
      )
      sprAdjustmentEntry.isComplete shouldBe true
    }
    "isComplete should return true when all repackaged fields are defined" in {
      val repackagedAdjustmentEntry = adjustmentEntry.copy(
        adjustmentType = Some(RepackagedDraughtProducts),
        rateBand = Some(rateBand),
        repackagedDuty = Some(BigDecimal(1))
      )

      repackagedAdjustmentEntry.isComplete shouldBe true
    }
    "isComplete should return true when all repackaged fields are defined for spr case" in {
      val repackagedAdjustmentEntry = adjustmentEntry.copy(
        adjustmentType = Some(RepackagedDraughtProducts),
        repackagedRateBand = Some(rateBand.copy(rate = None)),
        repackagedSprDutyRate = Some(BigDecimal(1)),
        repackagedDuty = Some(BigDecimal(1))
      )

      repackagedAdjustmentEntry.isComplete shouldBe true
    }
    /* "isComplete should return false when all repackaged fields are not defined for spr case" in {
      val repackagedAdjustmentEntry = adjustmentEntry.copy(
        adjustmentType = Some(RepackagedDraughtProducts),
        repackagedRateBand = Some(rateBand.copy(rate = None)),
        repackagedSprDutyRate = None,
        repackagedDuty = None
      )

      repackagedAdjustmentEntry.isComplete shouldBe false
    }
     */
  }

}
