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

import models.CheckMode
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.Spoilt
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.Money
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

class AdjustmentDutyDueSummary {
  def row(adjustmentEntry: AdjustmentEntry)(implicit messages: Messages): Option[SummaryListRow] = {
    val action = adjustmentEntry.adjustmentType match {
      case Some(Spoilt) =>
        Seq(
          ActionItemViewModel(
            "site.change",
            controllers.adjustment.routes.SpoiltVolumeWithDutyController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(messages("spoiltVolumeWithDuty.change.hidden"))
        )
      case _            => Seq.empty
    }

    val dutyValue = adjustmentEntry.newDuty.orElse(adjustmentEntry.duty)

    dutyValue.map(duty =>
      SummaryListRowViewModel(
        key = "adjustmentDutyDue.duty.checkYourAnswersLabel",
        value = ValueViewModel(Money.format(duty)),
        actions = action
      )
    )
  }
}
