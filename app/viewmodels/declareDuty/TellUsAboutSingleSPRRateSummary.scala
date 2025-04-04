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

import models.{AlcoholRegime, UserAnswers}
import pages.declareDuty.{TellUsAboutSingleSPRRatePage, WhatDoYouNeedToDeclarePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import RateBandDescription.toDescription
import config.Constants.Css
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object TellUsAboutSingleSPRRateSummary {

  def rows(regime: AlcoholRegime, answers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = {
    val rows = for {
      rateBands         <- answers.getByKey(WhatDoYouNeedToDeclarePage, regime)
      dutyByTaxTypeList <- answers.getByKey(TellUsAboutSingleSPRRatePage, regime)
    } yield dutyByTaxTypeList.flatMap { dutyByTaxType =>
      val rateBand = rateBands
        .find(_.taxTypeCode == dutyByTaxType.taxType)
        .getOrElse(throw new IllegalArgumentException(s"Invalid tax type: ${dutyByTaxType.taxType}"))
      Seq(
        SummaryListRowViewModel(
          key = messages("howMuchDoYouNeedToDeclare.checkYourAnswersLabel.row.description"),
          value = ValueViewModel(toDescription(rateBand, Some(regime)).capitalize)
        ).withCssClass(Css.summaryListRowNoBorderCssClass),
        SummaryListRowViewModel(
          key = messages("checkYourAnswersLabel.row.totalLitres"),
          value = ValueViewModel(
            s"${messages("site.2DP", dutyByTaxType.totalLitres)} ${messages("site.unit.litres")}"
          )
        ).withCssClass(Css.summaryListRowNoBorderCssClass),
        SummaryListRowViewModel(
          key = messages("checkYourAnswersLabel.row.pureAlcohol"),
          value = ValueViewModel(
            s"${messages("site.4DP", dutyByTaxType.pureAlcohol)} ${messages("site.unit.litres")}"
          )
        ).withCssClass(Css.summaryListRowNoBorderCssClass),
        SummaryListRowViewModel(
          key = messages("checkYourAnswersLabel.row.dutyRate"),
          value = ValueViewModel(messages("site.currency.2DP", dutyByTaxType.dutyRate))
        )
      )
    }
    rows.getOrElse(Seq.empty)
  }
}
