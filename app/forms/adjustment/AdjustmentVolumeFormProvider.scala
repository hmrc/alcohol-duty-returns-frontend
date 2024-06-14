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

package forms.adjustment

import forms.mappings.Mappings
import models.AlcoholRegime
import models.adjustment.AdjustmentVolume
import play.api.data.Forms._

import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.Messages

class AdjustmentVolumeFormProvider @Inject() extends Mappings {

  def apply(regime: String)(implicit messages: Messages): Form[AdjustmentVolume] =
    Form(
      mapping(
        "adjustment-total-liters-input" -> bigDecimal(
          2,
          "adjustmentVolume.error.totalLitersVolume.required",
          "adjustmentVolume.error.totalLitersVolume.nonNumeric",
          "adjustmentVolume.error.totalLitersVolume.twoDecimalPlaces",
          Seq(messages(s"regime.$regime"))
        ).verifying(minimumValue(BigDecimal(0.01), "adjustmentVolume.error.totalLitersVolume.minimumRequired"))
          .verifying(
            maximumValue(BigDecimal(999999999.99), "adjustmentVolume.error.totalLitersVolume.maximumRequired")
          ),
        "adjustment-pure-alcohol-input" -> bigDecimal(
          2,
          "adjustmentVolume.error.pureAlcoholVolume.required",
          "adjustmentVolume.error.pureAlcoholVolume.nonNumeric",
          "adjustmentVolume.error.pureAlcoholVolume.twoDecimalPlaces",
          Seq(messages(s"regime.$regime"))
        ).verifying(minimumValue(BigDecimal(0.01), "adjustmentVolume.error.pureAlcoholVolume.minimumRequired"))
          .verifying(
            maximumValue(BigDecimal(999999999.99), "adjustmentVolume.error.pureAlcoholVolume.maximumRequired")
          )
      )(AdjustmentVolume.apply)(AdjustmentVolume.unapply)
    )
}
