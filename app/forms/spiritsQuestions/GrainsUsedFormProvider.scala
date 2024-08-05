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

package forms.spiritsQuestions

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.spiritsQuestions.GrainsUsed

class GrainsUsedFormProvider @Inject() extends Mappings {
  import GrainsUsedFormProvider._

  def apply(): Form[GrainsUsed] = Form(
    mapping(
      "maltedBarleyQuantity"     -> bigDecimal(
        quantityMaxDecimalPlaces,
        "grainsUsed.error.maltedBarleyQuantity.required",
        "grainsUsed.error.maltedBarleyQuantity.nonNumeric",
        "grainsUsed.error.maltedBarleyQuantity.decimalPlaces"
      ).verifying(minimumValue(quantityMinValue, "grainsUsed.error.maltedBarleyQuantity.minimumRequired"))
        .verifying(maximumValue(quantityMaxValue, "grainsUsed.error.maltedBarleyQuantity.maximumRequired")),
      "wheatQuantity"            -> bigDecimal(
        quantityMaxDecimalPlaces,
        "grainsUsed.error.wheatQuantity.required",
        "grainsUsed.error.wheatQuantity.nonNumeric",
        "grainsUsed.error.wheatQuantity.decimalPlaces"
      ).verifying(minimumValue(quantityMinValue, "grainsUsed.error.wheatQuantity.minimumRequired"))
        .verifying(maximumValue(quantityMaxValue, "grainsUsed.error.wheatQuantity.maximumRequired")),
      "maizeQuantity"            -> bigDecimal(
        quantityMaxDecimalPlaces,
        "grainsUsed.error.maizeQuantity.required",
        "grainsUsed.error.maizeQuantity.nonNumeric",
        "grainsUsed.error.maizeQuantity.decimalPlaces"
      ).verifying(minimumValue(quantityMinValue, "grainsUsed.error.maizeQuantity.minimumRequired"))
        .verifying(maximumValue(quantityMaxValue, "grainsUsed.error.maizeQuantity.maximumRequired")),
      "ryeQuantity"              -> bigDecimal(
        quantityMaxDecimalPlaces,
        "grainsUsed.error.ryeQuantity.required",
        "grainsUsed.error.ryeQuantity.nonNumeric",
        "grainsUsed.error.ryeQuantity.decimalPlaces"
      ).verifying(minimumValue(quantityMinValue, "grainsUsed.error.ryeQuantity.minimumRequired"))
        .verifying(maximumValue(quantityMaxValue, "grainsUsed.error.ryeQuantity.maximumRequired")),
      "unmaltedGrainQuantity"    -> bigDecimal(
        quantityMaxDecimalPlaces,
        "grainsUsed.error.unmaltedGrainQuantity.required",
        "grainsUsed.error.unmaltedGrainQuantity.nonNumeric",
        "grainsUsed.error.unmaltedGrainQuantity.decimalPlaces"
      ).verifying(minimumValue(quantityMinValue, "grainsUsed.error.unmaltedGrainQuantity.minimumRequired"))
        .verifying(maximumValue(quantityMaxValue, "grainsUsed.error.unmaltedGrainQuantity.maximumRequired")),
      "usedMaltedGrainNotBarley" -> boolean("grainsUsed.error.usedMaltedGrainNotBarley.required")
    )(GrainsUsed.apply)(GrainsUsed.unapply)
  )
}

object GrainsUsedFormProvider {
  val quantityMaxDecimalPlaces = 2
  val quantityMinValue         = BigDecimal(0.00)
  val quantityMaxValue         = BigDecimal(999999999.99)
}
