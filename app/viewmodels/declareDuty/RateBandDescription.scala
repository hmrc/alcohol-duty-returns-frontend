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

  private def abvRangeMessage(baseMessageKey: String, minAbv: BigDecimal, maxAbv: BigDecimal)(implicit
                                                                                              messages: Messages
  ): String = {
    val rateMessageToUse = Math.floor(maxAbv.doubleValue).toInt
    messages(s"$baseMessageKey.$rateMessageToUse", minAbv, maxAbv)
  }

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
    abvRange.maxABV match {
      case AlcoholByVolume.MAX =>
        messages(
          s"return.journey.abv.interval.exceeding.max",
          getAlcoholTypeWithDraughtStatus(abvRange, rateType, showDraughtStatus),
          abvRange.minABV.value,
          getTaxType(taxTypeCode, rateType)
        )
      case _                   =>
        messages(
          s"return.journey.abv.single.interval",
          getAlcoholTypeWithDraughtStatus(abvRange, rateType, showDraughtStatus),
          abvRange.minABV.value,
          messages(WelshHelper.chooseAnd(abvRange.maxABV.value)),
          abvRange.maxABV.value,
          getTaxType(taxTypeCode, rateType)
        )
    }

//  private def singleIntervalText(interval: ABVRange, taxType: String, rateType: RateType)(implicit
//                                                                                          messages: Messages
//  ): String =
//    interval.maxABV match {
//      case AlcoholByVolume.MAX =>
//        messages(
//          s"return.journey.abv.interval.exceeding.max.$rateType",
//          messages(s"return.journey.abv.interval.label.${interval.alcoholType}"),
//          interval.minABV.value,
//          taxType
//        )
//      case _                   =>
//        messages(
//          s"return.journey.abv.single.interval.$rateType",
//          messages(s"return.journey.abv.interval.label.${interval.alcoholType}"),
//          s"${abvRangeMessage("abvRangeKey", interval.minABV.value, interval.maxABV.value)}",
//          taxType
//        )
//    }


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
      s"return.journey.abv.multi.interval",
      getAlcoholTypeWithDraughtStatus(abvRange1, rateType, showDraughtStatus),
      abvRange1.minABV.value,
      messages(WelshHelper.chooseAnd(abvRange1.maxABV.value)),
      abvRange1.maxABV.value,
      messages(s"return.journey.abv.interval.label.${abvRange2.alcoholType}"),
      abvRange2.minABV.value,
      messages(WelshHelper.chooseAnd(abvRange2.maxABV.value)),
      abvRange2.maxABV.value,
      getTaxType(taxTypeCode, rateType)
    )
}

//private def multipleIntervalsText(abvRange1: ABVRange, abvRange2: ABVRange, taxType: String, rateType: RateType)(
//  implicit messages: Messages
//): String =
//  messages(
//    s"return.journey.abv.multi.interval.$rateType",
//    messages(s"return.journey.abv.interval.label.${abvRange1.alcoholType}"),
//    s"${abvRangeMessage("abvRangeKey", abvRange1.minABV.value, abvRange1.maxABV.value)}",
//    messages(s"return.journey.abv.interval.label.${abvRange2.alcoholType}"),
//    s"${abvRangeMessage("abvRangeKey", abvRange2.minABV.value, abvRange2.maxABV.value)}",
//    taxType
//  )
//}