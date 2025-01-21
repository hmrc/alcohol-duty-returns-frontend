/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.checkAnswers.adjustment

import base.SpecBase
import models.AlcoholRegime.Beer
import models.adjustment.AdjustmentEntry
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Key, SummaryListRow, Value}

class SpoiltAlcoholicProductTypeSummarySpec extends SpecBase {
  "SpoiltAlcoholicProductTypeSummary" - {
    "must return a row if the spoilt regime can be fetched" in new SetUp(true) {
      spoiltAlcoholicProductTypeSummary.row(adjustmentEntry) mustBe Some(
        SummaryListRow(
          Key(Text("Description")),
          Value(HtmlContent("Beer")),
          "",
          Some(
            Actions(items =
              List(
                ActionItem(
                  "/manage-alcohol-duty/complete-return/adjustments/adjustment/change/change/spoilt-product/alcohol-type",
                  Text("Change"),
                  Some("Alcoholic product type")
                )
              )
            )
          )
        )
      )
    }

    "must return no row if no spoilt regime can be fetched" in new SetUp(false) {
      spoiltAlcoholicProductTypeSummary.row(adjustmentEntry) mustBe None
    }
  }

  class SetUp(hasSpoiltRegime: Boolean) {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val maybeSpoiltRegime = if (hasSpoiltRegime) {
      Some(Beer)
    } else {
      None
    }

    val adjustmentEntry = AdjustmentEntry(spoiltRegime = maybeSpoiltRegime)

    val spoiltAlcoholicProductTypeSummary = new SpoiltAlcoholicProductTypeSummary()
  }
}
