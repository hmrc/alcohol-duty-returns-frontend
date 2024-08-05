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

class OtherMaltedGrainsFormProviderSpec extends StringFieldBehaviours with BigDecimalFieldBehaviours {

  val form = new OtherMaltedGrainsFormProvider()()

  ".otherMaltedGrainsTypes" - {

    val fieldName   = "otherMaltedGrainsTypes"
    val requiredKey = "otherMaltedGrains.error.otherMaltedGrainsTypes.required"
    val lengthKey   = "otherMaltedGrains.error.otherMaltedGrainsTypes.length"
    val maxLength   = 120

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

  ".otherMaltedGrainsQuantity" - {
    val fieldName   = "otherMaltedGrainsQuantity"
    val requiredKey = "otherMaltedGrains.error.otherMaltedGrainsQuantity.required"
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
      nonNumericError = FormError(fieldName, "otherMaltedGrains.error.otherMaltedGrainsQuantity.nonNumeric"),
      decimalPlacesError = FormError(fieldName, "otherMaltedGrains.error.otherMaltedGrainsQuantity.decimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError =
        FormError(fieldName, "otherMaltedGrains.error.otherMaltedGrainsQuantity.minimumRequired", Seq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError =
        FormError(fieldName, "otherMaltedGrains.error.otherMaltedGrainsQuantity.maximumRequired", Seq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
