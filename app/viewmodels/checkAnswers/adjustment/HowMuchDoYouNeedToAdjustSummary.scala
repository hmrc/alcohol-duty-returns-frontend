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

package viewmodels.checkAnswers.adjustment

import controllers.adjustment.routes
import models.adjustment.AdjustmentEntry
import models.CheckMode
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object HowMuchDoYouNeedToAdjustSummary {

  def row(adjustmentEntry: AdjustmentEntry)(implicit messages: Messages): Option[SummaryListRow] =
    for {
      totalLitres <- adjustmentEntry.totalLitresVolume
      pureAlcohol <- adjustmentEntry.pureAlcoholVolume
      regime      <- adjustmentEntry.rateBand.map(_.alcoholRegime)
    } yield {

      val value = HtmlFormat.escape(totalLitres.toString()).toString + " " + messages(
        "howMuchDoYouNeedToAdjust.totalLitres",
        regime.head
      ) + "<br/>" + HtmlFormat.escape(pureAlcohol.toString()).toString + " " + messages(
        "howMuchDoYouNeedToAdjust.pureAlcohol"
      )

      SummaryListRowViewModel(
        key = "howMuchDoYouNeedToAdjust.checkYourAnswersLabel",
        value = ValueViewModel(HtmlContent(value)),
        actions = Seq(
          ActionItemViewModel("site.change", routes.AdjustmentVolumeWithSPRController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("howMuchDoYouNeedToAdjust.change.hidden"))
        )
      )
    }
}
