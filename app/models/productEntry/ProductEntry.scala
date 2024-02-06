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

package models.productEntry

import models.{AlcoholByVolume, AlcoholRegime}
import play.api.libs.json.{Json, OFormat}

case class ProductEntry(
  name: Option[String] = None,
  abv: Option[AlcoholByVolume] = None,
  volume: Option[BigDecimal] = None,
  draughtRelief: Option[Boolean] = None,
  smallProducerRelief: Option[Boolean] = None,
  taxCode: Option[String] = None,
  regime: Option[AlcoholRegime] = None,
  taxRate: Option[BigDecimal] = None,
  sprDutyRate: Option[BigDecimal] = None,
  duty: Option[BigDecimal] = None,
  pureAlcoholVolume: Option[BigDecimal] = None
) {
  def isComplete: Boolean      = true
  def rate: Option[BigDecimal] = (taxRate, sprDutyRate) match {
    case (Some(_), None) => taxRate
    case (None, Some(_)) => sprDutyRate
    case _               => None
  }
}

object ProductEntry {
  implicit val formats: OFormat[ProductEntry] = Json.format[ProductEntry]
}
