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

import config.Constants.ukTimeZoneStringId
import play.api.i18n.Messages
import uk.gov.hmrc.play.language.LanguageUtils

import java.time.{Instant, LocalDate, LocalTime, YearMonth, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.TimeZone
import javax.inject.Inject

class DateTimeHelper @Inject() (languageUtils: LanguageUtils) {
  private val ukTimeZone: ZoneId = TimeZone.getTimeZone(ukTimeZoneStringId).toZoneId

  private val hourMinuteFormatter = DateTimeFormatter.ofPattern("h:mm")
  private val meridiemFormatter   = DateTimeFormatter.ofPattern("a")

  def instantToLocalDate(instant: Instant): LocalDate = LocalDate.ofInstant(instant, ukTimeZone)
  def instantToLocalTime(instant: Instant): LocalTime = LocalTime.ofInstant(instant, ukTimeZone)

  def formatDateMonthYear(localDate: LocalDate)(implicit messages: Messages): String =
    languageUtils.Dates.formatDate(localDate)

  // Workaround as LanguageUtils doesn't support YearMonth nor expose formatter
  def formatMonthYear(yearMonth: YearMonth)(implicit messages: Messages): String = {
    val localDate = LocalDate.of(yearMonth.getYear, yearMonth.getMonth, 1)
    formatDateMonthYear(localDate).drop(2)
  }

  /**
    * ante/post meridiem (am/pm) is locale specific, thus convert to lowercase
    */
  def formatHourMinuteMeridiem(localTime: LocalTime): String = {
    val formattedTime = hourMinuteFormatter.format(localTime)
    val meridiem      = meridiemFormatter.format(localTime).toLowerCase()
    s"$formattedTime$meridiem"
  }
}
