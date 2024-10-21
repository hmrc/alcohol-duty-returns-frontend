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

import base.SpecBase
import models.AlcoholRegime.Beer
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Empty, Text}

class MultipleSPRListSummarySpec extends SpecBase {
  "MultipleSPRListSummary" - {
    "should summarise tax types" in new SetUp {
      val answers =
        specifyAllMultipleSPRListUnsorted(whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands), Beer)

      val rows = MultipleSPRListSummary.rows(Beer, answers)
      rows.map(_.key.content) mustBe
        Seq(
          Text("Draught beer between 2% and 3% ABV (124)"),
          Text("Total volume"),
          Text("Pure alcohol"),
          Text("Non-draught beer between 3% and 4% ABV (125 SPR)"),
          Text("Total volume"),
          Text("Pure alcohol"),
          Text("Draught beer between 4% and 5% ABV (126 SPR)"),
          Text("Total volume"),
          Text("Pure alcohol")
        )
      rows.map(_.value.content) mustBe
        Seq(
          Empty,
          Text("100.00 litres"),
          Text("2.5000 litres"),
          Empty,
          Text("1,000.00 litres"),
          Text("3.5000 litres"),
          Empty,
          Text("30,000.00 litres"),
          Text("9.3000 litres")
        )
    }

    "should return no rows if WhatDoYouNeedToDeclare page doesn't have an answer" in new SetUp {
      val answers = specifyAllMultipleSPRListUnsorted(userAnswersWithBeer, Beer)

      MultipleSPRListSummary.rows(Beer, answers) mustBe Seq.empty
    }

    "should return no rows if MultipleSPRListPage page doesn't have an answer" in new SetUp {
      val answers = whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands)

      MultipleSPRListSummary.rows(Beer, answers) mustBe Seq.empty
    }

    "should throw an exception if a tax type is not found in the rateBands" in new SetUp {
      val badTaxCode                = "555"
      val badVolumeAndRateByTaxType = Seq(
        volumeAndRateByTaxType1,
        volumeAndRateByTaxType2,
        volumeAndRateByTaxType3,
        volumeAndRateByTaxType4.copy(taxType = badTaxCode)
      )
      val answers                   = multipleSPRListPage(
        whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands),
        Beer,
        badVolumeAndRateByTaxType
      )

      an[IllegalArgumentException] shouldBe thrownBy(MultipleSPRListSummary.rows(Beer, answers))
    }
  }

  class SetUp {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)
  }
}
