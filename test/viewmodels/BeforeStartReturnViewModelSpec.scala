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
import connectors.UserAnswersConnector
import models.ReturnPeriod
import play.api.inject.bind
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.warningtext.WarningText
import ReturnPeriodViewModel.viewDateFormatter

import java.time.LocalDate

class BeforeStartReturnViewModelSpec extends SpecBase {
  "WarningTextViewModel" - {
    val mockUserAnswersConnector = mock[UserAnswersConnector]

    "should show the correct message when currentDate is before returnDueDate" in {
      val currentDate  = LocalDate.of(2024, 1, 14)
      val returnPeriod = ReturnPeriod.fromPeriodKey("23AL").get
      val viewModel    = BeforeStartReturnViewModel(returnPeriod, currentDate)

      val application = applicationBuilder()
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val messages = getMessages(application)
        val result   = viewModel.warningText(messages)
        result mustBe
          WarningText(
            iconFallbackText = Some("Warning"),
            content = Text(
              messages(s"beforeStartReturn.text.dueDateWarning", viewDateFormatter.format(returnPeriod.periodDueDate()))
            ),
            classes = ""
          )

      }
    }

    "should show the correct message when currentDate is the same as returnDueDate" in {
      val currentDate  = LocalDate.of(2024, 1, 15)
      val returnPeriod = ReturnPeriod.fromPeriodKey("23AL").get
      val viewModel    = BeforeStartReturnViewModel(returnPeriod, currentDate)

      val application = applicationBuilder()
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val messages = getMessages(application)
        val result   = viewModel.warningText(messages)
        result mustBe
          WarningText(
            iconFallbackText = Some("Warning"),
            content = Text(messages("beforeStartReturn.text.dueWarning")),
            classes = ""
          )

      }
    }

    "should show the correct message when currentDate is after returnDueDate" in {
      val currentDate  = LocalDate.of(2024, 1, 16)
      val returnPeriod = ReturnPeriod.fromPeriodKey("23AL").get
      val viewModel    = BeforeStartReturnViewModel(returnPeriod, currentDate)

      val application = applicationBuilder()
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val messages = getMessages(application)
        val result   = viewModel.warningText(messages)
        result mustBe
          WarningText(
            iconFallbackText = Some("Warning"),
            content = Text(
              messages("beforeStartReturn.text.overdueWarning", viewDateFormatter.format(returnPeriod.periodDueDate()))
            ),
            classes = ""
          )

      }
    }
  }
}
