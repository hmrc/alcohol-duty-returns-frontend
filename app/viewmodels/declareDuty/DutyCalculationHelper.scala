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

package viewmodels.declareDuty

import config.Constants.Css
import models.declareDuty.{AlcoholDuty, DutyByTaxType}
import models.{AlcoholRegime, RateBand, UserAnswers}
import pages.declareDuty.WhatDoYouNeedToDeclarePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import viewmodels.declareDuty.RateBandDescription.toDescription
import viewmodels.{TableRowActionViewModel, TableRowViewModel, TableViewModel}

object DutyCalculationHelper {

  def dutyDueTableViewModel(
    totalDutyCalculationResponse: AlcoholDuty,
    userAnswers: UserAnswers,
    regime: AlcoholRegime
  )(implicit messages: Messages): Either[String, TableViewModel] =
    createRows(totalDutyCalculationResponse.dutiesByTaxType, userAnswers, regime).map { rows =>
      TableViewModel(
        head = Seq(
          HeadCell(Text(messages("dutyCalculation.table.description")), classes = Css.oneHalfCssClass),
          HeadCell(Text(messages("dutyCalculation.table.litresOfPureAlcohol")), classes = Css.numericHeaderClass),
          HeadCell(Text(messages("dutyCalculation.table.dutyRate")), classes = Css.numericHeaderClass),
          HeadCell(Text(messages("dutyCalculation.table.dutyDue")), classes = Css.numericHeaderClass),
          HeadCell(Text(messages("dutyCalculation.table.action")))
        ),
        rows = rows
      )
    }

  private def createRows(totalsByTaxType: Seq[DutyByTaxType], userAnswers: UserAnswers, regime: AlcoholRegime)(implicit
    messages: Messages
  ): Either[String, Seq[TableRowViewModel]] =
    userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime) match {
      case Some(rateBands) => extractRateBandsAndCreateRows(regime, rateBands, totalsByTaxType)
      case None            => Left("No rate bands found")
    }

  private def extractRateBandsAndCreateRows(
    regime: AlcoholRegime,
    rateBands: Set[RateBand],
    totalsByTaxType: Seq[DutyByTaxType]
  )(implicit messages: Messages): Either[String, Seq[TableRowViewModel]] =
    totalsByTaxType
      .map { totalByTaxType =>
        rateBands.find(_.taxTypeCode == totalByTaxType.taxType) match {
          case Some(rateBand) =>
            Right(createTableRowViewModel(regime, totalByTaxType, rateBand))
          case None           => Left(s"No rate band found for taxType: ${totalByTaxType.taxType}")
        }
      }
      .foldRight[Either[String, Seq[TableRowViewModel]]](Right(Seq.empty[TableRowViewModel])) { (either, acc) =>
        for {
          rows <- acc
          row  <- either
        } yield row +: rows
      }

  private def createTableRowViewModel(regime: AlcoholRegime, totalByTaxType: DutyByTaxType, rateBand: RateBand)(implicit
    messages: Messages
  ) =
    TableRowViewModel(
      cells = Seq(
        TableRow(Text(toDescription(rateBand, Some(regime)).capitalize)),
        TableRow(Text(messages("site.4DP", totalByTaxType.pureAlcohol)), classes = Css.numericCellClass),
        TableRow(Text(messages("site.currency.2DP", totalByTaxType.dutyRate)), classes = Css.numericCellClass),
        TableRow(Text(messages("site.currency.2DP", totalByTaxType.dutyDue)), classes = Css.numericCellClass)
      ),
      actions = Seq(
        TableRowActionViewModel(
          label = messages("site.change"),
          href = controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime),
          visuallyHiddenText = Some(toDescription(rateBand, Some(regime)))
        )
      )
    )
}
