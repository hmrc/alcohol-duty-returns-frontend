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

package viewmodels.checkAnswers.checkAndSubmit


import models.{AlcoholRegime, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.HeadCell
import viewmodels.{TableRowViewModel, TableViewModel}

object DutyDueForThisReturnHelper {

  def dutyDueByRegime(userAnswers: UserAnswers, regime: AlcoholRegime)(implicit messages: Messages): TableViewModel =
    TableViewModel(
      head = Seq(
        HeadCell(Text(messages("dutyCalculation.table.dutyDue"))),
        HeadCell(Text(messages("dutyCalculation.table.action")))
      ),
      rows = Seq.empty[TableRowViewModel],
      total = 0
    )
}
