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

import models.ReturnPeriod
import play.api.i18n.Messages

import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.warningtext.WarningText
import viewmodels.returns.ReturnPeriodViewModel.viewDateFormatter

import java.time.LocalDate

case class BeforeStartReturnViewModel(returnPeriod: ReturnPeriod, currentDate: LocalDate) {
  private val returnDueDate = returnPeriod.periodDueDate()
  def warningText(implicit messages: Messages): WarningText = {
    val message: String = if (currentDate.isBefore(returnDueDate)) {
      messages("beforeStartReturn.text.dueDateWarning", viewDateFormatter.format(returnDueDate))
    } else if (currentDate.isAfter(returnDueDate)) {
      messages("beforeStartReturn.text.overdueWarning", viewDateFormatter.format(returnDueDate))
    } else {
      messages("beforeStartReturn.text.dueWarning")
    }

    WarningText(
      iconFallbackText = Some("Warning"),
      content = Text(message)
    )

  }
}
