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
import models.CheckMode
import models.adjustment.AdjustmentEntry
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}

class SpoiltAlcoholicProductTypeSummarySpec extends SpecBase {
  "SpoiltAlcoholicProductTypeSummary" - {
    "must return a row if the spoilt regime can be fetched" in new SetUp(true) {
      val row = spoiltAlcoholicProductTypeSummary.row(adjustmentEntry)

      val expectedAction = ActionItem(
        content = Text("Change"),
        href = controllers.adjustment.routes.SpoiltAlcoholicProductTypeController.onPageLoad(CheckMode).url,
        visuallyHiddenText = Some("description")
      )

      row.get.key.content.asHtml.toString   mustBe "Description"
      row.get.value.content.asHtml.toString mustBe "Beer"
      row.get.actions.get.items.head        mustBe expectedAction
    }

    "must return no row if no spoilt regime can be fetched" in new SetUp(false) {
      spoiltAlcoholicProductTypeSummary.row(adjustmentEntry) mustBe None
    }
  }

  class SetUp(hasSpoiltRegime: Boolean) {
    implicit val messages: Messages = getMessages(app)

    val maybeSpoiltRegime = if (hasSpoiltRegime) {
      Some(Beer)
    } else {
      None
    }

    val adjustmentEntry = AdjustmentEntry(spoiltRegime = maybeSpoiltRegime)

    val spoiltAlcoholicProductTypeSummary = new SpoiltAlcoholicProductTypeSummary()
  }
}
