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
import models.{RateBand, ReturnPeriod}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HeadCell, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewmodels.declareDuty.RateBandHelper.rateBandRecap
import viewmodels.{Money, TableRowViewModel, TableTotalViewModel, TableViewModel}

import java.time.YearMonth
import javax.inject.Inject

class ViewReturnViewModel @Inject() () {
  def createAlcoholDeclaredViewModel(
    returnDetails: ReturnDetails,
    ratePeriodsAndTaxCodesToRateBands: Map[(YearMonth, String), RateBand]
  )(implicit messages: Messages): TableViewModel = {
    val rows = returnDetails.alcoholDeclared.alcoholDeclaredDetails.toSeq.flatten

    if (rows.nonEmpty) {
      TableViewModel(
        head = alcoholDeclaredTableHeader(),
        rows =
          alcoholDeclaredRows(returnDetails.identification.periodKey, rows.sorted, ratePeriodsAndTaxCodesToRateBands),
        total = Some(dutyToDeclareTotal(returnDetails.alcoholDeclared))
      )
    } else {
      TableViewModel(head = noAlcoholDeclaredTableHeader(), rows = nilDeclarationRow(), total = None)
    }
  }

  private def alcoholDeclaredTableHeader()(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(content = Text(messages("viewReturn.table.description.legend"))),
      HeadCell(content = Text(messages("viewReturn.table.lpa.legend")), classes = Constants.textAlignRightWrapCssClass),
      HeadCell(
        content = Text(messages("viewReturn.table.dutyRate.legend")),
        classes = Constants.textAlignRightWrapCssClass
      ),
      HeadCell(
        content = Text(messages("viewReturn.table.dutyDue.legend")),
        classes = Constants.textAlignRightWrapCssClass
      )
    )

  private def noAlcoholDeclaredTableHeader()(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(
        content = Text(messages("viewReturn.table.description.legend"))
      ),
      HeadCell(
        content = Text(messages("viewReturn.table.dutyDue.legend")),
        classes = s"${Constants.textAlignRightCssClass} govuk-table__cell--numeric"
      )
    )

  private def alcoholDeclaredRows(
    periodKey: String,
    alcoholDeclaredDetails: Seq[ReturnAlcoholDeclaredRow],
    ratePeriodsAndTaxCodesToRateBands: Map[(YearMonth, String), RateBand]
  )(implicit messages: Messages): Seq[TableRowViewModel] = {
    val maybeRatePeriod: Option[YearMonth] = ReturnPeriod.fromPeriodKey(periodKey).map(_.period)
    alcoholDeclaredDetails.map { alcoholDeclaredDetailsRow =>
      val taxType: String = alcoholDeclaredDetailsRow.taxType
      TableRowViewModel(
        cells = Seq(
          TableRow(content =
            Text(getDescriptionOrFallbackToTaxTypeCode(ratePeriodsAndTaxCodesToRateBands, maybeRatePeriod, taxType))
          ),
          TableRow(
            content = Text(messages("site.4DP", alcoholDeclaredDetailsRow.litresOfPureAlcohol)),
            classes = s"${Constants.textAlignRightCssClass} govuk-table__cell--numeric"
          ),
          TableRow(
            content = Text(s"${Money.format(alcoholDeclaredDetailsRow.dutyRate)}"),
            classes = s"${Constants.textAlignRightCssClass} govuk-table__cell--numeric"
          ),
          TableRow(
            content = Text(Money.format(alcoholDeclaredDetailsRow.dutyValue)),
            classes = s"${Constants.textAlignRightCssClass} govuk-table__cell--numeric"
          )
        )
      )
    }
  }

  private def getDescriptionOrFallbackToTaxTypeCode(
    ratePeriodsAndTaxCodesToRateBands: Map[(YearMonth, String), RateBand],
    maybeRatePeriod: Option[YearMonth],
    taxType: String
  )(implicit messages: Messages): String =
    maybeRatePeriod
      .flatMap(ratePeriod => ratePeriodsAndTaxCodesToRateBands.get((ratePeriod, taxType)))
      .map(rateBandRecap(_))
      .getOrElse(taxType)

