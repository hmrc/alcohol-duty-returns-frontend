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

import java.time.{LocalDate, YearMonth}
import java.time.format.DateTimeFormatter

case class ReturnPeriod(yearMonth: YearMonth) {
  def firstDateViewString(): String = ReturnPeriod.toViewString(yearMonth.atDay(1))
  def lastDateViewString(): String  = ReturnPeriod.toViewString(yearMonth.atEndOfMonth)
}

object ReturnPeriod {
  private val viewDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

  def apply(year: Int, month: Int): ReturnPeriod = ReturnPeriod(YearMonth.of(year, month))

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

  def fromPeriodKey(key: String): Either[String, ReturnPeriod] =
    if (validatePeriodKey(key)) {
      val year  = (key.charAt(0) - '0') * 10 + (key.charAt(1) - '0') + 2000
      val month = key.charAt(3) - 'A' + 1
      Right(ReturnPeriod(year, month))
    } else {
      Left("Period key should be 4 characters yyAc where yy is year, A is a literal A and c is month character A-L")
    }
}
