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

import config.Constants

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.spiritsQuestions.GrainsUsed

class GrainsUsedFormProvider @Inject() extends Mappings {
  def apply(): Form[GrainsUsed] = Form(
    mapping(
      "maltedBarleyQuantity"     -> bigDecimal(
        Constants.maximumDecimalPlaces,
        "grainsUsed.error.maltedBarleyQuantity.required",
        "grainsUsed.error.maltedBarleyQuantity.nonNumeric",
        "grainsUsed.error.maltedBarleyQuantity.decimalPlaces"
      ).verifying(
        minimumValue(Constants.volumeMinimumValueIncZero, "grainsUsed.error.maltedBarleyQuantity.minimumRequired")
      ).verifying(maximumValue(Constants.volumeMaximumValue, "grainsUsed.error.maltedBarleyQuantity.maximumRequired")),
      "wheatQuantity"            -> bigDecimal(
        Constants.maximumDecimalPlaces,
        "grainsUsed.error.wheatQuantity.required",
        "grainsUsed.error.wheatQuantity.nonNumeric",
        "grainsUsed.error.wheatQuantity.decimalPlaces"
      ).verifying(minimumValue(Constants.volumeMinimumValueIncZero, "grainsUsed.error.wheatQuantity.minimumRequired"))
        .verifying(maximumValue(Constants.volumeMaximumValue, "grainsUsed.error.wheatQuantity.maximumRequired")),
      "maizeQuantity"            -> bigDecimal(
        Constants.maximumDecimalPlaces,
        "grainsUsed.error.maizeQuantity.required",
        "grainsUsed.error.maizeQuantity.nonNumeric",
        "grainsUsed.error.maizeQuantity.decimalPlaces"
      ).verifying(minimumValue(Constants.volumeMinimumValueIncZero, "grainsUsed.error.maizeQuantity.minimumRequired"))
        .verifying(maximumValue(Constants.volumeMaximumValue, "grainsUsed.error.maizeQuantity.maximumRequired")),
      "ryeQuantity"              -> bigDecimal(
        Constants.maximumDecimalPlaces,
        "grainsUsed.error.ryeQuantity.required",
        "grainsUsed.error.ryeQuantity.nonNumeric",
        "grainsUsed.error.ryeQuantity.decimalPlaces"
      ).verifying(minimumValue(Constants.volumeMinimumValueIncZero, "grainsUsed.error.ryeQuantity.minimumRequired"))
        .verifying(maximumValue(Constants.volumeMaximumValue, "grainsUsed.error.ryeQuantity.maximumRequired")),
      "unmaltedGrainQuantity"    -> bigDecimal(
        Constants.maximumDecimalPlaces,
        "grainsUsed.error.unmaltedGrainQuantity.required",
        "grainsUsed.error.unmaltedGrainQuantity.nonNumeric",
        "grainsUsed.error.unmaltedGrainQuantity.decimalPlaces"
      ).verifying(
        minimumValue(Constants.volumeMinimumValueIncZero, "grainsUsed.error.unmaltedGrainQuantity.minimumRequired")
      ).verifying(maximumValue(Constants.volumeMaximumValue, "grainsUsed.error.unmaltedGrainQuantity.maximumRequired")),
      "usedMaltedGrainNotBarley" -> boolean("grainsUsed.error.usedMaltedGrainNotBarley.required")
    )(GrainsUsed.apply)(GrainsUsed.unapply)
  )
}
