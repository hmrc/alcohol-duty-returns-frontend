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
import pages.declareDuty.WhatDoYouNeedToDeclarePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

import javax.inject.Inject

case class ReturnSummaryList(
  whatDoYouNeedToDeclareSummary: SummaryList,
  howMuchDoYouNeedToDeclareSummary: Option[SummaryList],
  smallProducerReliefSummary: Option[SummaryList]
)

class CheckYourAnswersSummaryListHelper @Inject() (
  howMuchDoYouNeedToDeclareSummary: HowMuchDoYouNeedToDeclareSummary,
  smallProducerReliefSummary: SmallProducerReliefSummary,
  whatDoYouNeedToDeclareSummary: WhatDoYouNeedToDeclareSummary
) {
  def createSummaryList(regime: AlcoholRegime, userAnswers: UserAnswers)(implicit
    messages: Messages
  ): Option[ReturnSummaryList] =
    userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime) match {
      case Some(whatDoYouNeedToDeclare) =>
        Some(
          ReturnSummaryList(
            whatDoYouNeedToDeclareSummary = whatDoYouNeedToDeclareSummary.summaryList(regime, whatDoYouNeedToDeclare),
            howMuchDoYouNeedToDeclareSummary =
              howMuchDoYouNeedToDeclareSummary.summaryList(regime, whatDoYouNeedToDeclare, userAnswers),
            smallProducerReliefSummary = smallProducerReliefSummary.summaryList(regime, userAnswers)
          )
        )
      case _                            => None
    }

}
