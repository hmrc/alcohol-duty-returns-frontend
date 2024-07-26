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

import models.{AlcoholRegime, CheckMode, RateBand}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Card, CardTitle, SummaryList, SummaryListRow}
import RateBandHelper.rateBandRecap
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object WhatDoYouNeedToDeclareSummary {

  def summaryList(regime: AlcoholRegime, rateBands: Set[RateBand])(implicit messages: Messages): SummaryList =
    SummaryList(
      rows = Seq(row(regime, rateBands)),
      card = Some(
        Card(
          title = Some(
            CardTitle(content =
              Text(
                messages(
                  s"whatDoYouNeedToDeclare.checkYourAnswersLabel.card",
                  messages(s"return.regime.${regime.toString}")
                ).capitalize
              )
            )
          ),
          actions = Some(
            Actions(
              items = Seq(
                ActionItemViewModel(
                  "site.change",
                  controllers.returns.routes.WhatDoYouNeedToDeclareController.onPageLoad(CheckMode, regime).url
                )
              )
            )
          )
        )
      )
    )

  def row(regime: AlcoholRegime, rateBands: Set[RateBand])(implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key = messages(
        s"whatDoYouNeedToDeclare.checkYourAnswersLabel.row",
        messages(s"return.regime.${regime.toString}").capitalize
      ),
      value = ValueViewModel(
        HtmlContent(
          "<ul>" +
            rateBands.toSeq
              .sortBy(_.taxTypeCode)
              .map(answer => s"<li>${rateBandRecap(answer)}</li>")
              .mkString("")
            + "</ul>"
        )
      ),
      actions = Seq()
    )

}
