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
import generators.ModelGenerators
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class AdjustmentEntrySpec extends SpecBase with MockitoSugar with ScalaCheckPropertyChecks with ModelGenerators {

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
      val taxRate         = Some(BigDecimal(1))
      val adjustmentEntry = AdjustmentEntry(taxRate = taxRate)

      adjustmentEntry.rate shouldBe taxRate
    }

    "when both spr relief is true and taxType has a rate" in {
      val taxRate         = Some(BigDecimal(1))
      val sprDutyRate     = Some(BigDecimal(1))
      val adjustmentEntry = AdjustmentEntry(taxRate = taxRate, sprDutyRate = sprDutyRate)

      adjustmentEntry.rate shouldBe None
    }
  }

}
