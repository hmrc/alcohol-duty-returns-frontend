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

class TellUsAboutSingleSPRRateSummarySpec extends SpecBase {
  "TellUsAboutSingleSPRRateSummary" - {
    "must summarise tax types" in new SetUp {
      val answers =
        specifyTellUsAboutAllSingleSPRRate(whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands), Beer)

      val rows = TellUsAboutSingleSPRRateSummary.rows(Beer, answers)
      rows.map(_.key.content)   mustBe
        Seq(
          Text("Description"),
          Text("Total volume"),
          Text("Pure alcohol"),
          Text("SPR duty rate"),
          Text("Description"),
          Text("Total volume"),
          Text("Pure alcohol"),
          Text("SPR duty rate"),
          Text("Description"),
          Text("Total volume"),
          Text("Pure alcohol"),
          Text("SPR duty rate")
        )
      rows.map(_.value.content) mustBe
        Seq(
          Text("Non-draught beer between 3% and 4% ABV (tax type code 125 SPR)"),
          Text("1,000.00 litres"),
          Text("3.5000 litres"),
          Text("£1.46"),
          Text("Draught beer between 4% and 5% ABV (tax type code 126 SPR)"),
          Text("10,000.00 litres"),
          Text("4.5000 litres"),
          Text("£1.66"),
          Text("Draught beer between 4% and 5% ABV (tax type code 126 SPR)"),
          Text("20,000.00 litres"),
          Text("4.8000 litres"),
          Text("£1.66")
        )
    }

    "must return no rows if WhatDoYouNeedToDeclare page doesn't have an answer" in new SetUp {
      val answers = specifyTellUsAboutAllSingleSPRRate(userAnswersWithBeer, Beer)

      TellUsAboutSingleSPRRateSummary.rows(Beer, answers) mustBe Seq.empty
    }

    "must return no rows if TellUsAboutSingleSPRRate page doesn't have an answer" in new SetUp {
      val answers = whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands)

      TellUsAboutSingleSPRRateSummary.rows(Beer, answers) mustBe Seq.empty
    }

    "must throw an exception if a tax type is not found in the rateBands" in new SetUp {
      val badTaxCode                = "555"
      val badVolumeAndRateByTaxType = Seq(
        volumeAndRateByTaxType1,
        volumeAndRateByTaxType2,
        volumeAndRateByTaxType3,
        volumeAndRateByTaxType4.copy(taxType = badTaxCode)
      )
      val answers                   = tellUsAboutSingleSPRRatePage(
        whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands),
        Beer,
        badVolumeAndRateByTaxType
      )

      an[IllegalArgumentException] mustBe thrownBy(TellUsAboutSingleSPRRateSummary.rows(Beer, answers))
    }
  }

  class SetUp {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)
  }
}
