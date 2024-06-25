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

import java.time.{Instant, LocalDateTime, YearMonth}
import java.time.format.DateTimeFormatter

class DateTimeHelperSpec extends SpecBase {
  "DateTimeHelper" - {
    "should convert an Instant to a LocalDateTime" in {
      DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(
        new DateTimeHelper().instantToDateTime(Instant.now(clock))
      ) mustBe "2024-06-11T16:07:47.838"
    }

    "format a LocalDateTime to the format 'day month year at hour:minuteam/pm'" in {
      new DateTimeHelper().formatDateMonthYearAtHourMinuteMerediem(
        LocalDateTime.now(clock)
      ) mustBe "11 June 2024 at 3:07pm"
    }

    "format a YearMonth to the format 'month year'" in {
      new DateTimeHelper().formatMonthYear(YearMonth.now(clock)) mustBe "June 2024"
    }
  }
}
