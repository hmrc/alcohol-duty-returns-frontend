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

import config.Constants
import play.api.libs.json.{Format, JsResult, JsString, JsValue}

import java.time.{LocalDate, YearMonth}
import scala.util.matching.Regex

case class ReturnPeriod(period: YearMonth) {
  def toPeriodKey                 = s"${period.getYear - 2000}A${(period.getMonthValue + 64).toChar}"
  def periodFromDate(): LocalDate = period.atDay(1)
  def periodToDate(): LocalDate   = period.atEndOfMonth()

  def hasQuarterlySpirits: Boolean =
    Constants.quarterlySpiritsMonths.contains(period.getMonth)
}

object ReturnPeriod {
  val returnPeriodPattern: Regex = """^(\d{2}A[A-L])$""".r

  def fromPeriodKey(periodKey: String): Option[ReturnPeriod] =
    periodKey match {
      case returnPeriodPattern(_) =>
        val year  = periodKey.substring(0, 2).toInt + 2000
        val month = periodKey.charAt(3) - 'A' + 1
        Some(ReturnPeriod(YearMonth.of(year, month)))
      case _                      => None
    }

  def fromDateInPeriod(date: LocalDate): ReturnPeriod =
    ReturnPeriod(YearMonth.from(date))

  implicit val format: Format[ReturnPeriod] = new Format[ReturnPeriod] {
    override def reads(json: JsValue): JsResult[ReturnPeriod] =
      json
        .validate[String]
        .map(pk =>
          ReturnPeriod.fromPeriodKey(pk).getOrElse(throw new IllegalArgumentException(s"$pk is not a valid period key"))
        )

    override def writes(returnPeriod: ReturnPeriod): JsValue =
      JsString(returnPeriod.toPeriodKey)
  }
}
