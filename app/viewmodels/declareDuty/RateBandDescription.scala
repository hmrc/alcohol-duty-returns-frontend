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

import models.{ABVRange, AlcoholRegime, RateBand, RateType}
import play.api.i18n.Messages

object RateBandDescription {

  private def abvRangesFromRateBand(rateBand: RateBand, maybeByRegime: Option[AlcoholRegime]): Set[ABVRange] =
    maybeByRegime
      .fold(rateBand.rangeDetails)(regime => rateBand.rangeDetails.filter(_.alcoholRegime == regime))
      .flatMap(_.abvRanges.toSeq)

  def toDescription(
    rateBand: RateBand,
    maybeByRegime: Option[AlcoholRegime],
    showDraughtStatus: Boolean = true
  )(implicit messages: Messages): String = {
    val taxTypeCode = rateBand.taxTypeCode

    abvRangesFromRateBand(rateBand, maybeByRegime).toList match {
      case Nil                        =>
        throw new IllegalArgumentException(
          s"No ranges found for tax code $taxTypeCode regime ${maybeByRegime.map(_.entryName).getOrElse("None")}"
        )
      case List(abvRange)             =>
        singleIntervalText(
          abvRange,
          taxTypeCode,
          rateBand.rateType,
          showDraughtStatus
        )
      case List(abvRange1, abvRange2) =>
        multipleIntervalsText(abvRange1, abvRange2, taxTypeCode, rateBand.rateType, showDraughtStatus)
      case _                          =>
        throw new IllegalArgumentException(
          s"Only 2 ranges supported at present, more found for tax code $taxTypeCode regime ${maybeByRegime.map(_.entryName).getOrElse("None")}"
        )
    }
  }

  private def getAlcoholTypeWithDraughtStatus(interval: ABVRange, rateType: RateType, showDraughtStatus: Boolean)(
    implicit messages: Messages
  ): String =
    if (showDraughtStatus) {
      val draughtOrNotKey = if (rateType.isDraught) "draught" else "nondraught"
      messages(s"return.journey.abv.interval.label.${interval.alcoholType}.$draughtOrNotKey")
    } else {
      messages(s"return.journey.abv.interval.label.${interval.alcoholType}")
    }

  private def getAbvRange(minAbv: BigDecimal, maxAbv: BigDecimal)(implicit messages: Messages): String = {
    val rateMessageToUse = maxAbv.toInt

    val messageKey = s"return.journey.abv.rangeKey.$rateMessageToUse"

    val text = messages(messageKey, minAbv, maxAbv)

    if (text == messageKey) {
      throw new IllegalArgumentException(s"Cannot find message key $messageKey")
    }

    text
  }

  private def getTaxType(taxTypeCode: String, rateType: RateType)(implicit
    messages: Messages
  ): String =
    if (rateType.isSPR) {
      messages(s"return.journey.abv.taxTypeCode.SPR", taxTypeCode)
    } else {
      messages(s"return.journey.abv.taxTypeCode", taxTypeCode)
    }

  private def singleIntervalText(
    abvRange: ABVRange,
    taxTypeCode: String,
    rateType: RateType,
    showDraughtStatus: Boolean
  )(implicit
    messages: Messages
  ): String =
    messages(
      s"return.journey.abv.single.interval",
      getAlcoholTypeWithDraughtStatus(abvRange, rateType, showDraughtStatus),
      getAbvRange(abvRange.minABV.value, abvRange.maxABV.value),
      getTaxType(taxTypeCode, rateType)
    )

  private def multipleIntervalsText(
    abvRange1: ABVRange,
    abvRange2: ABVRange,
    taxTypeCode: String,
    rateType: RateType,
    showDraughtStatus: Boolean
  )(implicit
    messages: Messages
  ): String =
    messages(
      s"return.journey.abv.multi.interval.${abvRange2.alcoholType}",
      getAlcoholTypeWithDraughtStatus(abvRange1, rateType, showDraughtStatus),
      getAbvRange(abvRange1.minABV.value, abvRange1.maxABV.value),
      getAbvRange(abvRange2.minABV.value, abvRange2.maxABV.value),
      getTaxType(taxTypeCode, rateType)
    )
}
