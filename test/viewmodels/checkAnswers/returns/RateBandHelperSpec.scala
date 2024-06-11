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

package viewmodels.checkAnswers.returns

import base.SpecBase
import cats.data.NonEmptySeq
import models.RateType.{Core, DraughtAndSmallProducerRelief, DraughtRelief, SmallProducerRelief}
import models.{ABVInterval, ABVIntervalLabel, AlcoholByVolume, RateBand}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen

class RateBandHelperSpec extends SpecBase {

  "RateBandHelper" - {
    val application = applicationBuilder().build()

    val regime                = regimeGen.sample.value
    val taxType               = "001"
    val description           = "test"
    val label                 = arbitrary[ABVIntervalLabel].sample.value
    val alcoholLabel          = messages(application).messages(s"return.journey.abv.interval.label.$label")
    val secondaryLabel        = arbitrary[ABVIntervalLabel].suchThat(_ != label).sample.value
    val secondaryAlcoholLabel = messages(application).messages(s"return.journey.abv.interval.label.$secondaryLabel")

    val rateTypeStandard = Gen.oneOf(Core, DraughtRelief).sample.value
    val rateTypeSPR      = Gen.oneOf(SmallProducerRelief, DraughtAndSmallProducerRelief).sample.value

    "should return the correct message when choosing bands to report" - {

      "for a single interval" in {

        val lowerLimit = 1
        val upperLimit = 10

        val rateBand = RateBand(
          taxType,
          description,
          rateTypeStandard,
          Set(regime),
          NonEmptySeq.one(
            ABVInterval(
              label,
              AlcoholByVolume(lowerLimit),
              AlcoholByVolume(upperLimit)
            )
          ),
          None
        )

        val result = RateBandHelper.rateBandContent(rateBand)(messages(application))

        result mustEqual s"$alcoholLabel between $lowerLimit% and $upperLimit% ABV (tax type code $taxType)".capitalize

      }

      "for multiple intervals" in {

        val lowerLimit1 = 1
        val upperLimit1 = 10
        val lowerLimit2 = 11
        val upperLimit2 = 20

        val rateBand = RateBand(
          taxType,
          description,
          rateTypeStandard,
          Set(regime),
          NonEmptySeq.of(
            ABVInterval(
              label,
              AlcoholByVolume(lowerLimit1),
              AlcoholByVolume(upperLimit1)
            ),
            ABVInterval(
              secondaryLabel,
              AlcoholByVolume(lowerLimit2),
              AlcoholByVolume(upperLimit2)
            )
          ),
          None
        )

        val result = RateBandHelper.rateBandContent(rateBand)(messages(application))

        result mustEqual s"$alcoholLabel between $lowerLimit1% and $upperLimit1% ABV and $secondaryAlcoholLabel between $lowerLimit2% and $upperLimit2% ABV (tax type code $taxType)".capitalize
      }

      "for interval with MAX value" in {

        val lowerLimit = 1

        val rateBand = RateBand(
          taxType,
          description,
          rateTypeStandard,
          Set(regime),
          NonEmptySeq.one(
            ABVInterval(
              label,
              AlcoholByVolume(lowerLimit),
              AlcoholByVolume.MAX
            )
          ),
          None
        )

        val result = RateBandHelper.rateBandContent(rateBand)(messages(application))

        result mustEqual s"$alcoholLabel exceeding $lowerLimit% ABV (tax type code $taxType)".capitalize
      }
    }

    "should return the correct message when creating labels for confirmation lists and tables to report" - {

      "for a single interval" in {
        val lowerLimit = 1
        val upperLimit = 10

        val rateBand = RateBand(
          taxType,
          description,
          Core,
          Set(regime),
          NonEmptySeq.one(
            ABVInterval(
              label,
              AlcoholByVolume(lowerLimit),
              AlcoholByVolume(upperLimit)
            )
          ),
          None
        )

        val result = RateBandHelper.rateBandRecap(rateBand)(messages(application))

        result mustEqual s"Non-draught $alcoholLabel between $lowerLimit% and $upperLimit% ABV ($taxType)"
      }

      "for multiple intervals" in {
        val lowerLimit1 = 1
        val upperLimit1 = 10
        val lowerLimit2 = 11
        val upperLimit2 = 20

        val rateBand = RateBand(
          taxType,
          description,
          Core,
          Set(regime),
          NonEmptySeq.of(
            ABVInterval(
              label,
              AlcoholByVolume(lowerLimit1),
              AlcoholByVolume(upperLimit1)
            ),
            ABVInterval(
              secondaryLabel,
              AlcoholByVolume(lowerLimit2),
              AlcoholByVolume(upperLimit2)
            )
          ),
          None
        )

        val result = RateBandHelper.rateBandRecap(rateBand)(messages(application))

        result mustEqual s"Non-draught $alcoholLabel between $lowerLimit1% and $upperLimit1% ABV and $secondaryAlcoholLabel between $lowerLimit2% and $upperLimit2% ABV ($taxType)"
      }

      "for interval with MAX value" in {
        val lowerLimit = 1

        val rateBand = RateBand(
          taxType,
          description,
          Core,
          Set(regime),
          NonEmptySeq.one(
            ABVInterval(
              label,
              AlcoholByVolume(lowerLimit),
              AlcoholByVolume.MAX
            )
          ),
          None
        )

        val result = RateBandHelper.rateBandRecap(rateBand)(messages(application))

        result mustEqual s"Non-draught $alcoholLabel exceeding $lowerLimit% ABV ($taxType)"
      }

      "with Single Producer Relief" - {

        "for a single interval" in {}

        "for multiple intervals" in {}

        "for interval with MAX value" in {}
      }
    }

    "should return the correct message for Volumes heading" - {

      "for a single interval" in {}

      "for multiple intervals" in {}

      "for interval with MAX value" in {}
    }

    "should return the correct message for Total litres field label" - {

      "for a single interval" in {}

      "for multiple intervals" in {}

      "for interval with MAX value" in {}
    }

    "should return the correct message for Pure Alcohol field label" - {

      "for a single interval" in {}

      "for multiple intervals" in {}

      "for interval with MAX value" in {}
    }

  }
}
