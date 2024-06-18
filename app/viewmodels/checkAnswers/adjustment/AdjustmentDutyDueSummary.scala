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

package viewmodels.checkAnswers.adjustment

import models.adjustment.AdjustmentEntry
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import views.ViewUtils.valueFormatter

object AdjustmentDutyDueSummary {
  def row(adjustmentEntry: AdjustmentEntry)(implicit messages: Messages): Option[SummaryListRow] = {
    val dutyValue = if (adjustmentEntry.newDuty.isDefined) {
      adjustmentEntry.newDuty
    } else {
      adjustmentEntry.duty
    }
    Some(
      SummaryListRowViewModel(
        key = "adjustmentDutyDue.duty.checkYourAnswersLabel",
        value = ValueViewModel(
          valueFormatter(dutyValue.getOrElse(throw new RuntimeException("Couldn't fetch duty value from cache")))
        )
      )
    )
  }
}
