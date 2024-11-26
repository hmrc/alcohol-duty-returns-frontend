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

package viewmodels

import java.time.{Instant, LocalDate, LocalTime, YearMonth, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.TimeZone
import javax.inject.Inject

class DateTimeHelper @Inject() () {
  private val ukTimeZone: ZoneId = TimeZone.getTimeZone("Europe/London").toZoneId

  private val monthYearFormatter     = DateTimeFormatter.ofPattern("LLLL yyyy")
  private val dateMonthYearFormatter = DateTimeFormatter.ofPattern("d LLLL yyyy")
  private val hourMinuteFormatter    = DateTimeFormatter.ofPattern("h:mm")
  private val meridiemFormat         = DateTimeFormatter.ofPattern("a")

  def instantToLocalDate(instant: Instant): LocalDate = LocalDate.ofInstant(instant, ukTimeZone)
  def instantToLocalTime(instant: Instant): LocalTime = LocalTime.ofInstant(instant.minusSeconds(3600 * 4), ukTimeZone)

  /**
    * ante/post meridiem (am/pm) is locale specific, thus convert to lowercase
    */
  def formatDateMonthYear(localDate: LocalDate): String =
    dateMonthYearFormatter.format(localDate)

  def formatHourMinuteMeridiem(localTime: LocalTime): String = {
    val formattedTime = hourMinuteFormatter.format(localTime)
    val meridiem      = meridiemFormat.format(localTime).toLowerCase()
    s"$formattedTime$meridiem"
  }

  def formatMonthYear(yearMonth: YearMonth): String = monthYearFormatter.format(yearMonth)
}
