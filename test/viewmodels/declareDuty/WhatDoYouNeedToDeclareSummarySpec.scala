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
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, HtmlContent, Text}

class WhatDoYouNeedToDeclareSummarySpec extends SpecBase {
  "WhatDoYouNeedToDeclareSummary" - {
    "must summarise the ratebands for a regime" in new SetUp {
      val summaryList = whatDoYouNeedToDeclareSummary.summaryList(Beer, allNonSmallProducerReliefRateBands)

      val expectedValue = Seq(
        HtmlContent(
          "<ul><li>Non-draught beer between 1% and 3% ABV (tax type code 123)</li><li>Draught beer between 2% and 3% ABV (tax type code 124)</li></ul>"
        )
      )

      val expectedCardAction = ActionItem(
        content = Text("Change"),
        href = controllers.declareDuty.routes.WhatDoYouNeedToDeclareController.onPageLoad(CheckMode, Beer).url
      )

      summaryList.rows.map(_.key.content)         mustBe Seq(Text("Selected beer to declare"))
      summaryList.rows.map(_.value.content)       mustBe expectedValue
      summaryList.card.get.title.get.content      mustBe Text("Beer to declare")
      summaryList.card.get.actions.get.items.head mustBe expectedCardAction
    }
  }

  class SetUp {
    implicit val messages: Messages = getMessages(app)

    val whatDoYouNeedToDeclareSummary = new WhatDoYouNeedToDeclareSummary
  }
}
