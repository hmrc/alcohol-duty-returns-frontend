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

package viewmodels.declareDuty

import base.SpecBase
import models.ReturnPeriod
import viewmodels.ReturnPeriodViewModelFactory

class ReturnPeriodViewModelSpec extends SpecBase {

  "ReturnPeriodViewModelSpec" - {
    "must return a ReturnPeriodViewModel when apply method is called with a valid ReturnPeriod" in {
      implicit val messages = getMessages(app)
      val yearMonth         = arbitraryYearMonth.arbitrary.sample.get
      val returnPeriod      = ReturnPeriod(yearMonth)
      val result            = new ReturnPeriodViewModelFactory(createDateTimeHelper())(returnPeriod)
      val returnDueMonth    = returnPeriod.period.plusMonths(1)

      result.fromDate      mustBe createDateTimeHelper().formatDateMonthYear(yearMonth.atDay(1))
      result.toDate        mustBe createDateTimeHelper().formatDateMonthYear(yearMonth.atEndOfMonth())
      result.returnDueDate mustBe createDateTimeHelper().formatDateMonthYear(returnDueMonth.atDay(15))
    }
  }
}
