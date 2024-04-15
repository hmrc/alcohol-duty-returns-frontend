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

import java.time.{LocalDate, YearMonth}
import java.time.format.DateTimeFormatter

case class ReturnPeriod(periodKey: String, yearMonth: YearMonth) {
  def firstDateViewString(): String = ReturnPeriod.toViewString(yearMonth.atDay(1))
  def lastDateViewString(): String  = ReturnPeriod.toViewString(yearMonth.atEndOfMonth)
}

object ReturnPeriod {
  private val viewDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

  def apply(periodKey: String, year: Int, month: Int): ReturnPeriod = ReturnPeriod(periodKey, YearMonth.of(year, month))

  private def toViewString(date: LocalDate) = viewDateFormatter.format(date)

  private def validatePeriodKey(key: String): Boolean = key match {
    case _ if key.length != 4      => false
    case _ if key.charAt(0) < '0'  => false
    case _ if key.charAt(0) > '9'  => false
    case _ if key.charAt(1) < '0'  => false
    case _ if key.charAt(1) > '9'  => false
    case _ if key.charAt(2) != 'A' => false
    case _ if key.charAt(3) < 'A'  => false
    case _ if key.charAt(3) > 'L'  => false
    case _                         => true
  }

  def fromPeriodKey(periodKey: String): Either[String, ReturnPeriod] =
    if (validatePeriodKey(periodKey)) {
      val year  = (periodKey.charAt(0) - '0') * 10 + (periodKey.charAt(1) - '0') + 2000
      val month = periodKey.charAt(3) - 'A' + 1
      Right(ReturnPeriod(periodKey, year, month))
    } else {
      Left(
        s"Period key should be 4 characters yyAc where yy is year, A is a literal A and c is month character A-L. Received $periodKey"
      )
    }

  implicit val format: Format[ReturnPeriod] = new Format[ReturnPeriod] {
    override def reads(json: JsValue): JsResult[ReturnPeriod] =
      json
        .validate[String]
        .map(
          fromPeriodKey(_) match {
            case Right(rp)   => rp
            case Left(error) => throw new IllegalArgumentException(error)
          }
        )

    override def writes(returnPeriod: ReturnPeriod): JsValue =
      JsString(returnPeriod.periodKey)
  }
}
