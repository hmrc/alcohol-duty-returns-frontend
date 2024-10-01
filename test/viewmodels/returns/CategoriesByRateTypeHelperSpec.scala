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

package viewmodels.returns

import base.SpecBase
import cats.data.NonEmptySeq
import models.AlcoholRegime.Beer
import models.{ABVRange, AlcoholByVolume, AlcoholType, RangeDetailsByRegime, RateBand, RateType}
import play.api.i18n.Messages

import scala.collection.immutable.SortedSet

class CategoriesByRateTypeHelperSpec extends SpecBase {
  "CategoriesByRateTypeHelper" - {
    "should return an empty view model when no ratebands are passed" in new SetUp {
      CategoriesByRateTypeHelper
        .rateBandCategories(Set.empty) mustBe CategoriesByRateTypeViewModel(Seq.empty, Seq.empty, Seq.empty, Seq.empty)
    }

    "should return rate band description when all ratebands are passed" in new SetUp {
      CategoriesByRateTypeHelper.rateBandCategories(allRateBands) mustBe CategoriesByRateTypeViewModel(
        Seq(CategoryViewModel("Beer between 1% and 2% ABV (tax type code 123)", "123")),
        Seq(CategoryViewModel("Beer between 2% and 3% ABV (tax type code 124)", "124")),
        Seq(CategoryViewModel("Beer between 3% and 4% ABV (tax type code 125)", "125")),
        Seq(CategoryViewModel("Beer between 4% and 5% ABV (tax type code 126)", "126"))
      )
    }

    "should return rate band description when all ratebands are passed and recap" in new SetUp {
      CategoriesByRateTypeHelper.rateBandCategories(allRateBands, isRecap = true) mustBe CategoriesByRateTypeViewModel(
        Seq(CategoryViewModel("Non-draught beer between 1% and 2% ABV (123)", "123")),
        Seq(CategoryViewModel("Draught beer between 2% and 3% ABV (124)", "124")),
        Seq(CategoryViewModel("Non-draught beer between 3% and 4% ABV (125 SPR)", "125")),
        Seq(CategoryViewModel("Draught beer between 4% and 5% ABV (126 SPR)", "126"))
      )
    }

    "should return rate band description when all ratebands except core are passed" in new SetUp {
      CategoriesByRateTypeHelper.rateBandCategories(allRateBands - coreRateBand) mustBe CategoriesByRateTypeViewModel(
        Seq.empty,
        Seq(CategoryViewModel("Beer between 2% and 3% ABV (tax type code 124)", "124")),
        Seq(CategoryViewModel("Beer between 3% and 4% ABV (tax type code 125)", "125")),
        Seq(CategoryViewModel("Beer between 4% and 5% ABV (tax type code 126)", "126"))
      )
    }

    "should return rate band description when all ratebands except draught relief are passed" in new SetUp {
      CategoriesByRateTypeHelper.rateBandCategories(
        allRateBands - draughtReliefRateBand
      ) mustBe CategoriesByRateTypeViewModel(
        Seq(CategoryViewModel("Beer between 1% and 2% ABV (tax type code 123)", "123")),
        Seq.empty,
        Seq(CategoryViewModel("Beer between 3% and 4% ABV (tax type code 125)", "125")),
        Seq(CategoryViewModel("Beer between 4% and 5% ABV (tax type code 126)", "126"))
      )
    }

    "should return rate band description when all ratebands except small producer relief are passed" in new SetUp {
      CategoriesByRateTypeHelper.rateBandCategories(
        allRateBands - smallProducerReliefRateBand
      ) mustBe CategoriesByRateTypeViewModel(
        Seq(CategoryViewModel("Beer between 1% and 2% ABV (tax type code 123)", "123")),
        Seq(CategoryViewModel("Beer between 2% and 3% ABV (tax type code 124)", "124")),
        Seq.empty,
        Seq(CategoryViewModel("Beer between 4% and 5% ABV (tax type code 126)", "126"))
      )
    }

    "should return rate band description when all ratebands except draught and small producer relief are passed" in new SetUp {
      CategoriesByRateTypeHelper.rateBandCategories(
        allRateBands - draughtAndSmallProducerReliefRateBand
      ) mustBe CategoriesByRateTypeViewModel(
        Seq(CategoryViewModel("Beer between 1% and 2% ABV (tax type code 123)", "123")),
        Seq(CategoryViewModel("Beer between 2% and 3% ABV (tax type code 124)", "124")),
        Seq(CategoryViewModel("Beer between 3% and 4% ABV (tax type code 125)", "125")),
        Seq.empty
      )
    }

    "should group types and sort by tax code" in new SetUp {
      CategoriesByRateTypeHelper.rateBandCategories(threeCoreRandBandsReversed) mustBe CategoriesByRateTypeViewModel(
        Seq(
          CategoryViewModel("Beer between 1% and 2% ABV (tax type code 123)", "123"),
          CategoryViewModel("Beer between 1% and 2% ABV (tax type code 124)", "124"),
          CategoryViewModel("Beer between 1% and 2% ABV (tax type code 125)", "125")
        ),
        Seq.empty,
        Seq.empty,
        Seq.empty
      )
    }
  }

