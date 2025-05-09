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

import models.{AlcoholRegime, CheckMode, UserAnswers}
import pages.declareDuty.DoYouHaveMultipleSPRDutyRatesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Card, CardTitle, SummaryList, SummaryListRow}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

class SmallProducerReliefSummary {
  def summaryList(regime: AlcoholRegime, userAnswers: UserAnswers)(implicit
    messages: Messages
  ): Option[SummaryList] =
    userAnswers.getByKey(DoYouHaveMultipleSPRDutyRatesPage, regime) map { hasMultipleSPREntries =>
      if (hasMultipleSPREntries) {
        createSummaryList(
          regime,
          titleKey = "tellUsAboutSingleSPRRate",
          rows = MultipleSPRListSummary.rows(regime, userAnswers),
          call = controllers.declareDuty.routes.MultipleSPRListController.onPageLoad(regime).url
        )
      } else {
        createSummaryList(
          regime,
          titleKey = "tellUsAboutMultipleSPRRate",
          rows = TellUsAboutSingleSPRRateSummary.rows(regime, userAnswers),
          call = controllers.declareDuty.routes.TellUsAboutSingleSPRRateController.onPageLoad(CheckMode, regime).url
        )

      }
    }

  private def createSummaryList(regime: AlcoholRegime, titleKey: String, rows: Seq[SummaryListRow], call: String)(
    implicit messages: Messages
  ): SummaryList =
    SummaryList(
      rows = rows,
      card = Some(
        Card(
          title = Some(
            CardTitle(
              content = Text(
                messages(
                  s"$titleKey.checkYourAnswersLabel.cardTitle",
                  messages(regime.regimeMessageKey)
                ).capitalize
              )
            )
          ),
          actions = Some(
            Actions(
              items = Seq(
                ActionItemViewModel("site.change", call)
              )
            )
          )
        )
      )
    )
}
