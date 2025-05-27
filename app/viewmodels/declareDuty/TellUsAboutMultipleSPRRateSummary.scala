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

import controllers.declareDuty.routes
import models.{AlcoholRegime, CheckMode, RateBand, UserAnswers}
import pages.declareDuty.{TellUsAboutMultipleSPRRatePage, WhatDoYouNeedToDeclarePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, SummaryListRow}
import RateBandDescription.toDescription
import models.declareDuty.VolumeAndRateByTaxType
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object TellUsAboutMultipleSPRRateSummary {

  def rows(regime: AlcoholRegime, answers: UserAnswers, index: Option[Int])(implicit
    messages: Messages
  ): Seq[SummaryListRow] =
    (
      answers.getByKey(WhatDoYouNeedToDeclarePage, regime),
      answers.getByKey(TellUsAboutMultipleSPRRatePage, regime)
    ) match {
      case (Some(rateBands), Some(answer)) =>
        rateBands
          .find(_.taxTypeCode == answer.taxType)
          .map { rateBand =>
            val taxTypeRowViewModel = getTaxTypeRowViewModel(regime, index, rateBand)

            val totalLitresRowViewModel = getTotalLitresRowViewModel(regime, index, answer)

            val pureAlcoholRowViewModel = getPureAlcoholRowViewModel(regime, index, answer)

            val dutyRateRowViewModel = getDutyRateRowViewModel(regime, index, answer)

            Seq(taxTypeRowViewModel, totalLitresRowViewModel, pureAlcoholRowViewModel, dutyRateRowViewModel)
          }
          .getOrElse(Seq.empty)
      case _                               => Seq.empty
    }

  private def getTaxTypeRowViewModel(regime: AlcoholRegime, index: Option[Int], rateBand: RateBand)(implicit
    messages: Messages
  ) =
    SummaryListRowViewModel(
      key = "tellUsAboutMultipleSPRRate.checkYourAnswersLabel.taxType",
      value = ValueViewModel(content = toDescription(rateBand, Some(regime)).capitalize),
      actions = actions(
        "taxType",
        regime,
        messages("tellUsAboutMultipleSPRRate.checkYourAnswersLabel.taxType"),
        index
      )
    )

  private def getTotalLitresRowViewModel(regime: AlcoholRegime, index: Option[Int], answer: VolumeAndRateByTaxType)(
    implicit messages: Messages
  ) =
    SummaryListRowViewModel(
      key = KeyViewModel(
        messages(
          "tellUsAboutMultipleSPRRate.checkYourAnswersLabel.totalLitres.label",
          messages(regime.regimeMessageKey)
        )
      ),
      value = ValueViewModel(
        HtmlContent(
          messages(
            "tellUsAboutMultipleSPRRate.checkYourAnswersLabel.totalLitres.value",
            messages("site.2DP", answer.totalLitres)
          )
        )
      ),
      actions = actions(
        "totalLitres",
        regime,
        messages(
          "tellUsAboutMultipleSPRRate.checkYourAnswersLabel.totalLitres.label",
          messages(regime.regimeMessageKey)
        ),
        index
      )
    )

  private def getPureAlcoholRowViewModel(regime: AlcoholRegime, index: Option[Int], answer: VolumeAndRateByTaxType)(
    implicit messages: Messages
  ) =
    SummaryListRowViewModel(
      key = "tellUsAboutMultipleSPRRate.checkYourAnswersLabel.pureAlcohol.label",
      value = ValueViewModel(
        HtmlContent(
          messages(
            "tellUsAboutMultipleSPRRate.checkYourAnswersLabel.pureAlcohol.value",
            messages("site.4DP", answer.pureAlcohol)
          )
        )
      ),
      actions = actions(
        "pureAlcohol",
        regime,
        messages("tellUsAboutMultipleSPRRate.checkYourAnswersLabel.pureAlcohol.label"),
        index
      )
    )

  private def getDutyRateRowViewModel(regime: AlcoholRegime, index: Option[Int], answer: VolumeAndRateByTaxType)(
    implicit messages: Messages
  ) =
    SummaryListRowViewModel(
      key = "tellUsAboutMultipleSPRRate.checkYourAnswersLabel.dutyRate.label",
      value = ValueViewModel(
        HtmlContent(
          messages("tellUsAboutMultipleSPRRate.checkYourAnswersLabel.dutyRate.value", answer.dutyRate)
        )
      ),
      actions = actions(
        "dutyRate",
        regime,
        messages("tellUsAboutMultipleSPRRate.checkYourAnswersLabel.dutyRate.label"),
        index
      )
    )

  private def actions(elementId: String, regime: AlcoholRegime, hiddenMessage: String, index: Option[Int])(implicit
    messages: Messages
  ): Seq[ActionItem] = Seq(
    ActionItemViewModel(
      "site.change",
      routes.TellUsAboutMultipleSPRRateController.onPageLoad(CheckMode, regime, index).url + s"#$elementId"
    )
      .withVisuallyHiddenText(hiddenMessage)
  )
}
