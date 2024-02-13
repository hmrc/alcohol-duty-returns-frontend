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

package viewmodels.checkAnswers.productEntry

import controllers.productEntry.routes
import models.productEntry.ProductEntry
import models.CheckMode
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, SummaryListRow}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
object CurrentProductEntrySummary {

  def calculations(productEntry: ProductEntry)(implicit messages: Messages): Option[Seq[SummaryListRow]] =
    (productEntry.sprDutyRate, productEntry.taxRate) match {
      case (Some(_), None)                                   =>
        val action = Seq(
          ActionItemViewModel(
            "site.change",
            routes.DeclareSmallProducerReliefDutyRateController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(messages("declareSmallProducerReliefDutyRate.change.hidden"))
        )
        getRowElements(productEntry, action)
      case (None, Some(_)) if productEntry.taxRate.isDefined =>
        getRowElements(productEntry, Seq.empty)
      case _                                                 => None
    }

  def getRowElements(productEntry: ProductEntry, dutyRateAction: Seq[ActionItem])(implicit
    messages: Messages
  ): Option[Seq[SummaryListRow]] =
    for {
      pureAlcoholVolume <- productEntry.pureAlcoholVolume
      rate              <- productEntry.rate
      duty              <- productEntry.duty
    } yield Seq(
      SummaryListRowViewModel(
        key = "pureAlcohol.checkYourAnswersLabel",
        value = ValueViewModel(s"${pureAlcoholVolume.toString} ${messages("site.unit.litres")}")
      ),
      SummaryListRowViewModel(
        key = "dutyDue.rate.checkYourAnswersLabel",
        value = ValueViewModel(s"£${messages("site.2DP", rate)} ${messages("site.rate.litre")}"),
        actions = dutyRateAction
      ),
      SummaryListRowViewModel(
        key = "dutyDue.duty.checkYourAnswersLabel",
        value = ValueViewModel(s"£${messages("site.2DP", duty)}")
      )
    )
}