  class SetUp {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val coreRateBand                          = RateBand(
      "123",
      "core description",
      RateType.Core,
      Some(BigDecimal(1.1)),
      Set(
        RangeDetailsByRegime(
          Beer,
          NonEmptySeq.one(ABVRange(AlcoholType.Beer, AlcoholByVolume(BigDecimal(1)), AlcoholByVolume(BigDecimal(2))))
        )
      )
    )
    val coreRateBand2                         = RateBand(
      "124",
      "core description",
      RateType.Core,
      Some(BigDecimal(1.1)),
      Set(
        RangeDetailsByRegime(
          Beer,
          NonEmptySeq.one(ABVRange(AlcoholType.Beer, AlcoholByVolume(BigDecimal(1)), AlcoholByVolume(BigDecimal(2))))
        )
      )
    )
    val coreRateBand3                         = RateBand(
      "125",
      "core description",
      RateType.Core,
      Some(BigDecimal(1.1)),
      Set(
        RangeDetailsByRegime(
          Beer,
          NonEmptySeq.one(ABVRange(AlcoholType.Beer, AlcoholByVolume(BigDecimal(1)), AlcoholByVolume(BigDecimal(2))))
        )
      )
    )
    val draughtReliefRateBand                 = RateBand(
      "124",
      "draught relief description",
      RateType.DraughtRelief,
      Some(BigDecimal(2.1)),
      Set(
        RangeDetailsByRegime(
          Beer,
          NonEmptySeq.one(ABVRange(AlcoholType.Beer, AlcoholByVolume(BigDecimal(2)), AlcoholByVolume(BigDecimal(3))))
        )
      )
    )
    val smallProducerReliefRateBand           = RateBand(
      "125",
      "small producer relief description",
      RateType.SmallProducerRelief,
      Some(BigDecimal(3.1)),
      Set(
        RangeDetailsByRegime(
          Beer,
          NonEmptySeq.one(ABVRange(AlcoholType.Beer, AlcoholByVolume(BigDecimal(3)), AlcoholByVolume(BigDecimal(4))))
        )
      )
    )
    val draughtAndSmallProducerReliefRateBand = RateBand(
      "126",
      "draught and small producer relief description",
      RateType.DraughtAndSmallProducerRelief,
      Some(BigDecimal(4.1)),
      Set(
        RangeDetailsByRegime(
          Beer,
          NonEmptySeq.one(ABVRange(AlcoholType.Beer, AlcoholByVolume(BigDecimal(4)), AlcoholByVolume(BigDecimal(5))))
        )
      )
    )

    val allRateBands               =
      Set(coreRateBand, draughtReliefRateBand, smallProducerReliefRateBand, draughtAndSmallProducerReliefRateBand)
    val threeCoreRandBandsReversed = SortedSet(coreRateBand, coreRateBand2, coreRateBand3)((x: RateBand, y: RateBand) =>
      y.taxTypeCode(2) - x.taxTypeCode(2)
    )
  }
}
