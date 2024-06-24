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

package viewmodels.checkAnswers.returns

import models.{AlcoholRegimeName, UserAnswers}
import pages.returns.{MultipleSPRListPage, WhatDoYouNeedToDeclarePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryListRow, Value}
import viewmodels.checkAnswers.returns.RateBandHelper.rateBandRecap
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object MultipleSPRListSummary {

  def rows(regime: AlcoholRegimeName, answers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = {
    val rows = for {
      rateBands         <- answers.getByKey(WhatDoYouNeedToDeclarePage, regime)
      dutyByTaxTypeList <- answers.getByKey(MultipleSPRListPage, regime)
    } yield dutyByTaxTypeList
      .groupBy(_.taxType)
      .flatMap { case (taxType, dutiesByTaxType) =>
        val rateBand = rateBands
          .find(_.taxType == taxType)
          .getOrElse(throw new IllegalArgumentException(s"Invalid tax type: $taxType"))

        val totalLitres = dutiesByTaxType.map(_.totalLitres).sum
        val pureAlcohol = dutiesByTaxType.map(_.pureAlcohol).sum

        Seq(
          SummaryListRowViewModel(
            key = KeyViewModel(rateBandRecap(rateBand)),
            value = Value()
          ).withCssClass("govuk-summary-list__row--no-border govuk-summary-list__only_key"),
          SummaryListRowViewModel(
            key = messages("checkYourAnswersLabel.row.totalLitres"),
            value = ValueViewModel(s"${totalLitres.toString} ${messages("site.unit.litres")}")
          ).withCssClass("govuk-summary-list__row--no-border"),
          SummaryListRowViewModel(
            key = messages("checkYourAnswersLabel.row.pureAlcohol"),
            value = ValueViewModel(s"${pureAlcohol.toString} ${messages("site.unit.litres")}")
          )
        )
      }
      .toSeq
    rows.getOrElse(Seq.empty)
  }
}