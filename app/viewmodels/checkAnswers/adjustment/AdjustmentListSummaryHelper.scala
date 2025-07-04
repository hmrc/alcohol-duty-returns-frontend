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

import config.Constants.{Css, rowsPerPage}
import models.UserAnswers
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import models.adjustment.AdjustmentType.Spoilt
import pages.adjustment.AdjustmentEntryListPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HeadCell, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewmodels._
import viewmodels.declareDuty.RateBandDescription
import viewmodels.govuk.summarylist._

object AdjustmentListSummaryHelper {

  def adjustmentEntryTable(userAnswers: UserAnswers, total: BigDecimal, pageNumber: Int)(implicit
    messages: Messages
  ): TableViewModel = {

    val adjustmentEntries: Seq[AdjustmentEntry] = getPaginatedAdjustmentEntries(userAnswers, pageNumber)
    TableViewModel(
      head = Seq(
        HeadCell(content = Text(messages("adjustmentEntryList.type")), classes = Css.oneQuarterCssClass),
        HeadCell(content = Text(messages("adjustmentEntryList.description"))),
        HeadCell(
          content = Text(messages("adjustmentEntryList.duty")),
          classes = s"${Css.textAlignRightCssClass}"
        ),
        HeadCell(
          content = Text(messages("adjustmentEntryList.action")),
          classes = Css.oneQuarterCssClass
        )
      ),
      rows = getAdjustmentEntryRows(adjustmentEntries, pageNumber),
      total = Some(adjustmentsTotal(total))
    )
  }

  private def getPaginatedAdjustmentEntries(userAnswers: UserAnswers, pageNumber: Int): Seq[AdjustmentEntry] = {
    val adjustmentEntries = userAnswers.get(AdjustmentEntryListPage).getOrElse(Seq.empty)
    val fromIndex         = (pageNumber - 1) * rowsPerPage
    val toIndex           = pageNumber * rowsPerPage
    adjustmentEntries.slice(fromIndex, toIndex)
  }

  private def getAdjustmentEntryRows(adjustmentEntries: Seq[AdjustmentEntry], pageNumber: Int)(implicit
    messages: Messages
  ): Seq[TableRowViewModel] =
    adjustmentEntries.zipWithIndex.map { case (adjustmentEntry, index) =>
      val adjustmentIndex     = (pageNumber - 1) * rowsPerPage + index
      val adjustmentType      = adjustmentEntry.adjustmentType.getOrElse(
        throw new RuntimeException("Couldn't fetch adjustment type value from user answers")
      )
      val adjustmentTypeLabel = messages(s"adjustmentType.checkYourAnswersLabel.$adjustmentType")
      val dutyValue           = if (adjustmentEntry.newDuty.isDefined) {
        adjustmentEntry.newDuty
      } else {
        adjustmentEntry.duty
      }
      val formattedDutyValue  =
        Money.format(dutyValue.getOrElse(throw new RuntimeException("Couldn't fetch duty value from user answers")))
      val description: Text   = getDescription(adjustmentEntry, adjustmentType)

      TableRowViewModel(
        cells = Seq(
          TableRow(Text(adjustmentTypeLabel)),
          TableRow(description),
          TableRow(
            content = Text(formattedDutyValue),
            classes = Css.textAlignRightCssClass
          )
        ),
        actions = Seq(
          TableRowActionViewModel(
            label = messages("site.change"),
            href = controllers.adjustment.routes.CheckYourAnswersController.onPageLoad(Some(adjustmentIndex)),
            visuallyHiddenText = Some(
              messages(s"adjustmentEntryList.change.$adjustmentType.hidden", formattedDutyValue)
            )
          ),
          TableRowActionViewModel(
            label = messages("site.remove"),
            href = controllers.adjustment.routes.DeleteAdjustmentController.onPageLoad(adjustmentIndex),
            visuallyHiddenText = Some(
              messages(s"adjustmentEntryList.remove.$adjustmentType.hidden", formattedDutyValue)
            )
          )
        )
      )
    }

  private def getDescription(adjustmentEntry: AdjustmentEntry, adjustmentType: AdjustmentType)(implicit
    messages: Messages
  ): Text = {
    val description = (adjustmentType, adjustmentEntry.spoiltRegime) match {
      case (Spoilt, Some(spoiltRegime)) => Text(messages(spoiltRegime.regimeMessageKey).capitalize)
      case _                            =>
        Text(
          RateBandDescription
            .toDescription(
              adjustmentEntry.rateBand.getOrElse(
                throw new RuntimeException("Couldn't fetch rateBand from user answers")
              ),
              None
            )
            .capitalize
        )
    }
    description
  }

  private def adjustmentsTotal(total: BigDecimal)(implicit messages: Messages): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        TotalsSummaryList.row(messages("adjustmentList.total"), Money.format(total))
      )
    )

}
