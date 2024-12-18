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

class HowMuchDoYouNeedToDeclareSummarySpec extends SpecBase {
  "HowMuchDoYouNeedToDeclareSummary" - {
    "should return None if HowMuchDoYouNeedToDeclare page doesn't have an answer" in new SetUp {
      HowMuchDoYouNeedToDeclareSummary.summaryList(
        Beer,
        allNonSmallProducerReliefRateBands,
        userAnswersWithBeer
      ) mustBe None
    }

    "should summarise declarations" in new SetUp {
      val answers = specifyAllHowMuchDoYouNeedToDeclareUnsorted(userAnswersWithBeer, Beer)

      val summaryList = HowMuchDoYouNeedToDeclareSummary.summaryList(Beer, allNonSmallProducerReliefRateBands, answers)
      summaryList.get.rows.map(_.key.content) mustBe
        Seq(
          Text("Tax type code description"),
          Text("Total volume"),
          Text("Pure alcohol"),
          Text("Tax type code description"),
          Text("Total volume"),
          Text("Pure alcohol")
        )
      summaryList.get.rows.map(_.value.content) mustBe
        Seq(
          Text("Non-draught beer between 1% and 2% ABV (123)"),
          Text("30,000.00 litres"),
          Text("4.1100 litres"),
          Text("Draught beer between 2% and 3% ABV (124)"),
          Text("100.00 litres"),
          Text("2.5000 litres")
        )
    }

    "should return no rows if no ratebands" in new SetUp {
      val answers = specifyAllHowMuchDoYouNeedToDeclareUnsorted(userAnswersWithBeer, Beer)

      val sumamryList = HowMuchDoYouNeedToDeclareSummary.summaryList(Beer, Set.empty, answers)
      sumamryList.get.rows.map(_.key.content) mustBe Seq.empty
      sumamryList.get.rows.map(_.value.content) mustBe Seq.empty
    }

    "should throw an exception if a tax type is not found in the rateBands" in new SetUp {
      val badTaxCode                = "555"
      val badVolumeAndRateByTaxType = Seq(
        volumeAndRateByTaxType1,
        volumeAndRateByTaxType5.copy(taxType = badTaxCode)
      )

      val answers = howMuchDoYouNeedToDeclare(userAnswersWithBeer, Beer, badVolumeAndRateByTaxType)

      an[IllegalArgumentException] shouldBe thrownBy(
        HowMuchDoYouNeedToDeclareSummary.summaryList(Beer, allNonSmallProducerReliefRateBands, answers)
      )
    }
  }

  class SetUp {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)
  }
}
