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

package viewmodels

import base.SpecBase
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.{AlcoholRegime, AlcoholRegimes}

class AlcoholRegimesViewOrderSpec extends SpecBase {
  "AlcoholRegimesViewOrder" - {
    "must cover all regimes" in new SetUp {
      implicit val ord: Ordering[AlcoholRegime] = (x: AlcoholRegime, y: AlcoholRegime) => x.hashCode() - y.hashCode()

      AlcoholRegimesViewOrder.viewOrder.sorted mustBe AlcoholRegime.values.sorted
    }

    "return the regimes in view order" - {
      "when all regimes are present" in new SetUp {
        AlcoholRegimesViewOrder
          .regimesInViewOrder(allRegimes) mustBe Seq(Beer, Cider, Wine, Spirits, OtherFermentedProduct)
      }

      "when all regimes are present except Beer" in new SetUp {
        AlcoholRegimesViewOrder
          .regimesInViewOrder(allRegimesExceptBeer) mustBe Seq(Cider, Wine, Spirits, OtherFermentedProduct)
      }

      "when all regimes are present except Cider and Wine" in new SetUp {
        AlcoholRegimesViewOrder.regimesInViewOrder(allRegimesExceptCiderWineOtherFermented) mustBe Seq(Beer, Spirits)
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
        AlcoholRegimesViewOrder.nextViewRegime(allRegimes, current) mustBe expectedNext
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
        AlcoholRegimesViewOrder.nextViewRegime(allRegimesExceptBeer, current) mustBe expectedNext
      }
    }

    Seq((None, Some(Beer)), (Some(Beer), Some(Spirits)), (Some(Spirits), None)).foreach {
      case (current, expectedNext) =>
        s"find the next view regime when all regimes are present except Cider and Wine and the current regime is ${current
          .map(_.entryName)
          .getOrElse("None")}" in new SetUp {
          AlcoholRegimesViewOrder.nextViewRegime(allRegimesExceptCiderWineOtherFermented, current) mustBe expectedNext
        }
    }
  }

  class SetUp {
    val allRegimes                              = AlcoholRegimes(Set(Beer, Cider, Wine, Spirits, OtherFermentedProduct))
    val allRegimesExceptBeer                    = AlcoholRegimes(Set(Cider, Wine, Spirits, OtherFermentedProduct))
    val allRegimesExceptCiderWineOtherFermented = AlcoholRegimes(Set(Beer, Spirits))
  }
}
