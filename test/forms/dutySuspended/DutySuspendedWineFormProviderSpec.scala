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

package forms.dutySuspended

import forms.behaviours.BigDecimalFieldBehaviours
import play.api.data.FormError

import scala.collection.immutable.ArraySeq

class DutySuspendedWineFormProviderSpec extends BigDecimalFieldBehaviours {

  val form = new DutySuspendedWineFormProvider()()

  ".totalWine" - {

    val fieldName   = "totalWine"
    val requiredKey = "dutySuspendedWine.error.totalWine.required"
    val minimum     = -999999999.99
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
      nonNumericError = FormError(fieldName, "dutySuspendedWine.error.totalWine.nonNumeric"),
      decimalPlacesError = FormError(fieldName, "dutySuspendedWine.error.totalWine.decimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError = FormError(fieldName, "dutySuspendedWine.error.totalWine.minimumRequired", ArraySeq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError = FormError(fieldName, "dutySuspendedWine.error.totalWine.maximumRequired", ArraySeq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".pureAlcoholInWine" - {

    val fieldName   = "pureAlcoholInWine"
    val requiredKey = "dutySuspendedWine.error.pureAlcoholInWine.required"
    val minimum     = -999999999.9999
    val maximum     = 999999999.9999
    val decimal     = 4

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, decimal)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "dutySuspendedWine.error.pureAlcoholInWine.nonNumeric"),
      decimalPlacesError = FormError(fieldName, "dutySuspendedWine.error.pureAlcoholInWine.decimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError =
        FormError(fieldName, "dutySuspendedWine.error.pureAlcoholInWine.minimumRequired", ArraySeq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError =
        FormError(fieldName, "dutySuspendedWine.error.pureAlcoholInWine.maximumRequired", ArraySeq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
