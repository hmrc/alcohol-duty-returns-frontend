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

import models.{ABVRange, AlcoholByVolume, AlcoholRegime, RateBand, RateType}
import play.api.i18n.Messages
import utils.WelshHelper

object RateBandHelper {

  private def abvRangesFromRateBand(rateBand: RateBand, maybeByRegime: Option[AlcoholRegime]): Set[ABVRange] =
    maybeByRegime
      .fold(rateBand.rangeDetails)(regime => rateBand.rangeDetails.filter(_.alcoholRegime == regime))
      .flatMap(_.abvRanges.toSeq)

  def rateBandContent(rateBand: RateBand, maybeByRegime: Option[AlcoholRegime])(implicit
    messages: Messages
  ): String =
    abvRangesFromRateBand(rateBand, maybeByRegime).toList match {
      case Nil                        =>
        throw new IllegalArgumentException(
          s"No ranges found for tax code ${rateBand.taxTypeCode} regime ${maybeByRegime.map(_.entryName).getOrElse("None")}"
        )
      case List(abvRange)             =>
        singleInterval(abvRange, rateBand.taxTypeCode)
      case List(abvRange1, abvRange2) =>
        multipleIntervals(abvRange1, abvRange2, rateBand.taxTypeCode)
      case _                          =>
        throw new IllegalArgumentException(
          s"Only 2 ranges supported at present, more found for tax code ${rateBand.taxTypeCode} regime ${maybeByRegime.map(_.entryName).getOrElse("None")}"
        )

    }

  private def singleInterval(abvRange: ABVRange, taxType: String)(implicit messages: Messages): String =
    abvRange.maxABV match {
      case AlcoholByVolume.MAX =>
        messages(
          "return.journey.abv.interval.exceeding.max",
          messages(abvRange.alcoholType.alcoholTypeMessageKey),
          abvRange.minABV.value,
          taxType
        )
      case _                   =>
        messages(
          "return.journey.abv.single.interval",
          messages(abvRange.alcoholType.alcoholTypeMessageKey),
          abvRange.minABV.value,
          messages(WelshHelper.chooseAnd(abvRange.maxABV.value)),
          abvRange.maxABV.value,
          taxType
        )
    }

  private def multipleIntervals(abvRange1: ABVRange, abvRange2: ABVRange, taxType: String)(implicit
    messages: Messages
  ): String =
    messages(
      "return.journey.abv.multi.interval",
      messages(abvRange1.alcoholType.alcoholTypeMessageKey),
      abvRange1.minABV.value,
      messages(WelshHelper.chooseAnd(abvRange1.maxABV.value)),
      abvRange1.maxABV.value,
      messages(abvRange2.alcoholType.alcoholTypeMessageKey),
      abvRange2.minABV.value,
      messages(WelshHelper.chooseAnd(abvRange2.maxABV.value)),
      abvRange2.maxABV.value,
      taxType
    )

  def rateBandRecap(
    rateBand: RateBand,
    maybeByRegime: Option[AlcoholRegime],
    useSoftMutationForFirstAlcoholType: Boolean = false
  )(implicit messages: Messages): String =
    abvRangesFromRateBand(rateBand, maybeByRegime).toList match {
      case Nil                        =>
        throw new IllegalArgumentException(
          s"No ranges found for tax code ${rateBand.taxTypeCode} regime ${maybeByRegime.map(_.entryName).getOrElse("None")}"
        )
      case List(abvRange)             =>
        singleIntervalRecap(
          abvRange,
          rateBand.taxTypeCode,
          rateBand.rateType,
          useSoftMutationForFirstAlcoholType
        )
      case List(abvRange1, abvRange2) =>
        multipleIntervalsRecap(
          abvRange1,
          abvRange2,
          rateBand.taxTypeCode,
          rateBand.rateType,
          useSoftMutationForFirstAlcoholType
        )
      case _                          =>
        throw new IllegalArgumentException(
          s"Only 2 ranges supported at present, more found for tax code ${rateBand.taxTypeCode} regime ${maybeByRegime.map(_.entryName).getOrElse("None")}"
        )
    }

  private def singleIntervalRecap(
    abvRange: ABVRange,
    taxType: String,
    rateType: RateType,
    useSoftMutationForFirstAlcoholType: Boolean
  )(implicit
    messages: Messages
  ): String = {
    val alcoholTypeMessageKey = if (useSoftMutationForFirstAlcoholType) {
      abvRange.alcoholType.alcoholTypeMessageSoftMutationKey
    } else {
      abvRange.alcoholType.alcoholTypeMessageKey
    }

    abvRange.maxABV match {
      case AlcoholByVolume.MAX =>
        messages(
          s"return.journey.abv.recap.interval.exceeding.max.$rateType",
          messages(alcoholTypeMessageKey),
          abvRange.minABV.value,
          taxType
        )
      case _                   =>
        messages(
          s"return.journey.abv.recap.single.interval.$rateType",
          messages(alcoholTypeMessageKey),
          abvRange.minABV.value,
          messages(WelshHelper.chooseAnd(abvRange.maxABV.value)),
          abvRange.maxABV.value,
          taxType
        )
    }
  }

  private def multipleIntervalsRecap(
    abvRange1: ABVRange,
    abvRange2: ABVRange,
    taxType: String,
    rateType: RateType,
    useSoftMutationForFirstAlcoholType: Boolean
  )(implicit
    messages: Messages
  ): String = {
    val firstAlcoholTypeMessageKey = if (useSoftMutationForFirstAlcoholType) {
      abvRange1.alcoholType.alcoholTypeMessageSoftMutationKey
    } else {
      abvRange1.alcoholType.alcoholTypeMessageKey
    }

    messages(
      s"return.journey.abv.recap.multi.interval.$rateType",
      messages(firstAlcoholTypeMessageKey),
      abvRange1.minABV.value,
      messages(WelshHelper.chooseAnd(abvRange1.maxABV.value)),
      abvRange1.maxABV.value,
      messages(abvRange2.alcoholType.alcoholTypeMessageKey),
      abvRange2.minABV.value,
      messages(WelshHelper.chooseAnd(abvRange2.maxABV.value)),
      abvRange2.maxABV.value,
      taxType
    )
  }
}
