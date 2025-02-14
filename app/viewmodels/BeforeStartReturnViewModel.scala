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

import java.time.LocalDate
import javax.inject.Inject

case class BeforeStartReturnViewModel(returnDueDate: LocalDate, dueDateString: String, currentDate: LocalDate) {
  def warningText(implicit messages: Messages): WarningText = {
    val message: String = if (currentDate.isBefore(returnDueDate)) {
      messages("beforeStartReturn.text.dueDateWarning", dueDateString)
    } else if (currentDate.isAfter(returnDueDate)) {
      messages("beforeStartReturn.text.overdueWarning", dueDateString)
    } else {
      messages("beforeStartReturn.text.dueWarning")
    }

    WarningText(
      iconFallbackText = Some(messages("site.warning")),
      content = Text(message)
    )
  }
}

class BeforeStartReturnViewModelFactory @Inject() (dateTimeHelper: DateTimeHelper) {
  def apply(returnPeriod: ReturnPeriod, currentDate: LocalDate)(implicit
    messages: Messages
  ): BeforeStartReturnViewModel = {
    val returnDueDate = returnPeriod.periodDueDate()

    BeforeStartReturnViewModel(returnDueDate, dateTimeHelper.formatDateMonthYear(returnDueDate), currentDate)
  }
}
