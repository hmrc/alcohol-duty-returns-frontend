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

import controllers.returns.routes
import models.{AlcoholRegimeName, CheckMode, UserAnswers}
import pages.returns.{TellUsAboutMultipleSPRRatePage, WhatDoYouNeedToDeclarePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, SummaryListRow}
import viewmodels.checkAnswers.returns.RateBandHelper.rateBandRecap
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object TellUsAboutMultipleSPRRateSummary {

  def rows(regime: AlcoholRegimeName, answers: UserAnswers, index: Option[Int])(implicit
    messages: Messages
  ): Seq[SummaryListRow] =
    (
      answers.getByKey(WhatDoYouNeedToDeclarePage, regime),
      answers.getByKey(TellUsAboutMultipleSPRRatePage, regime)
    ) match {
      case (Some(rateBands), Some(answer)) =>
        rateBands
          .find(_.taxType == answer.taxType)
          .map { rateBand =>
            val taxTypeRowViewModel =
              SummaryListRowViewModel(
                key = "tellUsAboutMultipleSPRRate.checkYourAnswersLabel.taxType",
                value = ValueViewModel(content = rateBandRecap(rateBand)),
                actions = actions("taxType", regime, index)
              )

            val totalLitresRowViewModel =
              SummaryListRowViewModel(
                key = KeyViewModel(
                  messages(
                    "tellUsAboutMultipleSPRRate.checkYourAnswersLabel.totalLitres.label",
                    messages(s"return.regime.$regime")
                  )
                ),
                value = ValueViewModel(
                  HtmlContent(
                    messages("tellUsAboutMultipleSPRRate.checkYourAnswersLabel.totalLitres.value", answer.totalLitres)
                  )
                ),
                actions = actions("totalLitres", regime, index)
              )

            val pureAlcoholRowViewModel =
              SummaryListRowViewModel(
                key = "tellUsAboutMultipleSPRRate.checkYourAnswersLabel.pureAlcohol.label",
                value = ValueViewModel(
                  HtmlContent(
                    messages("tellUsAboutMultipleSPRRate.checkYourAnswersLabel.pureAlcohol.value", answer.pureAlcohol)
                  )
                ),
                actions = actions("pureAlcohol", regime, index)
              )

            val dutyRateRowViewModel =
              SummaryListRowViewModel(
                key = "tellUsAboutMultipleSPRRate.checkYourAnswersLabel.dutyRate.label",
                value = ValueViewModel(
                  HtmlContent(
                    messages("tellUsAboutMultipleSPRRate.checkYourAnswersLabel.dutyRate.value", answer.dutyRate)
                  )
                ),
                actions = actions("dutyRate", regime, index)
              )

            Seq(taxTypeRowViewModel, totalLitresRowViewModel, pureAlcoholRowViewModel, dutyRateRowViewModel)
          }
          .getOrElse(Seq.empty)
      case _                               => Seq.empty
    }

  def actions(elementId: String, regime: AlcoholRegimeName, index: Option[Int])(implicit
    messages: Messages
  ): Seq[ActionItem] = Seq(
    ActionItemViewModel(
      "site.change",
      routes.TellUsAboutMultipleSPRRateController.onPageLoad(CheckMode, regime, index).url + s"#$elementId"
    )
      .withVisuallyHiddenText(messages("tellUsAboutMultipleSPRRate.change.hidden"))
  )
}