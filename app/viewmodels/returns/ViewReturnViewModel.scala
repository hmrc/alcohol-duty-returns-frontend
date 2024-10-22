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

package viewmodels.returns

import config.Constants
import models.returns._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HeadCell, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewmodels.{Money, TableRowViewModel, TableTotalViewModel, TableViewModel}

import javax.inject.Inject

class ViewReturnViewModel @Inject() () {
  def createAlcoholDeclaredViewModel(returnDetails: ReturnDetails)(implicit messages: Messages): TableViewModel = {
    val rows = returnDetails.alcoholDeclared.alcoholDeclaredDetails.toSeq.flatten

    if (rows.nonEmpty) {
      TableViewModel(
        head = alcoholDeclaredTableHeader(),
        rows = alcoholDeclaredRows(rows.sorted),
        total = Some(dutyToDeclareTotal(returnDetails.alcoholDeclared))
      )
    } else {
      TableViewModel(head = noAlcoholDeclaredTableHeader(), rows = nilDeclarationRow(), total = None)
    }
  }

  private def alcoholDeclaredTableHeader()(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(content = Text(messages("viewReturn.table.description.legend")), classes = Constants.oneQuarterCssClass),
      HeadCell(content = Text(messages("viewReturn.table.lpa.legend")), classes = Constants.oneQuarterCssClass),
      HeadCell(content = Text(messages("viewReturn.table.dutyRate.legend")), classes = Constants.oneQuarterCssClass),
      HeadCell(
        content = Text(messages("viewReturn.table.dutyDue.legend")),
        classes = s"${Constants.oneQuarterCssClass} ${Constants.textAlignRightCssClass}"
      )
    )

  private def noAlcoholDeclaredTableHeader()(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(
        content = Text(messages("viewReturn.table.description.legend")),
        classes = Constants.threeQuartersCssClass
      ),
      HeadCell(
        content = Text(messages("viewReturn.table.dutyDue.legend")),
        classes = s"${Constants.oneQuarterCssClass} ${Constants.textAlignRightCssClass}"
      )
    )

  private def alcoholDeclaredRows(
    alcoholDeclaredDetails: Seq[ReturnAlcoholDeclaredRow]
  )(implicit messages: Messages): Seq[TableRowViewModel] =
    alcoholDeclaredDetails.map { alcoholDeclaredDetailsRow =>
      TableRowViewModel(
        cells = Seq(
          TableRow(content = Text(alcoholDeclaredDetailsRow.taxType)),
          TableRow(content =
            Text(
              s"${messages("site.4DP", alcoholDeclaredDetailsRow.litresOfPureAlcohol)} ${messages("site.unit.litre.unit")}"
            )
          ),
          TableRow(content =
            Text(
              s"${Money.format(alcoholDeclaredDetailsRow.dutyRate)} ${messages("site.unit.per.litre")}"
            )
          ),
          TableRow(
            content = Text(Money.format(alcoholDeclaredDetailsRow.dutyValue)),
            classes = Constants.textAlignRightCssClass
          )
        )
      )
    }

  private def nilDeclarationRow()(implicit messages: Messages): Seq[TableRowViewModel] =
    Seq(
      TableRowViewModel(cells =
        Seq(
          TableRow(content = Text(messages("viewReturn.alcoholDuty.noneDeclared"))),
          TableRow(content = Text(messages("site.nil")), classes = Constants.textAlignRightCssClass)
        )
      )
    )

  private def dutyToDeclareTotal(alcoholDeclared: ReturnAlcoholDeclared)(implicit
    messages: Messages
  ): TableTotalViewModel =
    TableTotalViewModel(
      HeadCell(
        content = Text(messages("viewReturn.alcoholDuty.total.legend")),
        classes = Constants.threeQuartersCssClass
      ),
      HeadCell(
        content = Text(Money.format(alcoholDeclared.total)),
        classes = s"${Constants.oneQuarterCssClass} ${Constants.textAlignRightCssClass}"
      )
    )

