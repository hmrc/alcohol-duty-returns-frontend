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

import base.SpecBase
import models.AlcoholRegime.Beer
import models.CheckMode
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}

class SmallProducerReliefSummarySpec extends SpecBase {
  "SmallProducerReliefSummary" - {
    "must return a multiple SPR list summary if has multiple SPR duty rates" in new SetUp {
      val answers = doYouHaveMultipleSPRDutyRatesPage(userAnswersWithBeer, Beer, true)

      val expectedCardAction = ActionItem(
        content = Text("Change"),
        href = controllers.declareDuty.routes.MultipleSPRListController.onPageLoad(Beer).url
      )

      val summaryList = smallProducerReliefSummary.summaryList(Beer, answers)

      summaryList.get.card.get.title.get.content      mustBe Text(
        "Beer eligible for Small Producer Relief (multiple duty rates)"
      )
      summaryList.get.card.get.actions.get.items.head mustBe expectedCardAction
    }

    "must return a single SPR list summary if has multiple SPR duty rates" in new SetUp {
      val answers = doYouHaveMultipleSPRDutyRatesPage(userAnswersWithBeer, Beer, false)

      val expectedCardAction = ActionItem(
        content = Text("Change"),
        href = controllers.declareDuty.routes.TellUsAboutSingleSPRRateController.onPageLoad(CheckMode, Beer).url
      )

      val summaryList = smallProducerReliefSummary.summaryList(Beer, answers)

      summaryList.get.card.get.title.get.content      mustBe Text("Beer eligible for Small Producer Relief")
      summaryList.get.card.get.actions.get.items.head mustBe expectedCardAction
    }

    "must return None if the question wasn't answered" in new SetUp {
      smallProducerReliefSummary.summaryList(Beer, userAnswersWithBeer) mustBe None
    }
  }

  class SetUp {
    implicit val messages: Messages = getMessages(app)

    val smallProducerReliefSummary = new SmallProducerReliefSummary
  }
}
