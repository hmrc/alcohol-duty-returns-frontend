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

import models.{AlcoholByVolume, AlcoholRegime, CheckMode, RateBand, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Card, CardTitle, SummaryList, SummaryListRow}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object WhatDoYouNeedToDeclareSummary {

  def summaryList(regime: AlcoholRegime, rateBands: Set[RateBand])(implicit messages: Messages): SummaryList =
    SummaryList(
      rows = Seq(row(regime, rateBands)),
      card = Some(
        Card(
          title =
            Some(CardTitle(content = Text(messages(s"whatDoYouNeedToDeclare.$regime.checkYourAnswersLabel.card")))),
          actions = Some(
            Actions(
              items = Seq(
                ActionItemViewModel(
                  "site.change",
                  controllers.returns.routes.WhatDoYouNeedToDeclareController.onPageLoad(CheckMode, regime).url
                )
                  .withVisuallyHiddenText(messages("whatDoYouNeedToDeclare.change.hidden"))
              )
            )
          )
        )
      )
    )

  def row(regime: AlcoholRegime, rateBands: Set[RateBand])(implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key = messages(s"whatDoYouNeedToDeclare.$regime.checkYourAnswersLabel.row"),
      value = ValueViewModel(
        HtmlContent(
          "<ul>" +
            rateBands
              .map(answer => s"<li>${rateBandContent(regime, answer)}</li>")
              .mkString("")
            + "</ul>"
        )
      ),
      actions = Seq()
    )

  def rateBandContent(regime: AlcoholRegime, rateBand: RateBand)(implicit messages: Messages): String =
    rateBand.maxABV match {
      case AlcoholByVolume.MAX =>
        messages(
          s"whatDoYouNeedToDeclare.$regime.checkYourAnswersLabel.option.abv.exceeding.max.${rateBand.rateType}",
          rateBand.minABV.value,
          rateBand.taxType
        )
      case _                   =>
        messages(
          s"whatDoYouNeedToDeclare.$regime.checkYourAnswersLabel.option.abv.interval.${rateBand.rateType}",
          rateBand.minABV.value,
          rateBand.maxABV.value,
          rateBand.taxType
        )
    }

}
