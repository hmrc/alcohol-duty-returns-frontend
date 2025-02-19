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
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class TellUsAboutMultipleSPRRateSummarySpec extends SpecBase {
  "TellUsAboutMultipleSPRRateSummary" - {
    "must summarise tax types" in new SetUp {
      val answers = tellUsAboutMultipleSPRRatePage(
        whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands),
        Beer,
        volumeAndRateByTaxType1
      )

      val rows = TellUsAboutMultipleSPRRateSummary.rows(Beer, answers, None)
      rows.map(_.key.content) mustBe
        Seq(Text("Description"), Text("Total beer"), Text("Total pure alcohol"), Text("Duty rate"))
      rows.map(_.value.content) mustBe
        Seq(
          Text("Draught beer between 2% and 3% ABV (124)"),
          HtmlContent("100.00 litres"),
          HtmlContent("2.5000 LPA"),
          HtmlContent("£1.26 per litre")
        )
    }

    "must return no rows if WhatDoYouNeedToDeclare page doesn't have an answer" in new SetUp {
      val answers = tellUsAboutMultipleSPRRatePage(userAnswersWithBeer, Beer, volumeAndRateByTaxType1)

      TellUsAboutMultipleSPRRateSummary.rows(Beer, answers, None) mustBe Seq.empty
    }

    "must return no rows if TellUsAboutMultipleSPRRatePage page doesn't have an answer" in new SetUp {
      val answers = whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands)

      TellUsAboutMultipleSPRRateSummary.rows(Beer, answers, None) mustBe Seq.empty
    }

    "must return no rows if a tax type is not found in the rateBands" in new SetUp {
      val badTaxCode                = "555"
      val badVolumeAndRateByTaxType = volumeAndRateByTaxType4.copy(taxType = badTaxCode)
      val answers                   = tellUsAboutMultipleSPRRatePage(
        whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands),
        Beer,
        badVolumeAndRateByTaxType
      )

      TellUsAboutMultipleSPRRateSummary.rows(Beer, answers, None) mustBe Seq.empty
    }
  }

  class SetUp {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)
  }
}
