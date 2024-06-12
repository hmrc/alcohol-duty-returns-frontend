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

import cats.data.NonEmptySeq
import models.{ABVInterval, AlcoholByVolume, RateBand, RateType}
import play.api.i18n.Messages

object RateBandHelper {

  def rateBandContent(rateBand: RateBand)(implicit messages: Messages): String =
    rateBand.intervals.length match {
      case 1 =>
        singleInterval(rateBand.intervals.head, rateBand.taxType)
      case _ =>
        multipleIntervals(rateBand.intervals, rateBand.taxType)
    }

  private def singleInterval(interval: ABVInterval, taxType: String)(implicit messages: Messages): String =
    interval.maxABV match {
      case AlcoholByVolume.MAX =>
        messages(
          "return.journey.abv.interval.exceeding.max",
          messages(s"return.journey.abv.interval.label.${interval.label}"),
          interval.minABV.value,
          taxType
        ).capitalize
      case _                   =>
        messages(
          "return.journey.abv.single.interval",
          messages(s"return.journey.abv.interval.label.${interval.label}"),
          interval.minABV.value,
          interval.maxABV.value,
          taxType
        ).capitalize
    }

  private def multipleIntervals(intervals: NonEmptySeq[ABVInterval], taxType: String)(implicit
    messages: Messages
  ): String = {
    val firstInterval = intervals.head
    val lastInterval  = intervals.last

    messages(
      "return.journey.abv.multi.interval",
      messages(s"return.journey.abv.interval.label.${firstInterval.label}"),
      firstInterval.minABV.value,
      firstInterval.maxABV.value,
      messages(s"return.journey.abv.interval.label.${lastInterval.label}"),
      lastInterval.minABV.value,
      lastInterval.maxABV.value,
      taxType
    ).capitalize
  }

  def rateBandRecap(rateBand: RateBand)(implicit messages: Messages): String =
    rateBand.intervals.length match {
      case 1 =>
        singleIntervalRecap(rateBand.intervals.head, rateBand.taxType, rateBand.rateType)
      case _ =>
        multipleIntervalsRecap(rateBand.intervals, rateBand.taxType, rateBand.rateType)
    }

  private def singleIntervalRecap(interval: ABVInterval, taxType: String, rateType: RateType)(implicit
    messages: Messages
  ): String =
    interval.maxABV match {
      case AlcoholByVolume.MAX =>
        messages(
          s"return.journey.abv.recap.interval.exceeding.max.$rateType",
          messages(s"return.journey.abv.interval.label.${interval.label}"),
          interval.minABV.value,
          taxType
        ).capitalize
      case _                   =>
        messages(
          s"return.journey.abv.recap.single.interval.$rateType",
          messages(s"return.journey.abv.interval.label.${interval.label}"),
          interval.minABV.value,
          interval.maxABV.value,
          taxType
        ).capitalize
    }

  private def multipleIntervalsRecap(intervals: NonEmptySeq[ABVInterval], taxType: String, rateType: RateType)(implicit
    messages: Messages
  ): String = {
    val firstInterval = intervals.head
    val lastInterval  = intervals.last

    messages(
      s"return.journey.abv.recap.multi.interval.$rateType",
      messages(s"return.journey.abv.interval.label.${firstInterval.label}"),
      firstInterval.minABV.value,
      firstInterval.maxABV.value,
      messages(s"return.journey.abv.interval.label.${lastInterval.label}"),
      lastInterval.minABV.value,
      lastInterval.maxABV.value,
      taxType
    ).capitalize
  }
}
