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

class WhatDoYouNeedToDeclareSummarySpec extends SpecBase {
  "WhatDoYouNeedToDeclareSummary" - {
    "should summarise the ratebands for a regime" in new SetUp {
      val summaryList = whatDoYouNeedToDeclareSummary.summaryList(Beer, allNonSmallProducerReliefRateBands)

      summaryList.rows.map(_.key.content) mustBe
        Seq(
          Text("Selected Beer to declare")
        )
      summaryList.rows.map(_.value.content) mustBe
        Seq(
          HtmlContent(
            "<ul><li>Non-draught beer between 1% and 2% ABV (123)</li><li>Draught beer between 2% and 3% ABV (124)</li></ul>"
          )
        )
    }
  }

  class SetUp {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val whatDoYouNeedToDeclareSummary = new WhatDoYouNeedToDeclareSummary
  }
}
