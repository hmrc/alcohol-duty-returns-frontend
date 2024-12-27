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
import uk.gov.hmrc.govukfrontend.views.Aliases.{SummaryList, SummaryListRow}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, Value}

class CheckYourAnswersSummaryListHelperSpec extends SpecBase {
  "WhatDoYouNeedToDeclareSummary" - {
    "must return None if there is no WhatDoYouNeedToDeclare page declared" in new SetUp {
      checkYourAnswersSummaryListHelper.createSummaryList(Beer, userAnswersWithBeer) mustBe None
    }

    "must return the summaries when the WhatDoYouNeedToDeclare page is declared" in new SetUp {
      val userAnswers = specifyWhatDoYouNeedToDeclare(userAnswersWithBeer, Beer)

      when(mockWhatDoYouNeedToDeclareSummary.summaryList(Beer, allRateBands)).thenReturn(summaryList1)
      when(mockHowMuchDoYouNeedToDeclareSummary.summaryList(Beer, allRateBands, userAnswers)).thenReturn(Some(summaryList2))
      when(mockSmallProducerReliefSummary.summaryList(Beer, userAnswers)).thenReturn(Some(summaryList3))

      checkYourAnswersSummaryListHelper.createSummaryList(Beer, userAnswers) mustBe Some(
        ReturnSummaryList(
          whatDoYouNeedToDeclareSummary = summaryList1,
          howMuchDoYouNeedToDeclareSummary = Some(summaryList2),
          smallProducerReliefSummary = Some(summaryList3)
        )
      )
    }
  }

  class SetUp {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val mockHowMuchDoYouNeedToDeclareSummary: HowMuchDoYouNeedToDeclareSummary = mock[HowMuchDoYouNeedToDeclareSummary]
    val mockSmallProducerReliefSummary: SmallProducerReliefSummary = mock[SmallProducerReliefSummary]
    val mockWhatDoYouNeedToDeclareSummary: WhatDoYouNeedToDeclareSummary = mock[WhatDoYouNeedToDeclareSummary]

    val checkYourAnswersSummaryListHelper = new CheckYourAnswersSummaryListHelper(mockHowMuchDoYouNeedToDeclareSummary, mockSmallProducerReliefSummary, mockWhatDoYouNeedToDeclareSummary)

    val summaryList1 = SummaryList(rows = Seq(SummaryListRow(key = Key(content = Text("Key1")), value = Value(content = Text("Value1")))))
    val summaryList2 = SummaryList(rows = Seq(SummaryListRow(key = Key(content = Text("Key2")), value = Value(content = Text("Value2")))))
    val summaryList3 = SummaryList(rows = Seq(SummaryListRow(key = Key(content = Text("Key3")), value = Value(content = Text("Value3")))))
  }
}
