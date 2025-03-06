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

object RateBandDescription {

  private def abvRangesFromRateBand(rateBand: RateBand, maybeByRegime: Option[AlcoholRegime]): Set[ABVRange] =
    maybeByRegime
      .fold(rateBand.rangeDetails)(regime => rateBand.rangeDetails.filter(_.alcoholRegime == regime))
      .flatMap(_.abvRanges.toSeq)

  def toDescription(rateBand: RateBand, maybeByRegime: Option[AlcoholRegime])(implicit messages: Messages): String =
    abvRangesFromRateBand(rateBand, maybeByRegime).toList match {
      case Nil                        =>
        throw new IllegalArgumentException(
          s"No ranges found for tax code ${rateBand.taxTypeCode} regime ${maybeByRegime.map(_.entryName).getOrElse("None")}"
        )
      case List(abvRange)             =>
        singleIntervalText(
          abvRange,
          rateBand.taxTypeCode,
          rateBand.rateType
        )
      case List(abvRange1, abvRange2) =>
        multipleIntervalsText(abvRange1, abvRange2, rateBand.taxTypeCode, rateBand.rateType)
      case _                          =>
        throw new IllegalArgumentException(
          s"Only 2 ranges supported at present, more found for tax code ${rateBand.taxTypeCode} regime ${maybeByRegime.map(_.entryName).getOrElse("None")}"
        )
    }

  private def singleIntervalText(interval: ABVRange, taxType: String, rateType: RateType)(implicit
    messages: Messages
  ): String =
    interval.maxABV match {
      case AlcoholByVolume.MAX =>
        messages(
          s"return.journey.abv.interval.exceeding.max.$rateType",
          messages(s"return.journey.abv.interval.label.${interval.alcoholType}"),
          interval.minABV.value,
          taxType
        )
      case _                   =>
        messages(
          s"return.journey.abv.single.interval.$rateType",
          messages(s"return.journey.abv.interval.label.${interval.alcoholType}"),
          interval.minABV.value,
          messages(WelshHelper.chooseAnd(interval.maxABV.value)),
          interval.maxABV.value,
          taxType
        )
    }

  private def multipleIntervalsText(abvRange1: ABVRange, abvRange2: ABVRange, taxType: String, rateType: RateType)(
    implicit messages: Messages
  ): String =
    messages(
      s"return.journey.abv.multi.interval.$rateType",
      messages(s"return.journey.abv.interval.label.${abvRange1.alcoholType}"),
      abvRange1.minABV.value,
      messages(WelshHelper.chooseAnd(abvRange1.maxABV.value)),
      abvRange1.maxABV.value,
      messages(s"return.journey.abv.interval.label.${abvRange2.alcoholType}"),
      abvRange2.minABV.value,
      messages(WelshHelper.chooseAnd(abvRange2.maxABV.value)),
      abvRange2.maxABV.value,
      taxType
    )
}
