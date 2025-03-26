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
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, RangeDetailsByRegime, RateBand, RateType}
import play.api.i18n.Messages

class RateBandDescriptionSpec extends SpecBase {
  val application                 = applicationBuilder().build()
  implicit val messages: Messages = getMessages(application)

  "RateBandDescription" - {
    Seq(true, false).foreach { showDraughtStatus =>
      s"when ${if (showDraughtStatus) {
        "showing"
      } else {
        "not showing"
      }} draught status" - {
        "must return the correct description" - {
          Seq(
            (AlcoholRegime.Beer, AlcoholType.Beer, "beer"),
            (AlcoholRegime.Cider, AlcoholType.Cider, "cider"),
            (AlcoholRegime.Cider, AlcoholType.SparklingCider, "sparkling cider"),
            (AlcoholRegime.Wine, AlcoholType.Wine, "wine"),
            (AlcoholRegime.Spirits, AlcoholType.Spirits, "spirits"),
            (AlcoholRegime.OtherFermentedProduct, AlcoholType.OtherFermentedProduct, "other fermented products")
          ).foreach { case (alcoholRegime1, alcoholType1, alcoholTypeDescription1) =>
            s"when the first alcohol type is $alcoholTypeDescription1" - {
              Seq(
                (
                  RateType.Core,
                  if (showDraughtStatus) { "non-draught " }
                  else { "" },
                  ""
                ),
                (
                  RateType.DraughtRelief,
                  if (showDraughtStatus) { "draught " }
                  else { "" },
                  ""
                ),
                (
                  RateType.SmallProducerRelief,
                  if (showDraughtStatus) { "non-draught " }
                  else { "" },
                  " SPR"
                ),
                (
                  RateType.DraughtAndSmallProducerRelief,
                  if (showDraughtStatus) { "draught " }
                  else { "" },
                  " SPR"
                )
              ).foreach { case (rateType, draughtText, sprText) =>
                s"for a single interval for rate type: $rateType" - {
                  val lowerLimit = BigDecimal(1)
                  val upperLimit = BigDecimal(10)

                  "without knowing the regime" in new SetUp {
                    val rateBand =
                      singleIntervalRateBand(lowerLimit, upperLimit, rateType, alcoholRegime1, alcoholType1)

                    val result = RateBandDescription.toDescription(rateBand, None, showDraughtStatus)

                    result mustEqual s"$draughtText$alcoholTypeDescription1 between $lowerLimit% and $upperLimit% ABV (tax type code $taxTypeCode$sprText)"
                  }

                  "with a knowing the regime" - {
                    "and matches that of the rate band interval" in new SetUp {
                      val rateBand =
                        singleIntervalRateBand(lowerLimit, upperLimit, rateType, alcoholRegime1, alcoholType1)

                      val result = RateBandDescription.toDescription(rateBand, Some(alcoholRegime1), showDraughtStatus)

                      result mustEqual s"$draughtText$alcoholTypeDescription1 between $lowerLimit% and $upperLimit% ABV (tax type code $taxTypeCode$sprText)"
                    }

                    "and doesn't match that of the rate band interval" in new SetUp {
                      val rateBand =
                        singleIntervalRateBand(lowerLimit, upperLimit, rateType, alcoholRegime1, alcoholType1)

                      val otherRegime = if (alcoholRegime1 != AlcoholRegime.OtherFermentedProduct) {
                        AlcoholRegime.OtherFermentedProduct
                      } else {
                        AlcoholRegime.Beer
                      }

                      an[IllegalArgumentException] must be thrownBy RateBandDescription
                        .toDescription(rateBand, Some(otherRegime), showDraughtStatus)
                    }

                    "and has an interval with a MAX value" in new SetUp {
                      val rateBand = singleIntervalRateBand(
                        lowerLimit,
                        AlcoholByVolume.MAX.value.toInt,
                        rateType,
                        alcoholRegime1,
                        alcoholType1
                      )

                      val result = RateBandDescription.toDescription(rateBand, None, showDraughtStatus)

                      result mustEqual s"$draughtText$alcoholTypeDescription1 at or above $lowerLimit% ABV (tax type code $taxTypeCode$sprText)"
                    }
                  }
                }

                s"for multiple intervals for rate type: $rateType" - {
                  val lowerLimit1 = BigDecimal(1)
                  val upperLimit1 = BigDecimal(10)
                  val lowerLimit2 = BigDecimal(11)
                  val upperLimit2 = BigDecimal(20)

                  "without knowing the regime" in new SetUp {
                    val rateBand = multipleIntervalRateBand(
                      lowerLimit1,
                      upperLimit1,
                      lowerLimit2,
                      upperLimit2,
                      rateType,
                      alcoholRegime1,
                      alcoholType1
                    )

                    val result = RateBandDescription.toDescription(rateBand, None, showDraughtStatus)

                    result mustEqual s"$draughtText$alcoholTypeDescription1 between $lowerLimit1% and $upperLimit1% ABV and $alcoholDescription2 between $lowerLimit2% and $upperLimit2% ABV (tax type code $taxTypeCode$sprText)"
                  }

                  "with a known regime to select a single range from two regimes" in new SetUp {
                    val (otherRegime, otherAlcoholType: AlcoholType) =
                      if (alcoholRegime1 != AlcoholRegime.OtherFermentedProduct) {
                        (AlcoholRegime.OtherFermentedProduct, AlcoholType.OtherFermentedProduct)
                      } else {
                        (AlcoholRegime.Beer, AlcoholType.Beer)
                      }

                    val rateBand =
                      multipleIntervalRateBandTwoRegimes(
                        lowerLimit1,
                        upperLimit1,
                        lowerLimit2,
                        upperLimit2,
                        rateType,
                        alcoholRegime1,
                        alcoholType1,
                        otherRegime,
                        otherAlcoholType
                      )

                    val result = RateBandDescription.toDescription(rateBand, Some(alcoholRegime1), showDraughtStatus)

                    result mustEqual s"$draughtText$alcoholTypeDescription1 between $lowerLimit1% and $upperLimit1% ABV (tax type code $taxTypeCode$sprText)"
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  class SetUp extends ModelGenerators {
    val taxTypeCode         = "311"
    val description         = "test"
    val alcoholRegime2      = AlcoholRegime.OtherFermentedProduct
    val alcoholType2        = AlcoholType.OtherFermentedProduct
    val alcoholDescription2 = "other fermented products"

    def singleIntervalRateBand(
      lowerLimit: BigDecimal,
      upperLimit: BigDecimal,
      rateType: RateType,
      alcoholRegime: AlcoholRegime,
      alcoholType: AlcoholType
    ) = RateBand(
      taxTypeCode,
      description,
      rateType,
      None,
      Set(
        RangeDetailsByRegime(
          alcoholRegime,
          NonEmptySeq.one(
            ABVRange(
              alcoholType,
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
      rateType: RateType,
      alcoholRegime: AlcoholRegime,
      alcoholType1: AlcoholType
    ) = RateBand(
      taxTypeCode,
      description,
      rateType,
      None,
      Set(
        RangeDetailsByRegime(
          alcoholRegime,
          NonEmptySeq.fromSeqUnsafe(
            Seq(
              ABVRange(
                alcoholType1,
                AlcoholByVolume(lowerLimit1),
                AlcoholByVolume(upperLimit1)
              ),
              ABVRange(
                alcoholType2,
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
      rateType: RateType,
      alcoholRegime1: AlcoholRegime,
      alcoholType1: AlcoholType,
      alcoholRegime2: AlcoholRegime,
      alcoholType2: AlcoholType
    ) = RateBand(
      taxTypeCode,
      description,
      rateType,
      None,
      Set(
        RangeDetailsByRegime(
          alcoholRegime1,
          NonEmptySeq.fromSeqUnsafe(
            Seq(
              ABVRange(
                alcoholType1,
                AlcoholByVolume(lowerLimit1),
                AlcoholByVolume(upperLimit1)
              )
            )
          )
        ),
        RangeDetailsByRegime(
          alcoholRegime2,
          NonEmptySeq.fromSeqUnsafe(
            Seq(
              ABVRange(
                alcoholType2,
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
