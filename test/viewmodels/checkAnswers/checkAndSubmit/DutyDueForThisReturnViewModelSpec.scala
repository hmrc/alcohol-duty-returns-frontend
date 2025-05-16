/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.checkAnswers.checkAndSubmit

import base.SpecBase
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList

class DutyDueForThisReturnViewModelSpec extends SpecBase {

  val emptySummaryList            = SummaryList()
  implicit val messages: Messages = getMessages(app)

  "DutyDueForThisReturnViewModel" - {
    "getTitle should get the nil version of the title when total is zero" in {
      val testViewModel = DutyDueForThisReturnViewModel(emptySummaryList, emptySummaryList, totalDue = 0)

      testViewModel.getTitle mustBe messages("dutyDueForThisReturn.nil.title")
    }

    "getTitle should get the normal version of the title when total is above zero" in {
      val testViewModel = DutyDueForThisReturnViewModel(emptySummaryList, emptySummaryList, totalDue = 1)

      testViewModel.getTitle mustBe messages("dutyDueForThisReturn.title", messages("site.currency.2DP", 1))
    }

    "getTitle should get the normal version of the title when total is below zero" in {
      val testViewModel = DutyDueForThisReturnViewModel(emptySummaryList, emptySummaryList, totalDue = -1)

      testViewModel.getTitle mustBe messages("dutyDueForThisReturn.title", messages("site.currency.2DP", -1))
    }
  }

  "isTotalDueZero" - {
    "should return true when the total due is 0" in {
      val testViewModel = DutyDueForThisReturnViewModel(emptySummaryList, emptySummaryList, totalDue = 0)

      testViewModel.isTotalDueZero mustBe true
    }

    "should return false when the total due is not zero" in {
      val testViewModel = DutyDueForThisReturnViewModel(emptySummaryList, emptySummaryList, totalDue = 3)

      testViewModel.isTotalDueZero mustBe false
    }
  }

  "isTotalDueAboveZero" - {
    "should return true when the total due is above zero" in {
      val testViewModel = DutyDueForThisReturnViewModel(emptySummaryList, emptySummaryList, totalDue = 1)

      testViewModel.isTotalDueAboveZero mustBe true
    }

    "should return false when the total due is zero or less" in {
      val testViewModel = DutyDueForThisReturnViewModel(emptySummaryList, emptySummaryList, totalDue = 0)

      testViewModel.isTotalDueAboveZero mustBe false
    }
  }

  "isTotalDueBelowZero" - {
    "should return true when the total due is below zero" in {
      val testViewModel = DutyDueForThisReturnViewModel(emptySummaryList, emptySummaryList, totalDue = -1)

      testViewModel.isTotalDueBelowZero mustBe true
    }

    "should return false when the total due is zero or more" in {
      val testViewModel = DutyDueForThisReturnViewModel(emptySummaryList, emptySummaryList, totalDue = 0)

      testViewModel.isTotalDueBelowZero mustBe false
    }
  }

}