  private def nilDeclarationRow()(implicit messages: Messages): Seq[TableRowViewModel] =
    Seq(
      TableRowViewModel(cells =
        Seq(
          TableRow(content = Text(messages("viewReturn.alcoholDuty.noneDeclared"))),
          TableRow(
            content = Text(messages("site.nil")),
            classes = s"${Constants.textAlignRightCssClass} govuk-table__cell--numeric"
          )
        )
      )
    )

  private def dutyToDeclareTotal(alcoholDeclared: ReturnAlcoholDeclared)(implicit
    messages: Messages
  ): TableTotalViewModel =
    TableTotalViewModel(
      HeadCell(
        content = Text(messages("viewReturn.alcoholDuty.total.legend"))
      ),
      HeadCell(
        content = Text(Money.format(alcoholDeclared.total)),
        classes = s"${Constants.textAlignRightCssClass} govuk-table__cell--numeric"
      )
    )

  def createAdjustmentsViewModel(
    returnDetails: ReturnDetails,
    ratePeriodsAndTaxCodesToRateBands: Map[(YearMonth, String), RateBand]
  )(implicit messages: Messages): TableViewModel = {
    val rows = returnDetails.adjustments.adjustmentDetails.toSeq.flatten

    if (rows.nonEmpty) {
      TableViewModel(
        head = adjustmentsTableHeader(),
        rows = adjustmentRows(rows.sorted, ratePeriodsAndTaxCodesToRateBands),
        total = Some(adjustmentsTotal(returnDetails.adjustments))
      )
    } else {
      TableViewModel(head = noAdjustmentsTableHeader(), rows = nilAdjustmentsRow(), total = None)
    }
  }

  private def adjustmentsTableHeader()(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(content = Text(messages("viewReturn.table.adjustmentType.legend"))),
      HeadCell(content = Text(messages("viewReturn.table.description.legend"))),
      HeadCell(content = Text(messages("viewReturn.table.lpa.legend")), classes = Constants.textAlignRightWrapCssClass),
      HeadCell(
        content = Text(messages("viewReturn.table.dutyRate.legend")),
        classes = Constants.textAlignRightWrapCssClass
      ),
      HeadCell(
        content = Text(messages("viewReturn.table.dutyValue.legend")),
        classes = Constants.textAlignRightWrapCssClass
      )
    )

  private def noAdjustmentsTableHeader()(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(
        content = Text(messages("viewReturn.table.description.legend"))
      ),
      HeadCell(
        content = Text(messages("viewReturn.table.dutyValue.legend")),
        classes = s"${Constants.textAlignRightCssClass} govuk-table__cell--numeric"
      )
    )