  def createAdjustmentsViewModel(returnDetails: ReturnDetails)(implicit messages: Messages): TableViewModel = {
    val rows = returnDetails.adjustments.adjustmentDetails.toSeq.flatten

    if (rows.nonEmpty) {
      TableViewModel(
        head = adjustmentsTableHeader(),
        rows = adjustmentRows(rows.sorted),
        total = Some(adjustmentsTotal(returnDetails.adjustments))
      )

    } else {
      TableViewModel(head = noAdjustmentsTableHeader(), rows = nilAdjustmentsRow(), total = None)
    }
  }

  private def adjustmentsTableHeader()(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(
        content = Text(messages("viewReturn.table.adjustmentType.legend"))
      ),
      HeadCell(content = Text(messages("viewReturn.table.description.legend"))),
      HeadCell(content = Text(messages("viewReturn.table.lpa.legend"))),
      HeadCell(content = Text(messages("viewReturn.table.dutyRate.legend"))),
      HeadCell(
        content = Text(messages("viewReturn.table.dutyValue.legend")),
        classes = Constants.textAlignRightCssClass
      )
    )

  private def noAdjustmentsTableHeader()(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(
        content = Text(messages("viewReturn.table.description.legend")),
        classes = Constants.threeQuartersCssClass
      ),
      HeadCell(
        content = Text(messages("viewReturn.table.dutyValue.legend")),
        classes = s"${Constants.oneQuarterCssClass} ${Constants.textAlignRightCssClass}"
      )
    )

  private def adjustmentRows(
    returnAdjustments: Seq[ReturnAdjustmentsRow]
  )(implicit messages: Messages): Seq[TableRowViewModel] =
    returnAdjustments.map { returnAdjustmentsRow =>
      TableRowViewModel(
        cells = Seq(
          TableRow(content = Text(messages(s"viewReturn.adjustments.type.${returnAdjustmentsRow.adjustmentTypeKey}"))),
          TableRow(content = Text(returnAdjustmentsRow.taxType)),
          TableRow(content =
            Text(
              s"${messages("site.4DP", returnAdjustmentsRow.litresOfPureAlcohol)} ${messages("site.unit.litre.unit")}"
            )
          ),
          TableRow(content =
            Text(s"${Money.format(returnAdjustmentsRow.dutyRate)} ${messages("site.unit.per.litre")}")
          ),
          TableRow(
            content = Text(Money.format(returnAdjustmentsRow.dutyValue)),
            classes = Constants.textAlignRightCssClass
          )
        )
      )
    }

  private def nilAdjustmentsRow()(implicit messages: Messages): Seq[TableRowViewModel] =
    Seq(
      TableRowViewModel(cells =
        Seq(
          TableRow(content = Text(messages("viewReturn.adjustments.noneDeclared"))),
          TableRow(content = Text(messages("site.nil")), classes = Constants.textAlignRightCssClass)
        )
      )
    )

  private def adjustmentsTotal(adjustments: ReturnAdjustments)(implicit messages: Messages): TableTotalViewModel =
    TableTotalViewModel(
      HeadCell(
        content = Text(messages("viewReturn.adjustments.total.legend")),
        classes = Constants.threeQuartersCssClass
      ),
      HeadCell(
        content = Text(Money.format(adjustments.total)),
        classes = s"${Constants.oneQuarterCssClass} ${Constants.textAlignRightCssClass}"
      )
    )

  def createTotalDueViewModel(returnDetails: ReturnDetails)(implicit messages: Messages): TableTotalViewModel = {
    val content =
      if (
        returnDetails.totalDutyDue.totalDue == BigDecimal(0) &&
        returnDetails.alcoholDeclared.alcoholDeclaredDetails.toSeq.flatten.isEmpty &&
        returnDetails.adjustments.adjustmentDetails.toSeq.flatten.isEmpty
      ) {
        Text(messages("site.nil"))
      } else {
        Text(Money.format(returnDetails.totalDutyDue.totalDue))
      }

    TableTotalViewModel(
      HeadCell(
        content = Text(messages("viewReturn.dutyDue.total.legend")),
        classes = Constants.threeQuartersCssClass
      ),
      HeadCell(content = content, classes = s"${Constants.oneQuarterCssClass} ${Constants.textAlignRightCssClass}")
    )
  }
}
