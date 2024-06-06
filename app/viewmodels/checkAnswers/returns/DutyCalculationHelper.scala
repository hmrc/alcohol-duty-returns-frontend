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

import models.returns.{AlcoholDuty, DutyByTaxType}
import models.{AlcoholRegime, UserAnswers}
import pages.returns.WhatDoYouNeedToDeclarePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.HeadCell
import viewmodels.checkAnswers.returns.RateBandHelper.rateBandContent
import viewmodels.{TableRowActionViewModel, TableRowViewModel, TableViewModel}

object DutyCalculationHelper {

  def dutyDueTableViewModel(
    totalDutyCalculationResponse: AlcoholDuty,
    userAnswers: UserAnswers,
    regime: AlcoholRegime
  )(implicit messages: Messages): TableViewModel =
    TableViewModel(
      head = Seq(
        HeadCell(Text("Description")),
        HeadCell(Text("Litres of pure alcohol")),
        HeadCell(Text("Duty rate")),
        HeadCell(Text("Duty due")),
        HeadCell(Text("Action"))
      ),
      rows = createRows(totalDutyCalculationResponse.dutiesByTaxType, userAnswers, regime),
      total = totalDutyCalculationResponse.totalDuty
    )

  private def createRows(totalsByTaxType: Seq[DutyByTaxType], userAnswers: UserAnswers, regime: AlcoholRegime)(implicit
    messages: Messages
  ): Seq[TableRowViewModel] = {
    val rateBands = userAnswers
      .getByKey(WhatDoYouNeedToDeclarePage, regime)
      .getOrElse(throw new RuntimeException("No duty to declare"))

    totalsByTaxType.map { totalByTaxType =>
      val rateBand = rateBands
        .find(_.taxType == totalByTaxType.taxType)
        .getOrElse(throw new IllegalArgumentException(s"Invalid tax type: ${totalByTaxType.taxType}"))

      TableRowViewModel(
        cells = Seq(
          Text(rateBandContent(rateBand, "checkYourAnswers.label")),
          Text(messages("dutyCalculation.pureAlcohol.value", totalByTaxType.pureAlcohol)),
          Text(messages("dutyCalculation.dutyRate.value", totalByTaxType.dutyRate)),
          Text(messages("site.currency.2DP", totalByTaxType.dutyDue))
        ),
        actions = Seq(
          TableRowActionViewModel(
            label = "Change",
            href = controllers.returns.routes.CheckYourAnswersController.onPageLoad(regime)
          )
        )
      )
    }
  }

}
