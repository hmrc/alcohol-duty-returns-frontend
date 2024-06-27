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

import models.UserAnswers
import models.adjustment.AdjustmentEntry
import pages.adjustment.AdjustmentEntryListPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HeadCell, Text}
import viewmodels.checkAnswers.returns.RateBandHelper.rateBandContent
import viewmodels.{TableRowActionViewModel, TableRowViewModel, TableViewModel}
import views.ViewUtils.valueFormatter

object AdjustmentListSummaryHelper {

  def adjustmentEntryTable(userAnswers: UserAnswers)(implicit messages: Messages): TableViewModel = {

    val adjustmentEntries: Seq[AdjustmentEntry] = getAdjustmentEntries(userAnswers)
    TableViewModel(
      head = Seq(
        HeadCell(content = Text(messages("adjustmentEntryList.type")), classes = "govuk-!-width-one-quarter"),
        HeadCell(content = Text(messages("adjustmentEntryList.description")), classes = "govuk-!-width-one-quarter"),
        HeadCell(content = Text(messages("adjustmentEntryList.duty")), classes = "govuk-!-width-one-quarter"),
        HeadCell(content = Text(messages("adjustmentEntryList.action")), classes = "govuk-!-width-one-quarter")
      ),
      rows = getAdjustmentEntryRows(adjustmentEntries),
      total = adjustmentEntries.map(_.duty.getOrElse(BigDecimal(0))).sum//how to sum with newDuty
    )
  }

  private def getAdjustmentEntries(userAnswers: UserAnswers): Seq[AdjustmentEntry] =
    userAnswers.get(AdjustmentEntryListPage).getOrElse(Seq.empty)

  private def getAdjustmentEntryRows(adjustmentEntries: Seq[AdjustmentEntry])(implicit
                                                                     messages: Messages
  ): Seq[TableRowViewModel] =
    adjustmentEntries.zipWithIndex.map { case (adjustmentEntry, index) =>
      val adjustmentType = adjustmentEntry.adjustmentType.getOrElse(throw new RuntimeException("Couldn't fetch adjustment type value from cache"))
      val dutyValue = if (adjustmentEntry.newDuty.isDefined) {
        adjustmentEntry.newDuty
      } else {
        adjustmentEntry.duty
      }
      TableRowViewModel(
        cells = Seq(
          Text(messages(s"adjustmentType.$adjustmentType")),
          Text(rateBandContent(adjustmentEntry.rateBand.getOrElse(throw new RuntimeException("Couldn't fetch rateBandfrom cache")), "adjustmentTaxType.checkYourAnswersLabel")),
          Text(valueFormatter(dutyValue.getOrElse(throw new RuntimeException("Couldn't fetch duty value from cache"))))
        ),
        actions = Seq(
          TableRowActionViewModel(
            label = messages("site.change"),
            href = controllers.adjustment.routes.CheckYourAnswersController.onPageLoad(Some(index)),
            visuallyHiddenText = Some(messages("adjustmentEntryList.change.hidden"))
          ),
          TableRowActionViewModel(
            label = messages("site.remove"),
            href = controllers.adjustment.routes.DeleteAdjustmentController.onPageLoad(index: Int),
            visuallyHiddenText = Some(messages("adjustmentEntryList.remove.hidden"))
          )
        )
      )
    }

}
