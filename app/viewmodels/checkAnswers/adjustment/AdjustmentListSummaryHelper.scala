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

import config.Constants
import config.Constants.rowsPerPage
import models.UserAnswers
import models.adjustment.AdjustmentEntry
import pages.adjustment.AdjustmentEntryListPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HeadCell, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewmodels.returns.RateBandHelper.rateBandRecap
import viewmodels.{Money, TableRowActionViewModel, TableRowViewModel, TableTotalViewModel, TableViewModel}

object AdjustmentListSummaryHelper {

  def adjustmentEntryTable(userAnswers: UserAnswers, total: BigDecimal, pageNumber: Int)(implicit
    messages: Messages
  ): TableViewModel = {

    val adjustmentEntries: Seq[AdjustmentEntry] = getPaginatedAdjustmentEntries(userAnswers, pageNumber)
    TableViewModel(
      head = Seq(
        HeadCell(content = Text(messages("adjustmentEntryList.type")), classes = Constants.oneQuarterCssClass),
        HeadCell(content = Text(messages("adjustmentEntryList.description")), classes = Constants.oneQuarterCssClass),
        HeadCell(
          content = Text(messages("adjustmentEntryList.duty")),
          classes = s"${Constants.oneQuarterCssClass} ${Constants.textAlignRightCssClass}"
        ),
        HeadCell(
          content = Text(messages("adjustmentEntryList.action")),
          classes = Constants.oneQuarterCssClass
        )
      ),
      rows = getAdjustmentEntryRows(adjustmentEntries),
      total = Some(adjustmentsTotal(total))
    )
  }

  private def getPaginatedAdjustmentEntries(userAnswers: UserAnswers, pageNumber: Int): Seq[AdjustmentEntry] = {
    val adjustmentEntries          = userAnswers.get(AdjustmentEntryListPage).getOrElse(Seq.empty)
    val fromIndex = (pageNumber - 1) * rowsPerPage
    val toIndex = pageNumber * rowsPerPage
    adjustmentEntries.slice(fromIndex, toIndex)
  }

  private def getAdjustmentEntryRows(adjustmentEntries: Seq[AdjustmentEntry])(implicit
    messages: Messages
  ): Seq[TableRowViewModel] =
    adjustmentEntries.zipWithIndex.map { case (adjustmentEntry, index) =>
      val adjustmentType = adjustmentEntry.adjustmentType.getOrElse(
        throw new RuntimeException("Couldn't fetch adjustment type value from cache")
      )
      val dutyValue      = if (adjustmentEntry.newDuty.isDefined) {
        adjustmentEntry.newDuty
      } else {
        adjustmentEntry.duty
      }
      TableRowViewModel(
        cells = Seq(
          TableRow(Text(messages(s"adjustmentType.checkYourAnswersLabel.$adjustmentType"))),
          TableRow(
            Text(
              rateBandRecap(
                adjustmentEntry.rateBand.getOrElse(throw new RuntimeException("Couldn't fetch rateBand from cache"))
              )
            )
          ),
          TableRow(
            content = Text(
              Money.format(dutyValue.getOrElse(throw new RuntimeException("Couldn't fetch duty value from cache")))
            ),
            classes = Constants.textAlignRightCssClass
          )
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

  private def adjustmentsTotal(total: BigDecimal)(implicit
    messages: Messages
  ): TableTotalViewModel =
    TableTotalViewModel(
      HeadCell(
        content = Text(messages("adjustmentList.total")),
        classes = Constants.threeQuartersCssClass
      ),
      HeadCell(
        content = Text(Money.format(total)),
        classes = s"${Constants.oneQuarterCssClass} ${Constants.textAlignRightCssClass}"
      )
    )

}
