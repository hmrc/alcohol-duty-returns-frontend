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
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem

class TellUsAboutMultipleSPRRateSummarySpec extends SpecBase {
  implicit val messages: Messages = getMessages(app)

  "TellUsAboutMultipleSPRRateSummary" - {
    "must return summary list rows with the correct content" in {
      val answers = tellUsAboutMultipleSPRRatePage(
        whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands),
        Beer,
        volumeAndRateByTaxType1
      )

      val rows = TellUsAboutMultipleSPRRateSummary.rows(Beer, answers, None)

      val expectedKeys = Seq(Text("Description"), Text("Total beer"), Text("Total pure alcohol"), Text("Duty rate"))

      val expectedValues = Seq(
        Text("Draught beer between 2% and 3% ABV (tax type code 124)"),
        HtmlContent("100.00 litres"),
        HtmlContent("2.5000 LPA"),
        HtmlContent("£1.26 per litre")
      )

      val expectedActions = Seq(
        ActionItem(
          content = Text("Change"),
          href = controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController
            .onPageLoad(CheckMode, Beer)
            .url + "#taxType",
          visuallyHiddenText = Some("Description")
        ),
        ActionItem(
          content = Text("Change"),
          href = controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController
            .onPageLoad(CheckMode, Beer)
            .url + "#totalLitres",
          visuallyHiddenText = Some("Total beer")
        ),
        ActionItem(
          content = Text("Change"),
          href = controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController
            .onPageLoad(CheckMode, Beer)
            .url + "#pureAlcohol",
          visuallyHiddenText = Some("Total pure alcohol")
        ),
        ActionItem(
          content = Text("Change"),
          href = controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController
            .onPageLoad(CheckMode, Beer)
            .url + "#dutyRate",
          visuallyHiddenText = Some("Duty rate")
        )
      )

      rows.map(_.key.content)            mustBe expectedKeys
      rows.map(_.value.content)          mustBe expectedValues
      rows.map(_.actions.get.items.head) mustBe expectedActions
    }

    "must return no rows if WhatDoYouNeedToDeclare page doesn't have an answer" in {
      val answers = tellUsAboutMultipleSPRRatePage(userAnswersWithBeer, Beer, volumeAndRateByTaxType1)

      TellUsAboutMultipleSPRRateSummary.rows(Beer, answers, None) mustBe Seq.empty
    }

    "must return no rows if TellUsAboutMultipleSPRRatePage page doesn't have an answer" in {
      val answers = whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands)

      TellUsAboutMultipleSPRRateSummary.rows(Beer, answers, None) mustBe Seq.empty
    }

    "must return no rows if a tax type is not found in the rateBands" in {
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
}
