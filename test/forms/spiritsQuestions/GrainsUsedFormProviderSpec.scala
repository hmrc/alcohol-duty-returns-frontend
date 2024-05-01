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

import forms.behaviours.{BigDecimalFieldBehaviours, BooleanFieldBehaviours}
import play.api.data.FormError

class GrainsUsedFormProviderSpec extends BigDecimalFieldBehaviours with BooleanFieldBehaviours {

  val form = new GrainsUsedFormProvider()()

  ".maltedBarleyQuantity" - {
    val fieldName   = "maltedBarleyQuantity"
    val requiredKey = "grainsUsed.error.maltedBarleyQuantity.required"
    val minimum     = 0
    val maximum     = 999999999.99
    val decimal     = 2

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, decimal)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "grainsUsed.error.maltedBarleyQuantity.nonNumeric"),
      twoDecimalPlacesError = FormError(fieldName, "grainsUsed.error.maltedBarleyQuantity.twoDecimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError = FormError(fieldName, "grainsUsed.error.maltedBarleyQuantity.minimumRequired", Seq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError = FormError(fieldName, "grainsUsed.error.maltedBarleyQuantity.maximumRequired", Seq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".wheatQuantity" - {
    val fieldName   = "wheatQuantity"
    val requiredKey = "grainsUsed.error.wheatQuantity.required"
    val minimum     = 0
    val maximum     = 999999999.99
    val decimal     = 2

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, decimal)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "grainsUsed.error.wheatQuantity.nonNumeric"),
      twoDecimalPlacesError = FormError(fieldName, "grainsUsed.error.wheatQuantity.twoDecimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError = FormError(fieldName, "grainsUsed.error.wheatQuantity.minimumRequired", Seq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError = FormError(fieldName, "grainsUsed.error.wheatQuantity.maximumRequired", Seq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".maizeQuantity" - {
    val fieldName   = "maizeQuantity"
    val requiredKey = "grainsUsed.error.maizeQuantity.required"
    val minimum     = 0
    val maximum     = 999999999.99
    val decimal     = 2

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, decimal)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "grainsUsed.error.maizeQuantity.nonNumeric"),
      twoDecimalPlacesError = FormError(fieldName, "grainsUsed.error.maizeQuantity.twoDecimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError = FormError(fieldName, "grainsUsed.error.maizeQuantity.minimumRequired", Seq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError = FormError(fieldName, "grainsUsed.error.maizeQuantity.maximumRequired", Seq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".ryeQuantity" - {
    val fieldName   = "ryeQuantity"
    val requiredKey = "grainsUsed.error.ryeQuantity.required"
    val minimum     = 0
    val maximum     = 999999999.99
    val decimal     = 2

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, decimal)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "grainsUsed.error.ryeQuantity.nonNumeric"),
      twoDecimalPlacesError = FormError(fieldName, "grainsUsed.error.ryeQuantity.twoDecimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError = FormError(fieldName, "grainsUsed.error.ryeQuantity.minimumRequired", Seq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError = FormError(fieldName, "grainsUsed.error.ryeQuantity.maximumRequired", Seq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".unmaltedGrainQuantity" - {
    val fieldName   = "unmaltedGrainQuantity"
    val requiredKey = "grainsUsed.error.unmaltedGrainQuantity.required"
    val minimum     = 0
    val maximum     = 999999999.99
    val decimal     = 2

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, decimal)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "grainsUsed.error.unmaltedGrainQuantity.nonNumeric"),
      twoDecimalPlacesError = FormError(fieldName, "grainsUsed.error.unmaltedGrainQuantity.twoDecimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError = FormError(fieldName, "grainsUsed.error.unmaltedGrainQuantity.minimumRequired", Seq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError = FormError(fieldName, "grainsUsed.error.unmaltedGrainQuantity.maximumRequired", Seq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".usedMaltedGrainNotBarley" - {
    val fieldName   = "usedMaltedGrainNotBarley"
    val requiredKey = "grainsUsed.error.usedMaltedGrainNotBarley.required"
    val invalidKey  = "error.boolean"

    behave like booleanField(
      form = form,
      fieldName = fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form = form,
      fieldName = fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
