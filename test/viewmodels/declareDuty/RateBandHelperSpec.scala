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
import cats.data.NonEmptySeq
import generators.ModelGenerators
import models.AlcoholRegime.{Cider, OtherFermentedProduct}
import models.RateType.{Core, DraughtRelief}
import models.{ABVRange, AlcoholByVolume, AlcoholType, RangeDetailsByRegime, RateBand, RateType}
import org.scalacheck.Gen
import play.api.i18n.Messages

class RateBandHelperSpec extends SpecBase {
  val application                 = applicationBuilder().build()
  implicit val messages: Messages = getMessages(application)

  "RateBandHelper" - {

    "should return the correct message when choosing bands to report" - {

      "for a single interval" - {
        val lowerLimit = BigDecimal(1)
        val upperLimit = BigDecimal(10)

        "without a known regime" in new SetUp {
          val rateBand = singleIntervalRateBand(lowerLimit, upperLimit, rateTypeStandard)

          val result = RateBandHelper.rateBandContent(rateBand, None)

          result mustEqual messages.messages(
            "return.journey.abv.single.interval",
            alcoholTypeMessageKey,
            lowerLimit,
            andText,
            upperLimit,
            taxType
          )
        }

        "with a known regime" - {
          "and matches that of the rate band interval" in new SetUp {
            val rateBand = singleIntervalRateBand(lowerLimit, upperLimit, rateTypeStandard)

            val result = RateBandHelper.rateBandContent(rateBand, Some(regime))

            result mustEqual messages.messages(
              "return.journey.abv.single.interval",
              alcoholTypeMessageKey,
              lowerLimit,
              andText,
              upperLimit,
              taxType
            )
          }

          "and doesn't match that of the rate band interval" in new SetUp {
            val rateBand = singleIntervalRateBand(lowerLimit, upperLimit, rateTypeStandard)

            an[IllegalArgumentException] must be thrownBy RateBandHelper
              .rateBandContent(rateBand, Some(anotherRegime))
          }
        }

        "for interval with MAX value" in new SetUp {
          val rateBand = singleIntervalRateBand(lowerLimit, AlcoholByVolume.MAX.value.toInt, rateTypeStandard)

          val result = RateBandHelper.rateBandContent(rateBand, None)

          result mustEqual messages.messages(
            "return.journey.abv.interval.exceeding.max",
            alcoholTypeMessageKey,
            lowerLimit,
            taxType
          )
        }
      }

      "for multiple intervals" - {
        val lowerLimit1 = BigDecimal(1)
        val upperLimit1 = BigDecimal(10)
        val lowerLimit2 = BigDecimal(11)
        val upperLimit2 = BigDecimal(20)

        "without a known regime" in new SetUp {
          val rateBand = multipleIntervalRateBand(lowerLimit1, upperLimit1, lowerLimit2, upperLimit2, rateTypeStandard)

          val result = RateBandHelper.rateBandContent(rateBand, None)

          result mustEqual messages.messages(
            "return.journey.abv.multi.interval",
            alcoholTypeMessageKey,
            lowerLimit1,
            andText,
            upperLimit1,
            secondaryAlcoholTypeMessageKey,
            lowerLimit2,
            andText,
            upperLimit2,
            taxType
          )
        }

        "with a known regime to select a single range from two regimes" in new SetUp {
          val rateBand =
            multipleIntervalRateBandTwoRegimes(lowerLimit1, upperLimit1, lowerLimit2, upperLimit2, rateTypeStandard)

          val result = RateBandHelper.rateBandContent(rateBand, Some(regime))

          result mustEqual messages.messages(
            "return.journey.abv.single.interval",
            alcoholTypeMessageKey,
            lowerLimit1,
            andText,
            upperLimit1,
            taxType
          )
        }
      }
    }

    "should return the correct message when creating labels for confirmation lists and tables to report" - {
      RateType.values.foreach { rateType =>
        s"for a single interval for rate type: $rateType" - {
          val lowerLimit = BigDecimal(1)
          val upperLimit = BigDecimal(10)

          "without a known regime" in new SetUp {
            val rateBand = singleIntervalRateBand(lowerLimit, upperLimit, rateType)

            val result = RateBandHelper.rateBandRecap(rateBand, None)

            result mustEqual messages
              .messages(
                s"return.journey.abv.recap.single.interval.$rateType",
                alcoholTypeMessageKey,
                lowerLimit,
                andText,
                upperLimit,
                taxType
              )
          }

          "with a known regime" - {
            "and matches that of the rate band interval" in new SetUp {
              val rateBand = singleIntervalRateBand(lowerLimit, upperLimit, rateType)

              val result = RateBandHelper.rateBandRecap(rateBand, Some(regime))

              result mustEqual messages
                .messages(
                  s"return.journey.abv.recap.single.interval.$rateType",
                  alcoholTypeMessageKey,
                  lowerLimit,
                  andText,
                  upperLimit,
                  taxType
                )
            }

            "and doesn't match that of the rate band interval" in new SetUp {
              val rateBand = singleIntervalRateBand(lowerLimit, upperLimit, rateType)

              an[IllegalArgumentException] must be thrownBy RateBandHelper
                .rateBandRecap(rateBand, Some(anotherRegime))
            }

            "for interval with MAX value" in new SetUp {
              val rateBand = singleIntervalRateBand(lowerLimit, AlcoholByVolume.MAX.value.toInt, rateType)

              val result = RateBandHelper.rateBandRecap(rateBand, None)

              result mustEqual messages
                .messages(
                  s"return.journey.abv.recap.interval.exceeding.max.$rateType",
                  alcoholTypeMessageKey,
                  lowerLimit,
                  taxType
                )
            }
          }
        }

        s"for multiple intervals for rate type: $rateType" - {
          val lowerLimit1 = BigDecimal(1)
          val upperLimit1 = BigDecimal(10)
          val lowerLimit2 = BigDecimal(11)
          val upperLimit2 = BigDecimal(20)

          "without a known regime" in new SetUp {
            val rateBand = multipleIntervalRateBand(lowerLimit1, upperLimit1, lowerLimit2, upperLimit2, rateType)

            val result = RateBandHelper.rateBandRecap(rateBand, None)

            result mustEqual messages
              .messages(
                s"return.journey.abv.recap.multi.interval.$rateType",
                alcoholTypeMessageKey,
                lowerLimit1,
                andText,
                upperLimit1,
                secondaryAlcoholTypeMessageKey,
                lowerLimit2,
                andText,
                upperLimit2,
                taxType
              )
          }

          "with a known regime to select a single range from two regimes" in new SetUp {
            val rateBand =
              multipleIntervalRateBandTwoRegimes(lowerLimit1, upperLimit1, lowerLimit2, upperLimit2, rateType)

            val result = RateBandHelper.rateBandRecap(rateBand, Some(regime))

            result mustEqual messages
              .messages(
                s"return.journey.abv.recap.single.interval.$rateType",
                alcoholTypeMessageKey,
                lowerLimit1,
                andText,
                upperLimit1,
                taxType
              )
          }
        }
      }
    }
  }

