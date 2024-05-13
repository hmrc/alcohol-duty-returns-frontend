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

package viewmodels.checkAnswers.returns

import models.{AlcoholByVolume, RateBand, RateType}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.all.CheckboxGroupedItemViewModel

case class TaxBandsViewModel(
  core: Seq[CheckboxItem],
  draught: Seq[CheckboxItem],
  smallProducerRelief: Seq[CheckboxItem],
  draughtAndSmallProducerRelief: Seq[CheckboxItem]
)

object TaxBandsViewModel {
  def apply(rateBands: Seq[RateBand])(implicit messages: Messages): TaxBandsViewModel = {
    val rateBandsByType = rateBands
      .groupBy(_.rateType)
      .view
      .mapValues(rateBands =>
        rateBands.map(rateBand =>
          CheckboxGroupedItemViewModel(
            content = rateBandContent(rateBand),
            fieldId = "",
            groupId = rateBand.rateType.toString,
            value = rateBand.taxType
          )
        )
      )
      .toMap

    TaxBandsViewModel(
      core = rateBandsByType.getOrElse(RateType.Core, Seq.empty),
      draught = rateBandsByType.getOrElse(RateType.DraughtRelief, Seq.empty),
      smallProducerRelief = rateBandsByType.getOrElse(RateType.SmallProducerRelief, Seq.empty),
      draughtAndSmallProducerRelief = rateBandsByType.getOrElse(RateType.DraughtAndSmallProducerRelief, Seq.empty)
    )
  }

  private def rateBandContent(rateBand: RateBand)(implicit messages: Messages): Text = Text(
    rateBand.maxABV match {
      case AlcoholByVolume.MAX =>
        messages(
          s"whatDoYouNeedToDeclare.option.abv.exceeding.max",
          rateBand.minABV.value,
          rateBand.taxType
        )
      case _                   =>
        messages(
          s"whatDoYouNeedToDeclare.option.abv.interval",
          rateBand.minABV.value,
          rateBand.maxABV.value,
          rateBand.taxType
        )
    }
  )
}
