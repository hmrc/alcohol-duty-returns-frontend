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
import models.AlcoholRegime.Beer
import models.RateBand
import play.api.i18n.Messages

import scala.collection.immutable.SortedSet

class CategoriesByRateTypeHelperSpec extends SpecBase {
  "CategoriesByRateTypeHelper" - {
    "must return an empty view model when no ratebands are passed" in new SetUp {
      CategoriesByRateTypeHelper
        .rateBandCategories(Set.empty, regime) mustBe CategoriesByRateTypeViewModel(
        Seq.empty,
        Seq.empty,
        Seq.empty,
        Seq.empty
      )
    }

    "must return rate band description when all ratebands are passed" in new SetUp {
      CategoriesByRateTypeHelper.rateBandCategories(allRateBands, regime) mustBe CategoriesByRateTypeViewModel(
        Seq(
          CategoryViewModel(
            "123",
            "non-draught beer between 1% and 3% ABV (tax type code 123)"
          )
        ),
        Seq(
          CategoryViewModel(
            "124",
            "draught beer between 2% and 3% ABV (tax type code 124)"
          )
        ),
        Seq(
          CategoryViewModel(
            "125",
            "non-draught beer between 3% and 4% ABV (tax type code 125 SPR)"
          )
        ),
        Seq(
          CategoryViewModel(
            "126",
            "draught beer between 4% and 5% ABV (tax type code 126 SPR)"
          )
        )
      )
    }

    "must return rate band description when all ratebands except core are passed" in new SetUp {
      CategoriesByRateTypeHelper.rateBandCategories(
        allRateBands - coreRateBand,
        regime
      ) mustBe CategoriesByRateTypeViewModel(
        Seq.empty,
        Seq(
          CategoryViewModel(
            "124",
            "draught beer between 2% and 3% ABV (tax type code 124)"
          )
        ),
        Seq(
          CategoryViewModel(
            "125",
            "non-draught beer between 3% and 4% ABV (tax type code 125 SPR)"
          )
        ),
        Seq(
          CategoryViewModel(
            "126",
            "draught beer between 4% and 5% ABV (tax type code 126 SPR)"
          )
        )
      )
    }

    "must return rate band description when all ratebands except draught relief are passed" in new SetUp {
      CategoriesByRateTypeHelper.rateBandCategories(
        allRateBands - draughtReliefRateBand,
        regime
      ) mustBe CategoriesByRateTypeViewModel(
        Seq(
          CategoryViewModel(
            "123",
            "non-draught beer between 1% and 3% ABV (tax type code 123)"
          )
        ),
        Seq.empty,
        Seq(
          CategoryViewModel(
            "125",
            "non-draught beer between 3% and 4% ABV (tax type code 125 SPR)"
          )
        ),
        Seq(
          CategoryViewModel(
            "126",
            "draught beer between 4% and 5% ABV (tax type code 126 SPR)"
          )
        )
      )
    }

    "must return rate band description when all ratebands except small producer relief are passed" in new SetUp {
      CategoriesByRateTypeHelper.rateBandCategories(
        allRateBands - smallProducerReliefRateBand,
        regime
      ) mustBe CategoriesByRateTypeViewModel(
        Seq(
          CategoryViewModel(
            "123",
            "non-draught beer between 1% and 3% ABV (tax type code 123)"
          )
        ),
        Seq(
          CategoryViewModel(
            "124",
            "draught beer between 2% and 3% ABV (tax type code 124)"
          )
        ),
        Seq.empty,
        Seq(
          CategoryViewModel(
            "126",
            "draught beer between 4% and 5% ABV (tax type code 126 SPR)"
          )
        )
      )
    }

    "must return rate band description when all ratebands except draught and small producer relief are passed" in new SetUp {
      CategoriesByRateTypeHelper.rateBandCategories(
        allRateBands - draughtAndSmallProducerReliefRateBand,
        regime
      ) mustBe CategoriesByRateTypeViewModel(
        Seq(
          CategoryViewModel(
            "123",
            "non-draught beer between 1% and 3% ABV (tax type code 123)"
          )
        ),
        Seq(
          CategoryViewModel(
            "124",
            "draught beer between 2% and 3% ABV (tax type code 124)"
          )
        ),
        Seq(
          CategoryViewModel(
            "125",
            "non-draught beer between 3% and 4% ABV (tax type code 125 SPR)"
          )
        ),
        Seq.empty
      )
    }

    "must group types and sort by tax code" in new SetUp {
      CategoriesByRateTypeHelper.rateBandCategories(
        threeCoreRandBandsReversed,
        regime
      ) mustBe CategoriesByRateTypeViewModel(
        Seq(
          CategoryViewModel(
            "123",
            "non-draught beer between 1% and 3% ABV (tax type code 123)"
          ),
          CategoryViewModel(
            "124",
            "non-draught beer between 1% and 3% ABV (tax type code 124)"
          ),
          CategoryViewModel(
            "125",
            "non-draught beer between 1% and 3% ABV (tax type code 125)"
          )
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

    val regime = Beer

    val threeCoreRandBandsReversed = SortedSet(coreRateBand, coreRateBand2, coreRateBand3)((x: RateBand, y: RateBand) =>
      y.taxTypeCode(2) - x.taxTypeCode(2)
    )
  }
}
