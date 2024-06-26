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
import models.RateType.{Core, DraughtRelief}
import models.{ABVRange, ABVRangeName, AlcoholByVolume, AlcoholRegime, AlcoholRegimeName, RateBand, RateType}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen

class RateBandHelperSpec extends SpecBase {

  "RateBandHelper" - {
    val application = applicationBuilder().build()

    val regime                = regimeGen.sample.value
    val taxType               = "001"
    val description           = "test"
    val label                 = arbitrary[ABVRangeName].sample.value
    val alcoholLabel          = messages(application).messages(s"return.journey.abv.interval.label.$label")
    val secondaryLabel        = arbitrary[ABVRangeName].suchThat(_ != label).sample.value
    val secondaryAlcoholLabel = messages(application).messages(s"return.journey.abv.interval.label.$secondaryLabel")

    val rateTypeStandard = Gen.oneOf(Core, DraughtRelief).sample.value

    "should return the correct message when choosing bands to report" - {

      "for a single interval" in {

        val lowerLimit = 1
        val upperLimit = 10

        val rateBand = RateBand(
          taxType,
          description,
          rateTypeStandard,
          Set(
            AlcoholRegime(
              regime,
              NonEmptySeq.one(
                ABVRange(
                  label,
                  AlcoholByVolume(lowerLimit),
                  AlcoholByVolume(upperLimit)
                )
              )
            )
          ),
          None
        )

        val result = RateBandHelper.rateBandContent(rateBand)(messages(application))

        result mustEqual messages(application).messages(
          "return.journey.abv.single.interval",
          alcoholLabel.capitalize,
          lowerLimit,
          upperLimit,
          taxType
        )

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
          Set(
            AlcoholRegime(
              regime,
              NonEmptySeq.fromSeqUnsafe(
                Seq(
                  ABVRange(
                    label,
                    AlcoholByVolume(lowerLimit1),
                    AlcoholByVolume(upperLimit1)
                  ),
                  ABVRange(
                    secondaryLabel,
                    AlcoholByVolume(lowerLimit2),
                    AlcoholByVolume(upperLimit2)
                  )
                )
              )
            )
          ),
          None
        )

        val result = RateBandHelper.rateBandContent(rateBand)(messages(application))

        result mustEqual messages(application).messages(
          "return.journey.abv.multi.interval",
          alcoholLabel.capitalize,
          lowerLimit1,
          upperLimit1,
          secondaryAlcoholLabel,
          lowerLimit2,
          upperLimit2,
          taxType
        )
      }

      "for interval with MAX value" in {

        val lowerLimit = 1

        val rateBand = RateBand(
          taxType,
          description,
          rateTypeStandard,
          Set(
            AlcoholRegime(
              regime,
              NonEmptySeq.one(
                ABVRange(
                  label,
                  AlcoholByVolume(lowerLimit),
                  AlcoholByVolume.MAX
                )
              )
            )
          ),
          None
        )

        val result = RateBandHelper.rateBandContent(rateBand)(messages(application))

        result mustEqual messages(application).messages(
          "return.journey.abv.interval.exceeding.max",
          alcoholLabel.capitalize,
          lowerLimit,
          taxType
        )
      }
    }

    "should return the correct message when creating labels for confirmation lists and tables to report" - {

      RateType.values.foreach { rateType =>
        s"for a single interval for rate type: $rateType" in {
          val lowerLimit = 1
          val upperLimit = 10

          val rateBand = RateBand(
            taxType,
            description,
            rateType,
            Set(
              AlcoholRegime(
                regime,
                NonEmptySeq.one(
                  ABVRange(
                    label,
                    AlcoholByVolume(lowerLimit),
                    AlcoholByVolume(upperLimit)
                  )
                )
              )
            ),
            None
          )

          val result = RateBandHelper.rateBandRecap(rateBand)(messages(application))

          result mustEqual messages(application)
            .messages(
              s"return.journey.abv.recap.single.interval.$rateType",
              alcoholLabel,
              lowerLimit,
              upperLimit,
              taxType
            )
            .capitalize
        }

        s"for multiple intervals for rate type: $rateType" in {
          val lowerLimit1 = 1
          val upperLimit1 = 10
          val lowerLimit2 = 11
          val upperLimit2 = 20

          val rateBand = RateBand(
            taxType,
            description,
            rateType,
            Set(
              AlcoholRegime(
                regime,
                NonEmptySeq.fromSeqUnsafe(
                  Seq(
                    ABVRange(
                      label,
                      AlcoholByVolume(lowerLimit1),
                      AlcoholByVolume(upperLimit1)
                    ),
                    ABVRange(
                      secondaryLabel,
                      AlcoholByVolume(lowerLimit2),
                      AlcoholByVolume(upperLimit2)
                    )
                  )
                )
              )
            ),
            None
          )

          val result = RateBandHelper.rateBandRecap(rateBand)(messages(application))

          result mustEqual messages(application)
            .messages(
              s"return.journey.abv.recap.multi.interval.$rateType",
              alcoholLabel,
              lowerLimit1,
              upperLimit1,
              secondaryAlcoholLabel,
              lowerLimit2,
              upperLimit2,
              taxType
            )
            .capitalize
        }

        s"for interval with MAX value for rate type: $rateType" in {
          val lowerLimit = 1

          val rateBand = RateBand(
            taxType,
            description,
            rateType,
            Set(
              AlcoholRegime(
                regime,
                NonEmptySeq.one(
                  ABVRange(
                    label,
                    AlcoholByVolume(lowerLimit),
                    AlcoholByVolume.MAX
                  )
                )
              )
            ),
            None
          )

          val result = RateBandHelper.rateBandRecap(rateBand)(messages(application))

          result mustEqual messages(application)
            .messages(
              s"return.journey.abv.recap.interval.exceeding.max.$rateType",
              alcoholLabel,
              lowerLimit,
              taxType
            )
            .capitalize
        }
      }
    }
  }
}
