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

object RateBandHelper {

  private def abvRangesFromRateBand(rateBand: RateBand, maybeByRegime: Option[AlcoholRegime]): Set[ABVRange] =
    maybeByRegime
      .fold(rateBand.rangeDetails)(regime => rateBand.rangeDetails.filter(_.alcoholRegime == regime))
      .flatMap(_.abvRanges.toSeq)

  def rateBandContent(rateBand: RateBand, maybeByRegime: Option[AlcoholRegime])(implicit
    messages: Messages
  ): String =
    abvRangesFromRateBand(rateBand, maybeByRegime) match {
      case abvRanges if abvRanges.isEmpty   =>
        throw new IllegalArgumentException(
          s"No ranges found for tax code ${rateBand.taxTypeCode} regime ${maybeByRegime.map(_.entryName).getOrElse("None")}"
        )
      case abvRanges if abvRanges.size == 1 =>
        singleInterval(abvRanges.head, rateBand.taxTypeCode)
      case abvRanges                        =>
        multipleIntervals(abvRanges, rateBand.taxTypeCode)

    }

  private def singleInterval(interval: ABVRange, taxType: String)(implicit messages: Messages): String =
    interval.maxABV match {
      case AlcoholByVolume.MAX =>
        messages(
          "return.journey.abv.interval.exceeding.max",
          messages(s"return.journey.abv.interval.label.${interval.alcoholType}"),
          interval.minABV.value,
          taxType
        ).capitalize
      case _                   =>
        messages(
          "return.journey.abv.single.interval",
          messages(s"return.journey.abv.interval.label.${interval.alcoholType}"),
          interval.minABV.value,
          interval.maxABV.value,
          taxType
        ).capitalize
    }

  private def multipleIntervals(intervals: Set[ABVRange], taxType: String)(implicit
    messages: Messages
  ): String = {
    val firstInterval = intervals.head
    val lastInterval  = intervals.last

    messages(
      "return.journey.abv.multi.interval",
      messages(s"return.journey.abv.interval.label.${firstInterval.alcoholType}"),
      firstInterval.minABV.value,
      firstInterval.maxABV.value,
      messages(s"return.journey.abv.interval.label.${lastInterval.alcoholType}"),
      lastInterval.minABV.value,
      lastInterval.maxABV.value,
      taxType
    ).capitalize
  }

  def rateBandRecap(rateBand: RateBand, maybeByRegime: Option[AlcoholRegime])(implicit messages: Messages): String =
    abvRangesFromRateBand(rateBand, maybeByRegime) match {
      case abvRanges if abvRanges.isEmpty   =>
        throw new IllegalArgumentException(
          s"No ranges found for tax code ${rateBand.taxTypeCode} regime ${maybeByRegime.map(_.entryName).getOrElse("None")}"
        )
      case abvRanges if abvRanges.size == 1 =>
        singleIntervalRecap(
          abvRanges.head,
          rateBand.taxTypeCode,
          rateBand.rateType
        )
      case abvRanges                        =>
        multipleIntervalsRecap(abvRanges, rateBand.taxTypeCode, rateBand.rateType)
    }

  private def singleIntervalRecap(interval: ABVRange, taxType: String, rateType: RateType)(implicit
    messages: Messages
  ): String =
    interval.maxABV match {
      case AlcoholByVolume.MAX =>
        messages(
          s"return.journey.abv.recap.interval.exceeding.max.$rateType",
          messages(s"return.journey.abv.interval.label.${interval.alcoholType}"),
          interval.minABV.value,
          taxType
        ).capitalize
      case _                   =>
        messages(
          s"return.journey.abv.recap.single.interval.$rateType",
          messages(s"return.journey.abv.interval.label.${interval.alcoholType}"),
          interval.minABV.value,
          interval.maxABV.value,
          taxType
        ).capitalize
    }

  private def multipleIntervalsRecap(intervals: Set[ABVRange], taxType: String, rateType: RateType)(implicit
    messages: Messages
  ): String = {
    val firstInterval = intervals.head
    val lastInterval  = intervals.last

    messages(
      s"return.journey.abv.recap.multi.interval.$rateType",
      messages(s"return.journey.abv.interval.label.${firstInterval.alcoholType}"),
      firstInterval.minABV.value,
      firstInterval.maxABV.value,
      messages(s"return.journey.abv.interval.label.${lastInterval.alcoholType}"),
      lastInterval.minABV.value,
      lastInterval.maxABV.value,
      taxType
    ).capitalize
  }
}
