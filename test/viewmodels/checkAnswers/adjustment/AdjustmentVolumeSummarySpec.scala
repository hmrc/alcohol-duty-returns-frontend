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
import models.CheckMode
import models.adjustment.AdjustmentEntry
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}

class AdjustmentVolumeSummarySpec extends SpecBase {
  "AdjustmentVolumeSummary" - {
    "must return a row if the both the total litres volume and pure alcohol volume can be fetched" in new SetUp(
      true,
      true
    ) {
      val row = adjustmentVolumeSummary.row(adjustmentEntry)

      val expectedAction = ActionItem(
        content = Text("Change"),
        href = controllers.adjustment.routes.AdjustmentVolumeController.onPageLoad(CheckMode).url,
        visuallyHiddenText = Some("volume")
      )

      row.get.key.content.asHtml.toString   mustBe "Volume"
      row.get.value.content.asHtml.toString mustBe "<span class='break'>12.20 litres</span><span class='break'>1.2000 litres of pure alcohol</span>"
      row.get.actions.get.items.head        mustBe expectedAction
    }

    "must return no row if the total litres volume type can't be fetched" in new SetUp(true, false) {
      adjustmentVolumeSummary.row(adjustmentEntry) mustBe None
    }

    "must return no row if the pure alcohol volume can't be fetched" in new SetUp(false, true) {
      adjustmentVolumeSummary.row(adjustmentEntry) mustBe None
    }

    "must return no row if neither volume can be fetched" in new SetUp(false, false) {
      adjustmentVolumeSummary.row(adjustmentEntry) mustBe None
    }
  }

  class SetUp(hasTotalLitresVolume: Boolean, hasPureAlcoholVolume: Boolean) {
    implicit val messages: Messages = getMessages(app)

    val maybeTotalLitresVolume = if (hasTotalLitresVolume) {
      Some(BigDecimal("12.2"))
    } else {
      None
    }

    val maybePureAlcoholVolume = if (hasPureAlcoholVolume) {
      Some(BigDecimal("1.2000"))
    } else {
      None
    }

    val adjustmentEntry =
      AdjustmentEntry(totalLitresVolume = maybeTotalLitresVolume, pureAlcoholVolume = maybePureAlcoholVolume)

    val adjustmentVolumeSummary = new AdjustmentVolumeSummary
  }
}
