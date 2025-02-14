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

import config.Constants.Css
import config.FrontendAppConfig
import models.checkAndSubmit.AdrTypeOfSpirit
import models.checkAndSubmit.AdrTypeOfSpirit._
import models.returns._
import models.{RateBand, ReturnPeriod}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HeadCell, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewmodels.declareDuty.RateBandHelper.rateBandRecap
import viewmodels.govuk.summarylist._
import viewmodels.{Money, TableRowViewModel, TableViewModel, TotalsSummaryList}

import java.time.YearMonth
import javax.inject.Inject

class ViewReturnViewModel @Inject() (appConfig: FrontendAppConfig) {
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
      HeadCell(content = Text(messages("viewReturn.table.lpa.legend")), classes = Css.textAlignRightWrapCssClass),
      HeadCell(
        content = Text(messages("viewReturn.table.dutyRate.legend")),
        classes = Css.textAlignRightWrapCssClass
      ),
      HeadCell(
        content = Text(messages("viewReturn.table.dutyDue.legend")),
        classes = Css.textAlignRightWrapCssClass
      )
    )

  private def noAlcoholDeclaredTableHeader()(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(
        content = Text(messages("viewReturn.table.description.legend"))
      ),
      HeadCell(
        content = Text(messages("viewReturn.table.dutyDue.legend")),
        classes = s"${Css.textAlignRightCssClass} ${Css.numericCellClass}"
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
            classes = s"${Css.textAlignRightCssClass} ${Css.numericCellClass}"
          ),
          TableRow(
            content = Text(s"${Money.format(alcoholDeclaredDetailsRow.dutyRate)}"),
            classes = s"${Css.textAlignRightCssClass} ${Css.numericCellClass}"
          ),
          TableRow(
            content = Text(Money.format(alcoholDeclaredDetailsRow.dutyValue)),
            classes = s"${Css.textAlignRightCssClass} ${Css.numericCellClass}"
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
      .map(rateBandRecap(_, None))
      .getOrElse(taxType)

  private def nilDeclarationRow()(implicit messages: Messages): Seq[TableRowViewModel]                             =
    Seq(
      TableRowViewModel(cells =
        Seq(
          TableRow(content = Text(messages("viewReturn.alcoholDuty.noneDeclared"))),
          TableRow(
            content = Text(messages("site.nil")),
            classes = s"${Css.textAlignRightCssClass} ${Css.numericCellClass}"
          )
        )
      )
    )
  private def dutyToDeclareTotal(alcoholDeclared: ReturnAlcoholDeclared)(implicit messages: Messages): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        TotalsSummaryList.row(messages("viewReturn.alcoholDuty.total.legend"), Money.format(alcoholDeclared.total))
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
      HeadCell(content = Text(messages("viewReturn.table.lpa.legend")), classes = Css.textAlignRightWrapCssClass),
      HeadCell(
        content = Text(messages("viewReturn.table.dutyRate.legend")),
        classes = Css.textAlignRightWrapCssClass
      ),
      HeadCell(
        content = Text(messages("viewReturn.table.dutyValue.legend")),
        classes = Css.textAlignRightWrapCssClass
      )
    )

  private def noAdjustmentsTableHeader()(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(
        content = Text(messages("viewReturn.table.description.legend"))
      ),
      HeadCell(
        content = Text(messages("viewReturn.table.dutyValue.legend")),
        classes = s"${Css.textAlignRightCssClass} ${Css.numericCellClass}"
      )
    )

  private def adjustmentRows(
    returnAdjustments: Seq[ReturnAdjustmentsRow],
    ratePeriodsAndTaxCodesToRateBands: Map[(YearMonth, String), RateBand]
  )(implicit messages: Messages): Seq[TableRowViewModel] =
    returnAdjustments.map { returnAdjustmentsRow =>
      val maybeRatePeriod         = ReturnPeriod.fromPeriodKey(returnAdjustmentsRow.returnPeriodAffected).map(_.period)
      val taxType                 = returnAdjustmentsRow.taxType
      val (description, dutyRate) = if (returnAdjustmentsRow.adjustmentTypeKey.equals(ReturnAdjustments.spoiltKey)) {
        (
          appConfig.getRegimeNameByTaxTypeCode(taxType) match {
            case Some(regime) => messages(s"alcoholType.$regime")
            case _            => taxType
          },
          messages("viewReturn.notApplicable")
        )
      } else {
        (
          getDescriptionOrFallbackToTaxTypeCode(ratePeriodsAndTaxCodesToRateBands, maybeRatePeriod, taxType),
          Money.format(returnAdjustmentsRow.dutyRate)
        )
      }
      TableRowViewModel(
        cells = Seq(
          TableRow(content = Text(messages(s"viewReturn.adjustments.type.${returnAdjustmentsRow.adjustmentTypeKey}"))),
          TableRow(content = Text(description)),
          TableRow(
            content = Text(messages("site.4DP", returnAdjustmentsRow.litresOfPureAlcohol)),
            classes = s"${Css.textAlignRightCssClass} ${Css.numericCellClass}"
          ),
          TableRow(
            content = Text(dutyRate),
            classes = s"${Css.textAlignRightCssClass} ${Css.numericCellClass}"
          ),
          TableRow(
            content = Text(Money.format(returnAdjustmentsRow.dutyValue)),
            classes = s"${Css.textAlignRightCssClass} ${Css.numericCellClass}"
          )
        )
      )
    }

  private def nilAdjustmentsRow()(implicit messages: Messages): Seq[TableRowViewModel] =
    Seq(
      TableRowViewModel(cells =
        Seq(
          TableRow(content = Text(messages("viewReturn.adjustments.noneDeclared"))),
          TableRow(content = Text(messages("site.nil")), classes = Css.textAlignRightCssClass)
        )
      )
    )

  private def adjustmentsTotal(adjustments: ReturnAdjustments)(implicit messages: Messages): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        TotalsSummaryList.row(messages("viewReturn.adjustments.total.legend"), Money.format(adjustments.total))
      )
    )

  def createTotalDueSummaryList(returnDetails: ReturnDetails)(implicit messages: Messages): SummaryList = {
    val content =
      if (
        returnDetails.totalDutyDue.totalDue == BigDecimal(0) &&
        returnDetails.alcoholDeclared.alcoholDeclaredDetails.toSeq.flatten.isEmpty &&
        returnDetails.adjustments.adjustmentDetails.toSeq.flatten.isEmpty
      ) {
        messages("site.nil")
      } else {
        Money.format(returnDetails.totalDutyDue.totalDue)
      }

    SummaryListViewModel(
      rows = Seq(
        TotalsSummaryList.row(messages("viewReturn.dutyDue.total.legend"), content)
      )
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
        classes = Css.textAlignRightWrapCssClass
      ),
      HeadCell(
        content = Text(messages("viewReturn.table.lpa.legend")),
        classes = Css.textAlignRightWrapCssClass
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
          classes = s"${Css.textAlignRightCssClass} ${Css.numericCellClass}"
        ),
        TableRow(
          content = Text(messages("site.4DP", lpa)),
          classes = s"${Css.textAlignRightCssClass} ${Css.numericCellClass}"
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

  def createSpiritsViewModels(
    returnDetails: ReturnDetails
  )(implicit messages: Messages): Seq[TableViewModel] =
    returnDetails.spirits match {
      case Some(spirits) =>
        Seq(
          TableViewModel(
            head = spiritsDeclaredTableHeader(),
            rows = spiritsDeclaredRows(spirits),
            caption = Some(messages("viewReturn.spirits.caption"))
          ),
          TableViewModel(
            head = spiritsTypesDeclaredTableHeader(),
            rows = spiritsTypesDeclaredRows(spirits)
          )
        )
      case None          =>
        Seq(
          TableViewModel(
            head = spiritsNotDeclaredTableHeader(),
            rows = spiritsNotDeclaredRow(),
            caption = Some(messages("viewReturn.spirits.caption"))
          )
        )
    }

  private def spiritsDeclaredTableHeader()(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(
        content = Text(messages("viewReturn.table.description.legend"))
      ),
      HeadCell(
        content = Text(messages("viewReturn.table.totalVolume.lpa.legend")),
        classes = Css.textAlignRightWrapCssClass
      )
    )

  private def spiritsDeclaredRows(spirits: ReturnSpirits)(implicit messages: Messages): Seq[TableRowViewModel] =
    Seq(
      ("viewReturn.spirits.totalVolume", spirits.spiritsVolumes.totalSpirits),
      ("viewReturn.spirits.scotchWhisky", spirits.spiritsVolumes.scotchWhisky),
      ("viewReturn.spirits.irishWhiskey", spirits.spiritsVolumes.irishWhiskey)
    ).map { case (legendKey, value) =>
      TableRowViewModel(
        cells = Seq(
          TableRow(content = Text(messages(legendKey))),
          TableRow(
            content = Text(messages("site.2DP", value)),
            classes = s"${Css.textAlignRightCssClass} ${Css.numericCellClass}"
          )
        )
      )
    }

  private def spiritsTypesDeclaredTableHeader()(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(
        content = Text(messages("viewReturn.table.typesOfSpirits.legend"))
      )
    )

  private val spiritsTypeToMessageKey: Map[AdrTypeOfSpirit, String] =
    Map(
      Malt                -> "viewReturn.spirits.type.malt",
      Grain               -> "viewReturn.spirits.type.grain",
      NeutralAgricultural -> "viewReturn.spirits.type.neutralAgricultural",
      NeutralIndustrial   -> "viewReturn.spirits.type.neutralIndustrial",
      Beer                -> "viewReturn.spirits.type.beer",
      CiderOrPerry        -> "viewReturn.spirits.type.cider",
      WineOrMadeWine      -> "viewReturn.spirits.type.wine"
    )

  private def spiritsTypesDeclaredRows(spirits: ReturnSpirits)(implicit messages: Messages): Seq[TableRowViewModel] = {
    val typesOfSpirit = spirits.typesOfSpirit

    val spiritsTypesDetails = AdrTypeOfSpirit.values
      .flatMap {
        case Other if typesOfSpirit.contains(Other)               => spirits.otherSpiritTypeName
        case typeOfSpirit if typesOfSpirit.contains(typeOfSpirit) =>
          spiritsTypeToMessageKey.get(typeOfSpirit).map(messages(_))
        case _                                                    => None
      }
      .mkString(", ")

    Seq(
      TableRowViewModel(
        cells = Seq(
          TableRow(content = Text(spiritsTypesDetails))
        )
      )
    )
  }

  private def spiritsNotDeclaredTableHeader()(implicit messages: Messages): Seq[HeadCell] = Seq(
    HeadCell(
      content = Text(messages("viewReturn.table.description.legend"))
    )
  )

  private def spiritsNotDeclaredRow()(implicit messages: Messages) = Seq(
    TableRowViewModel(
      cells = Seq(
        TableRow(content = Text(messages("viewReturn.spirits.noneDeclared")))
      )
    )
  )
}
