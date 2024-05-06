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

import play.api.libs.json.{Format, JsResult, JsString, JsValue}

import java.time.YearMonth
import scala.util.matching.Regex

case class ReturnPeriod(periodKey: String) {
  def toYearMonth: YearMonth = {
    val year  = periodKey.substring(0, 2).toInt + 2000
    val month = periodKey.charAt(3) - 'A' + 1
    YearMonth.of(year, month)
  }
}

object ReturnPeriod {

  def fromPeriodKey(periodKey: String): Option[ReturnPeriod] = {
    val returnPeriodPattern: Regex = """^\\d{2}A[A-L]$""".r
    periodKey match {
      case returnPeriodPattern(_) => Some(ReturnPeriod(periodKey))
      case _                      => None
    }
  }

  implicit val format: Format[ReturnPeriod] = new Format[ReturnPeriod] {
    override def reads(json: JsValue): JsResult[ReturnPeriod] =
      json
        .validate[String]
        .map(
          ReturnPeriod.fromPeriodKey(_) match {
            case Some(rp) => rp
            case None     => throw new IllegalArgumentException("Invalid format")
          }
        )

    override def writes(returnPeriod: ReturnPeriod): JsValue =
      JsString(returnPeriod.periodKey)
  }
}
