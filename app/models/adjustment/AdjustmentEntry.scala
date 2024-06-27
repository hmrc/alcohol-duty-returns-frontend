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

import models.{RateBand, YearMonthModelFormatter}
import play.api.libs.json.{Json, OFormat}

import java.time.YearMonth

case class AdjustmentEntry(
  index: Option[Int] = None,
  adjustmentType: Option[AdjustmentType] = None,
  rateBand: Option[RateBand] = None,
  repackagedRateBand: Option[RateBand] = None,
  totalLitresVolume: Option[BigDecimal] = None,
  pureAlcoholVolume: Option[BigDecimal] = None,
  period: Option[YearMonth] = None,
  sprDutyRate: Option[BigDecimal] = None,
  repackagedSprDutyRate: Option[BigDecimal] = None,
  duty: Option[BigDecimal] = None,
  repackagedDuty: Option[BigDecimal] = None,
  newDuty: Option[BigDecimal] = None
) {
  def isComplete: Boolean =
    adjustmentType.isDefined &&
      period.isDefined &&
      rateBand.isDefined &&
      //repackagedRateBand.isDefined &&
      totalLitresVolume.isDefined &&
      pureAlcoholVolume.isDefined &&
      (rateBand.flatMap(_.rate).isDefined || sprDutyRate.isDefined) &&
      //  (repackagedRateBand.flatMap(_.rate).isDefined || repackagedSprDutyRate.isDefined) && how to check
      duty.isDefined

  def rate: Option[BigDecimal] =
    (rateBand.flatMap(_.rate), sprDutyRate) match {
      case (Some(_), None) => rateBand.map(_.rate).get
      case (None, Some(_)) => sprDutyRate
      case _               => None
    }

  def repackagedRate: Option[BigDecimal] =
    (repackagedRateBand.flatMap(_.rate), repackagedSprDutyRate) match {
      case (Some(_), None) => repackagedRateBand.map(_.rate).get
      case (None, Some(_)) => repackagedSprDutyRate
      case _               => None
    }
}
object AdjustmentEntry extends YearMonthModelFormatter {
  implicit val formats: OFormat[AdjustmentEntry] = Json.format[AdjustmentEntry]
}