  class SetUp extends ModelGenerators {
    val regime                         = Cider
    val anotherRegime                  = OtherFermentedProduct
    val taxType                        = "001"
    val description                    = "test"
    val label                          = AlcoholType.Cider
    val alcoholTypeMessageKey          = messages("alcoholType.cider")
    val secondaryLabel                 = AlcoholType.SparklingCider
    val secondaryAlcoholTypeMessageKey = messages("alcoholType.sparklingcider")
    val andText                        = "and"

    val rateTypeStandard = Gen.oneOf(Core, DraughtRelief).sample.value

    def singleIntervalRateBand(lowerLimit: BigDecimal, upperLimit: BigDecimal, rateType: RateType) = RateBand(
      taxType,
      description,
      rateType,
      None,
      Set(
        RangeDetailsByRegime(
          regime,
          NonEmptySeq.one(
            ABVRange(
              label,
              AlcoholByVolume(lowerLimit),
              AlcoholByVolume(upperLimit)
            )
          )
        )
      )
    )

    def multipleIntervalRateBand(
      lowerLimit1: BigDecimal,
      upperLimit1: BigDecimal,
      lowerLimit2: BigDecimal,
      upperLimit2: BigDecimal,
      rateType: RateType
    ) = RateBand(
      taxType,
      description,
      rateType,
      None,
      Set(
        RangeDetailsByRegime(
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
      )
    )

    def multipleIntervalRateBandTwoRegimes(
      lowerLimit1: BigDecimal,
      upperLimit1: BigDecimal,
      lowerLimit2: BigDecimal,
      upperLimit2: BigDecimal,
      rateType: RateType
    ) = RateBand(
      taxType,
      description,
      rateType,
      None,
      Set(
        RangeDetailsByRegime(
          regime,
          NonEmptySeq.fromSeqUnsafe(
            Seq(
              ABVRange(
                label,
                AlcoholByVolume(lowerLimit1),
                AlcoholByVolume(upperLimit1)
              )
            )
          )
        ),
        RangeDetailsByRegime(
          anotherRegime,
          NonEmptySeq.fromSeqUnsafe(
            Seq(
              ABVRange(
                secondaryLabel,
                AlcoholByVolume(lowerLimit2),
                AlcoholByVolume(upperLimit2)
              )
            )
          )
        )
      )
    )
  }
}
