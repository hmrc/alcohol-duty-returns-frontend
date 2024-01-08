/*
 * Copyright 2023 HM Revenue & Customs
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

package viewmodels

import models.{AlcoholByVolume, RateBand}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.ViewUtils.withPercentage
case class TaxTypePageViewModel(
  abv: String,
  eligibleForDraughtRelief: Boolean,
  eligibleForSmallProducerRelief: Boolean,
  radioItems: Seq[RadioItem]
)

object TaxTypePageViewModel {
  def apply(
    abv: AlcoholByVolume,
    eligibleForDraughtRelief: Boolean,
    eligibleForSmallProducerRelief: Boolean,
    rateBands: Seq[RateBand]
  )(implicit
    messages: Messages
  ): TaxTypePageViewModel = {

    val radioItems: Seq[RadioItem] = for {
      rateBand <- rateBands
      regime   <- rateBand.alcoholRegime
    } yield RadioItem(
      content = Text(
        s"${messages("taxType.taxTypeRadio.regime." + regime)}, ${messages("taxType.taxTypeRadio.taxType")} ${rateBand.taxType}"
      ),
      value = Some(s"${rateBand.taxType}_$regime"),
      id = Some(s"value_${rateBand.taxType}")
    )

    TaxTypePageViewModel(
      withPercentage(abv.value),
      eligibleForDraughtRelief,
      eligibleForSmallProducerRelief,
      radioItems
    )
  }
}
