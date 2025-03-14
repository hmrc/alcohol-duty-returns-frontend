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

import base.SpecBase
import play.api.i18n.Messages

import java.time.{Instant, LocalDate, LocalTime, YearMonth}
import java.time.format.DateTimeFormatter

class DateTimeHelperSpec extends SpecBase {
  "DateTimeHelper" - {
    "must convert an Instant to a LocalDate" in new SetUp {
      DateTimeFormatter.ISO_LOCAL_DATE.format(
        dateTimeHelper.instantToLocalDate(Instant.now(clock))
      ) mustBe "2024-06-11"
    }

    "must convert an Instant to a LocalTime" in new SetUp {
      DateTimeFormatter.ISO_LOCAL_TIME.format(
        dateTimeHelper.instantToLocalTime(Instant.now(clock))
      ) mustBe "16:07:47.838"
    }

    "format a LocalDate to the format 'day month year'" in new SetUp {
      dateTimeHelper.formatDateMonthYear(
        LocalDate.now(clock)
      ) mustBe "11 June 2024"
    }

    "format a YearMonth to the format 'month year'" in new SetUp {
      dateTimeHelper.formatMonthYear(YearMonth.now(clock)) mustBe "June 2024"
    }

    "format a LocalTime to the format 'hour:minuteam/pm'" in new SetUp {
      dateTimeHelper.formatHourMinuteMeridiem(
        LocalTime.now(clock)
      ) mustBe "4:07pm"
    }

    // regression
    "format a LocalTime to the format 'hour:minuteam/pm' when the hour is midday" in new SetUp {
      dateTimeHelper.formatHourMinuteMeridiem(
        LocalTime.now(clock).minusHours(4)
      ) mustBe "12:07pm"
    }
  }

  class SetUp {
    implicit val messages: Messages = getMessages(app)
    val dateTimeHelper              = createDateTimeHelper()
  }
}
