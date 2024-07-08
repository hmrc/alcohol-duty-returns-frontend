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
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}

class AlcoholRegimesSpec extends SpecBase {
  "AlcoholRegimes" - {
    "return true, false otherwise" - {
      "if the regimes contains Beer when hasBeer is called" in new SetUp {
        allRegimes.hasBeer() mustBe true
        allRegimesExceptBeer.hasBeer() mustBe false
      }

      "if the regimes contains Cider when hasCider is called" in new SetUp {
        allRegimes.hasCider() mustBe true
        allRegimesExceptCider.hasCider() mustBe false
      }

      "if the regimes contains Wine when hasWine is called" in new SetUp {
        allRegimes.hasWine() mustBe true
        allRegimesExceptWine.hasWine() mustBe false
      }

      "if the regimes contains Spirits when hasSpirits is called" in new SetUp {
        allRegimes.hasSpirits() mustBe true
        allRegimesExceptSpirits.hasSpirits() mustBe false
      }

      "if the regimes contains OtherFermentedProduct when hasOtherFermentedProduct is called" in new SetUp {
        allRegimes.hasOtherFermentedProduct() mustBe true
        allRegimesExceptCiderWineOtherFermented.hasOtherFermentedProduct() mustBe false
      }
    }

    "return the regimes in view order" - {
      "when all regimes are present" in new SetUp {
        allRegimes.regimesInViewOrder() mustBe Seq(Beer, Cider, Wine, Spirits, OtherFermentedProduct)
      }

      "when all regimes are present except Beer" in new SetUp {
        allRegimesExceptBeer.regimesInViewOrder() mustBe Seq(Cider, Wine, Spirits, OtherFermentedProduct)
      }

      "when all regimes are present except Cider and Wine" in new SetUp {
        allRegimesExceptCiderWineOtherFermented.regimesInViewOrder() mustBe Seq(Beer, Spirits)
      }
    }

    Seq(
      (None, Some(Beer)),
      (Some(Beer), Some(Cider)),
      (Some(Cider), Some(Wine)),
      (Some(Wine), Some(Spirits)),
      (Some(Spirits), Some(OtherFermentedProduct)),
      (Some(OtherFermentedProduct), None)
    ).foreach { case (current, expectedNext) =>
      s"find the next view regime when all regimes are present and the current regime is ${current.map(_.entryName).getOrElse("None")}" in new SetUp {
        allRegimes.nextViewRegime(current) mustBe expectedNext
      }
    }

    Seq(
      (None, Some(Cider)),
      (Some(Cider), Some(Wine)),
      (Some(Wine), Some(Spirits)),
      (Some(Spirits), Some(OtherFermentedProduct)),
      (Some(OtherFermentedProduct), None)
    ).foreach { case (current, expectedNext) =>
      s"find the next view regime when all regimes are present except Beer and the current regime is ${current.map(_.entryName).getOrElse("None")}" in new SetUp {
        allRegimesExceptBeer.nextViewRegime(current) mustBe expectedNext
      }
    }

    Seq((None, Some(Beer)), (Some(Beer), Some(Spirits)), (Some(Spirits), None)).foreach {
      case (current, expectedNext) =>
        s"find the next view regime when all regimes are present except Cider and Wine and the current regime is ${current
          .map(_.entryName)
          .getOrElse("None")}" in new SetUp {
          allRegimesExceptCiderWineOtherFermented.nextViewRegime(current) mustBe expectedNext
        }
    }
  }

  class SetUp {
    val allRegimes                              = AlcoholRegimes(Set(Beer, Cider, Wine, Spirits, OtherFermentedProduct))
    val allRegimesExceptBeer                    = AlcoholRegimes(Set(Cider, Wine, Spirits, OtherFermentedProduct))
    val allRegimesExceptCider                   = AlcoholRegimes(Set(Beer, Wine, Spirits, OtherFermentedProduct))
    val allRegimesExceptWine                    = AlcoholRegimes(Set(Beer, Cider, Spirits, OtherFermentedProduct))
    val allRegimesExceptSpirits                 = AlcoholRegimes(Set(Beer, Cider, Wine, OtherFermentedProduct))
    val allRegimesExceptCiderWineOtherFermented = AlcoholRegimes(Set(Beer, Spirits))
  }
}