  private def adjustmentRows(
    returnAdjustments: Seq[ReturnAdjustmentsRow],
    ratePeriodsAndTaxCodesToRateBands: Map[(YearMonth, String), RateBand]
  )(implicit messages: Messages): Seq[TableRowViewModel] =
    returnAdjustments.map { returnAdjustmentsRow =>
      val maybeRatePeriod = ReturnPeriod.fromPeriodKey(returnAdjustmentsRow.returnPeriodAffected).map(_.period)
      val taxType         = returnAdjustmentsRow.taxType
      TableRowViewModel(
        cells = Seq(
          TableRow(content = Text(messages(s"viewReturn.adjustments.type.${returnAdjustmentsRow.adjustmentTypeKey}"))),
          TableRow(content =
            Text(getDescriptionOrFallbackToTaxTypeCode(ratePeriodsAndTaxCodesToRateBands, maybeRatePeriod, taxType))
          ),
          TableRow(
            content = Text(messages("site.4DP", returnAdjustmentsRow.litresOfPureAlcohol)),
            classes = s"${Constants.textAlignRightCssClass} govuk-table__cell--numeric"
          ),
          TableRow(
            content = Text(Money.format(returnAdjustmentsRow.dutyRate)),
            classes = s"${Constants.textAlignRightCssClass} govuk-table__cell--numeric"
          ),
          TableRow(
            content = Text(Money.format(returnAdjustmentsRow.dutyValue)),
            classes = s"${Constants.textAlignRightCssClass} govuk-table__cell--numeric"
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
        content = Text(messages("viewReturn.adjustments.total.legend"))
      ),
      HeadCell(
        content = Text(Money.format(adjustments.total)),
        classes = s"${Constants.textAlignRightCssClass} govuk-table__cell--numeric"
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
        content = Text(messages("viewReturn.dutyDue.total.legend"))
      ),
      HeadCell(content = content, classes = s"${Constants.textAlignRightCssClass} govuk-table__cell--numeric")
    )
  }

  def createNetDutySuspensionViewModel(
    returnDetails: ReturnDetails
  )(implicit messages: Messages): TableViewModel =
    returnDetails.netDutySuspension match {
      case Some(netDutySuspension) =>
        TableViewModel(
          head = netDutySuspensionTableHeader(),
          rows = netDutySuspensionRows(netDutySuspension)
        )
      case None                    =>
        TableViewModel(
          head = noneDeclareDutySuspensionHeader(),
          rows = noneDeclaredDutySuspension()
        )
    }

  private def netDutySuspensionTableHeader()(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(
        content = Text(messages("viewReturn.table.description.legend"))
      ),
      HeadCell(
        content = Text(messages("viewReturn.table.totalVolume.legend")),
        classes = Constants.textAlignRightWrapCssClass
      ),
      HeadCell(
        content = Text(messages("viewReturn.table.lpa.legend")),
        classes = Constants.textAlignRightWrapCssClass
      )
    )

  private def netDutySuspensionRows(netDutySuspension: ReturnNetDutySuspension)(implicit
    messages: Messages
  ): Seq[TableRowViewModel] =
    Seq(
      netDutySuspensionCell(
        "return.regime.Beer",
        netDutySuspension.totalLtsBeer,
        netDutySuspension.totalLtsPureAlcoholBeer
      ),
      netDutySuspensionCell(
        "return.regime.Cider",
        netDutySuspension.totalLtsCider,
        netDutySuspension.totalLtsPureAlcoholCider
      ),
      netDutySuspensionCell(
        "return.regime.Spirits",
        netDutySuspension.totalLtsSpirit,
        netDutySuspension.totalLtsPureAlcoholSpirit
      ),
      netDutySuspensionCell(
        "return.regime.Wine",
        netDutySuspension.totalLtsWine,
        netDutySuspension.totalLtsPureAlcoholWine
      ),
      netDutySuspensionCell(
        "return.regime.OtherFermentedProduct",
        netDutySuspension.totalLtsOtherFermented,
        netDutySuspension.totalLtsPureAlcoholOtherFermented
      )
    ).flatten

  private def netDutySuspensionCell(
    messageKey: String,
    totalLitres: Option[BigDecimal],
    litresOfPureAlcohol: Option[BigDecimal]
  )(implicit messages: Messages): Option[TableRowViewModel] =
    for {
      total <- totalLitres
      lpa   <- litresOfPureAlcohol
    } yield TableRowViewModel(
      cells = Seq(
        TableRow(
          content = Text(messages(messageKey).capitalize)
        ),
        TableRow(
          content = Text(messages("site.2DP", total)),
          classes = s"${Constants.textAlignRightCssClass} govuk-table__cell--numeric"
        ),
        TableRow(
          content = Text(messages("site.4DP", lpa)),
          classes = s"${Constants.textAlignRightCssClass} govuk-table__cell--numeric"
        )
      )
    )

  private def noneDeclaredDutySuspension()(implicit messages: Messages) = Seq(
    TableRowViewModel(
      cells = Seq(
        TableRow(content = Text(messages("viewReturn.netDutySuspension.noneDeclared")))
      )
    )
  )

  private def noneDeclareDutySuspensionHeader()(implicit messages: Messages): Seq[HeadCell] = Seq(
    HeadCell(
      content = Text(messages("viewReturn.table.description.legend"))
    )
  )
}
