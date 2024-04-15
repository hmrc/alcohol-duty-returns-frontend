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

import enumeratum._
import play.api.libs.json.{Format, Json}

case class Return(returnPeriod: ReturnPeriod, appaId: String)

object Return {
  implicit val format: Format[Return] = Json.format[Return]
}

sealed trait ReturnError extends EnumEntry
object ReturnError extends Enum[ReturnError] {
  val values = findValues

  case object ReturnLocked extends ReturnError
  case object ReturnNotFound extends ReturnError
  case object ReturnParsingError extends ReturnError
  case object ReturnUpstreamError extends ReturnError
}
