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

import java.time.{Instant, LocalDateTime, YearMonth, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.TimeZone
import javax.inject.Inject

class DateTimeHelper @Inject() () {
  private val ukTimeZone: ZoneId = TimeZone.getTimeZone("Europe/London").toZoneId

  private val monthYearFormatter                      = DateTimeFormatter.ofPattern("LLLL yyyy")
  private val dateMonthYearAtHourMinuteMerediemFormat = DateTimeFormatter.ofPattern("dd LLLL yyyy 'at' K:mma")

  def instantToDateTime(instant: Instant): LocalDateTime = LocalDateTime.ofInstant(instant, ukTimeZone)

  def formatDateMonthYearAtHourMinuteMerediem(localDateTime: LocalDateTime): String =
    dateMonthYearAtHourMinuteMerediemFormat.format(localDateTime)
  def formatMonthYear(yearMonth: YearMonth): String                                 = monthYearFormatter.format(yearMonth)
}
