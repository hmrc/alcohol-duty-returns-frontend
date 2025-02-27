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
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class SmallProducerReliefSummarySpec extends SpecBase {
  "SmallProducerReliefSummary" - {
    "must return a multiple SPR list summary if has multiple SPR duty rates" in new SetUp {
      val answers = doYouHaveMultipleSPRDutyRatesPage(userAnswersWithBeer, Beer, true)

      smallProducerReliefSummary.summaryList(Beer, answers).get.card.get.title.get.content mustBe Text(
        "Beer eligible for Small Producer Relief (multiple duty rates)"
      )
    }

    "must return a single SPR list summary if has multiple SPR duty rates" in new SetUp {
      val answers = doYouHaveMultipleSPRDutyRatesPage(userAnswersWithBeer, Beer, false)

      smallProducerReliefSummary.summaryList(Beer, answers).get.card.get.title.get.content mustBe Text(
        "Beer eligible for Small Producer Relief"
      )
    }

    "must return None if the question wasn't answered" in new SetUp {
      smallProducerReliefSummary.summaryList(Beer, userAnswersWithBeer) mustBe None
    }
  }
  class SetUp {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val smallProducerReliefSummary = new SmallProducerReliefSummary
  }
}
