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

package models

import enumeratum.{Enum, EnumEntry, PlayEnum, PlayJsonEnum}
import play.api.libs.json.{Format, JsPath, Json, OFormat}
import queries.{Gettable, Settable}

import java.time.LocalDate

sealed trait ObligationStatus extends EnumEntry

object ObligationStatus extends Enum[ObligationStatus] with PlayJsonEnum[ObligationStatus] {
  val values = findValues

  case object Open extends ObligationStatus
  case object Fulfilled extends ObligationStatus
}

case class ObligationData(
  status: ObligationStatus,
  fromDate: LocalDate,
  toDate: LocalDate,
  dueDate: LocalDate,
  periodKey: String
)

object ObligationData extends Gettable[ObligationData] with Settable[ObligationData] {
  implicit val format: Format[ObligationData] = Json.format[ObligationData]

  override def toString     = "obligationData"
  override def path: JsPath = JsPath \ toString
}

case class FulfilledObligations(
  year: Int,
  obligations: Seq[ObligationData]
)

object FulfilledObligations {
  implicit val fulfilledObligationsFormat: OFormat[FulfilledObligations] = Json.format[FulfilledObligations]
}

sealed trait ObligationStatusToDisplay extends EnumEntry

object ObligationStatusToDisplay extends PlayEnum[ObligationStatusToDisplay] {
  val values = findValues

  case object Due extends ObligationStatusToDisplay
  case object Overdue extends ObligationStatusToDisplay
  case object Completed extends ObligationStatusToDisplay
}
