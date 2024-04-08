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

package models

import base.SpecBase
import generators.ModelGenerators
import models.RateType.{DraughtAndSmallProducerRelief, DraughtRelief, SmallProducerRelief}
import models.productEntry.ProductEntry
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ProductEntrySpec extends SpecBase with MockitoSugar with ScalaCheckPropertyChecks with ModelGenerators {

  "ProductEntry" - {

    "when taxType doesn't have a rate and spr relief is applied" in {
      val sprDutyRate  = Some(BigDecimal(1))
      val productEntry = ProductEntry(sprDutyRate = sprDutyRate)

      productEntry.rate shouldBe sprDutyRate
    }

    "when taxType doesn't have a rate and spr relief is not applied" in {
      val productEntry = ProductEntry()
      productEntry.rate shouldBe None
    }

    "when taxType has a rate and spr relief is not applied" in {
      val taxRate      = Some(BigDecimal(1))
      val productEntry = ProductEntry(taxRate = taxRate)

      productEntry.rate shouldBe taxRate
    }

    "when both spr relief is true and taxType has a rate" in {
      val taxRate      = Some(BigDecimal(1))
      val sprDutyRate  = Some(BigDecimal(1))
      val productEntry = ProductEntry(taxRate = taxRate, sprDutyRate = sprDutyRate)

      productEntry.rate shouldBe None
    }

    "isComplete" - {
      "returns true when all fields are defined" in {
        forAll(productEntryGen) { productEntry =>
          productEntry.isComplete shouldBe true
        }
      }

      "returns false when abv is not defined" in {
        forAll(productEntryGen) { productEntry =>
          val productEntryWithNoAbv = productEntry.copy(abv = None)
          productEntryWithNoAbv.isComplete shouldBe false
        }
      }

      "returns false when rateType is not defined" in {
        forAll(productEntryGen) { productEntry =>
          val productEntryWithNoRateType = productEntry.copy(rateType = None)
          productEntryWithNoRateType.isComplete shouldBe false
        }
      }

      "return false when volume is not defined" in {
        forAll(productEntryGen) { productEntry =>
          val productEntryWithNoVolume = productEntry.copy(volume = None)
          productEntryWithNoVolume.isComplete shouldBe false
        }
      }

      "returns false when draughtRelief is not defined and rateType is DraughtRelief" in {
        forAll(productEntryGen) { productEntry =>
          val productEntryWithNoDraughtRelief = productEntry.copy(
            rateType = Some(DraughtRelief),
            draughtRelief = None
          )
          productEntryWithNoDraughtRelief.isComplete shouldBe false
        }
      }

      "returns false when draughtRelief is not defined and rateType is DraughtAndSmallProducerRelief" in {
        forAll(productEntryGen) { productEntry =>
          val productEntryWithNoDraughtRelief = productEntry.copy(
            rateType = Some(DraughtAndSmallProducerRelief),
            draughtRelief = None
          )
          productEntryWithNoDraughtRelief.isComplete shouldBe false
        }
      }

      "returns false when smallProducerRelief is not defined and rateType is SmallProducerRelief" in {
        forAll(productEntryGen) { productEntry =>
          val productEntryWithNoDraughtRelief = productEntry.copy(
            rateType = Some(SmallProducerRelief),
            smallProducerRelief = None
          )
          productEntryWithNoDraughtRelief.isComplete shouldBe false
        }
      }

      "returns false when smallProducerRelief is not defined and rateType is DraughtAndSmallProducerRelief" in {
        forAll(productEntryGen) { productEntry =>
          val productEntryWithNoDraughtRelief = productEntry.copy(
            rateType = Some(DraughtAndSmallProducerRelief),
            smallProducerRelief = None
          )
          productEntryWithNoDraughtRelief.isComplete shouldBe false
        }
      }

      "returns false when taxCode is not defined" in {
        forAll(productEntryGen) { productEntry =>
          val productEntryWithNoDraughtRelief = productEntry.copy(taxCode = None)
          productEntryWithNoDraughtRelief.isComplete shouldBe false
        }
      }

      "returns false when regime is not defined" in {
        forAll(productEntryGen) { productEntry =>
          val productEntryWithNoDraughtRelief = productEntry.copy(regime = None)
          productEntryWithNoDraughtRelief.isComplete shouldBe false
        }
      }

      "returns false when taxRate and sprDutyRate are not defined" in {
        forAll(productEntryGen) { productEntry =>
          val productEntryWithNoDraughtRelief = productEntry.copy(taxRate = None, sprDutyRate = None)
          productEntryWithNoDraughtRelief.isComplete shouldBe false
        }
      }

      "returns false when duty is not defined" in {
        forAll(productEntryGen) { productEntry =>
          val productEntryWithNoDraughtRelief = productEntry.copy(duty = None)
          productEntryWithNoDraughtRelief.isComplete shouldBe false
        }
      }

      "returns false when pureAlcoholVolume is not defined" in {
        forAll(productEntryGen) { productEntry =>
          val productEntryWithNoDraughtRelief = productEntry.copy(pureAlcoholVolume = None)
          productEntryWithNoDraughtRelief.isComplete shouldBe false
        }
      }

      "returns false when any field is not defined" in {
        val productEntry = ProductEntry()
        productEntry.isComplete shouldBe false
      }
    }
  }

}
