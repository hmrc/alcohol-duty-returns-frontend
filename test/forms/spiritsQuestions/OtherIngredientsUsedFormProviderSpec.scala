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

import forms.behaviours.{BigDecimalFieldBehaviours, StringFieldBehaviours}
import play.api.data.FormError

class OtherIngredientsUsedFormProviderSpec extends StringFieldBehaviours with BigDecimalFieldBehaviours {

  val form = new OtherIngredientsUsedFormProvider()()

  ".otherIngredientsUsedTypes" - {

    val fieldName = "otherIngredientsUsedTypes"
    val requiredKey = "otherIngredientsUsed.error.otherIngredientsUsedTypes.required"
    val lengthKey = "otherIngredientsUsed.error.otherIngredientsUsedTypes.length"
    val maxLength = 120

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".otherIngredientsUsedUnit" - {

    val fieldName = "otherIngredientsUsedUnit"
    val requiredKey = "otherIngredientsUsed.error.otherIngredientsUsedUnit.required"
    val lengthKey = "otherIngredientsUsed.error.otherIngredientsUsedUnit.length"
    val maxLength = 100

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".otherIngredientsUsedQuantity" - {

    val fieldName = "otherIngredientsUsedQuantity"
    val requiredKey = "otherIngredientsUsed.error.otherIngredientsUsedQuantity.required"
    val minimum     = 0.01
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
      nonNumericError = FormError(fieldName, "otherIngredientsUsed.error.otherIngredientsUsedQuantity.nonNumeric"),
      twoDecimalPlacesError = FormError(fieldName, "otherIngredientsUsed.error.otherIngredientsUsedQuantity.twoDecimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError =
        FormError(fieldName, "otherIngredientsUsed.error.otherIngredientsUsedQuantity.minimumRequired", Seq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError =
        FormError(fieldName, "otherIngredientsUsed.error.otherIngredientsUsedQuantity.maximumRequired", Seq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
