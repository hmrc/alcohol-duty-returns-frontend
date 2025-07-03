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
import models.adjustment.AdjustmentType.Spoilt
import models.{CheckMode, YearMonthModelFormatter}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.DateTimeHelper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import javax.inject.Inject

class AdjustmentReturnPeriodSummary @Inject() (dateTimeHelper: DateTimeHelper) extends YearMonthModelFormatter {

  def row(adjustmentEntry: AdjustmentEntry)(implicit messages: Messages): Option[SummaryListRow] =
    (adjustmentEntry.period, adjustmentEntry.adjustmentType) match {
      case (Some(period), Some(adjustmentType)) if !adjustmentType.equals(Spoilt) =>
        Some(
          SummaryListRowViewModel(
            key = "whenDidYouPayDuty.checkYourAnswersLabel",
            value = ValueViewModel(HtmlContent(dateTimeHelper.formatMonthYear(period))),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                controllers.adjustment.routes.AdjustmentReturnPeriodController.onPageLoad(CheckMode).url
              )
                .withVisuallyHiddenText(messages("whenDidYouPayDuty.change.hidden"))
            )
          )
        )
      case (_, _)                                                                 => None
    }

}
