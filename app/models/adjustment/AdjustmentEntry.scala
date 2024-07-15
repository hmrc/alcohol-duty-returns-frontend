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

package models.adjustment

import models.adjustment.AdjustmentType.RepackagedDraughtProducts
import models.{RateBand, YearMonthModelFormatter}
import play.api.libs.json.{Json, OFormat}

import java.time.YearMonth

case class AdjustmentEntry(
  index: Option[Int] = None,
  adjustmentType: Option[AdjustmentType] = None,
  period: Option[YearMonth] = None,
  rateBand: Option[RateBand] = None,
  repackagedRateBand: Option[RateBand] = None,
  totalLitresVolume: Option[BigDecimal] = None,
  pureAlcoholVolume: Option[BigDecimal] = None,
  sprDutyRate: Option[BigDecimal] = None,
  repackagedSprDutyRate: Option[BigDecimal] = None,
  duty: Option[BigDecimal] = None,
  repackagedDuty: Option[BigDecimal] = None,
  newDuty: Option[BigDecimal] = None
) {
  def isComplete: Boolean = {
    val isRepackagedAdjustment = adjustmentType.isDefined && adjustmentType.equals(RepackagedDraughtProducts)

    adjustmentType.isDefined && period.isDefined && rateBand.isDefined &&
    totalLitresVolume.isDefined && pureAlcoholVolume.isDefined && duty.isDefined && (rateBand
      .flatMap(_.rate)
      .isDefined || sprDutyRate.isDefined) && (!isRepackagedAdjustment || (repackagedRateBand.isDefined &&
      (repackagedRateBand.flatMap(_.rate).isDefined || repackagedSprDutyRate.isDefined) && repackagedDuty.isDefined))
  }

  def rate: Option[BigDecimal] =
    (rateBand.flatMap(_.rate), sprDutyRate) match {
      case (Some(rate), None) => Some(rate)
      case (None, Some(_))    => sprDutyRate
      case _                  => None
    }

  def repackagedRate: Option[BigDecimal] =
    (repackagedRateBand.flatMap(_.rate), repackagedSprDutyRate) match {
      case (Some(repackagedRate), None) => Some(repackagedRate)
      case (None, Some(_))              => repackagedSprDutyRate
      case _                            => None
    }
}
object AdjustmentEntry extends YearMonthModelFormatter {
  implicit val formats: OFormat[AdjustmentEntry] = Json.format[AdjustmentEntry]
}
