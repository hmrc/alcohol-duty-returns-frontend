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

import models.{AlcoholRegime, RateBand, UserAnswers}
import pages.declareDuty.{MultipleSPRListPage, WhatDoYouNeedToDeclarePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryListRow, Value}
import RateBandHelper.rateBandRecap
import config.Constants
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object MultipleSPRListSummary {

  def rows(regime: AlcoholRegime, answers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = {
    val rows = for {
      rateBands         <- answers.getByKey(WhatDoYouNeedToDeclarePage, regime)
      dutyByTaxTypeList <- answers.getByKey(MultipleSPRListPage, regime)
    } yield dutyByTaxTypeList
      .groupBy(_.taxType)
      .toSeq
      .sortBy(_._1)
      .flatMap { case (taxType, dutiesByTaxType) =>
        val rateBand = rateBands
          .find(_.taxTypeCode == taxType)
          .getOrElse(throw new IllegalArgumentException(s"Invalid tax type: $taxType"))

        val totalLitres = dutiesByTaxType.map(_.totalLitres).sum
        val pureAlcohol = dutiesByTaxType.map(_.pureAlcohol).sum

        createRow(rateBand, totalLitres, pureAlcohol)
      }
    rows.getOrElse(Seq.empty)
  }

  private def createRow(rateBand: RateBand, totalLitres: BigDecimal, pureAlcohol: BigDecimal)(implicit
    messages: Messages
  ): Seq[SummaryListRow] =
    Seq(
      SummaryListRowViewModel(
        key = KeyViewModel(rateBandRecap(rateBand)),
        value = Value()
      ).withCssClass(Constants.tableRowNoBorderCssClass),
      SummaryListRowViewModel(
        key = messages("checkYourAnswersLabel.row.totalLitres"),
        value = ValueViewModel(s"${messages("site.2DP", totalLitres)} ${messages("site.unit.litres")}")
      ).withCssClass(Constants.tableRowNoBorderCssClass),
      SummaryListRowViewModel(
        key = messages("checkYourAnswersLabel.row.pureAlcohol"),
        value = ValueViewModel(s"${messages("site.4DP", pureAlcohol)} ${messages("site.unit.litres")}")
      )
    )
}
