/*
 * Copyright 2025 HM Revenue & Customs
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

class DutySuspendedQuantitiesFormProviderSpec extends BigDecimalFieldBehaviours {

  val regime = regimeGen.sample.value
  val form   = new DutySuspendedQuantitiesFormProvider()(regime)

  ".totalLitresDeliveredInsideUK" - {

    val fieldName   = "totalLitresDeliveredInsideUK"
    val requiredKey = s"dutySuspended.deliveredInsideUK.error.noValue.totalLitres.${regime.regimeMessageKey}"
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
      nonNumericError =
        FormError(fieldName, s"dutySuspended.deliveredInsideUK.error.invalid.totalLitres.${regime.regimeMessageKey}"),
      decimalPlacesError = FormError(
        fieldName,
        s"dutySuspended.deliveredInsideUK.error.decimalPlaces.totalLitres.${regime.regimeMessageKey}"
      )
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError = FormError(
        fieldName,
        s"dutySuspended.deliveredInsideUK.error.minimumValue.totalLitres.${regime.regimeMessageKey}",
        ArraySeq(minimum)
      )
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError = FormError(
        fieldName,
        s"dutySuspended.deliveredInsideUK.error.maximumValue.totalLitres.${regime.regimeMessageKey}",
        ArraySeq(maximum)
      )
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".pureAlcoholDeliveredInsideUK" - {

    val fieldName          = "pureAlcoholDeliveredInsideUK"
    val requiredKey        = "dutySuspended.deliveredInsideUK.error.noValue.pureAlcohol"
    val minimum            = 0
    val maximum            = 999999999.9999
    val decimal            = 4
    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, decimal)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "dutySuspended.deliveredInsideUK.error.invalid.pureAlcohol"),
      decimalPlacesError = FormError(fieldName, "dutySuspended.deliveredInsideUK.error.decimalPlaces.pureAlcohol")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError =
        FormError(fieldName, "dutySuspended.deliveredInsideUK.error.minimumValue.pureAlcohol", ArraySeq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError =
        FormError(fieldName, "dutySuspended.deliveredInsideUK.error.maximumValue.pureAlcohol", ArraySeq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".totalLitresDeliveredOutsideUK" - {

    val fieldName   = "totalLitresDeliveredOutsideUK"
    val requiredKey = s"dutySuspended.deliveredOutsideUK.error.noValue.totalLitres.${regime.regimeMessageKey}"
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
      nonNumericError =
        FormError(fieldName, s"dutySuspended.deliveredOutsideUK.error.invalid.totalLitres.${regime.regimeMessageKey}"),
      decimalPlacesError = FormError(
        fieldName,
        s"dutySuspended.deliveredOutsideUK.error.decimalPlaces.totalLitres.${regime.regimeMessageKey}"
      )
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError = FormError(
        fieldName,
        s"dutySuspended.deliveredOutsideUK.error.minimumValue.totalLitres.${regime.regimeMessageKey}",
        ArraySeq(minimum)
      )
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError = FormError(
        fieldName,
        s"dutySuspended.deliveredOutsideUK.error.maximumValue.totalLitres.${regime.regimeMessageKey}",
        ArraySeq(maximum)
      )
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".pureAlcoholDeliveredOutsideUK" - {

    val fieldName          = "pureAlcoholDeliveredOutsideUK"
    val requiredKey        = "dutySuspended.deliveredOutsideUK.error.noValue.pureAlcohol"
    val minimum            = 0
    val maximum            = 999999999.9999
    val decimal            = 4
    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, decimal)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "dutySuspended.deliveredOutsideUK.error.invalid.pureAlcohol"),
      decimalPlacesError = FormError(fieldName, "dutySuspended.deliveredOutsideUK.error.decimalPlaces.pureAlcohol")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError =
        FormError(fieldName, "dutySuspended.deliveredOutsideUK.error.minimumValue.pureAlcohol", ArraySeq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError =
        FormError(fieldName, "dutySuspended.deliveredOutsideUK.error.maximumValue.pureAlcohol", ArraySeq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".totalLitresReceived" - {

    val fieldName   = "totalLitresReceived"
    val requiredKey = s"dutySuspended.received.error.noValue.totalLitres.${regime.regimeMessageKey}"
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
      nonNumericError =
        FormError(fieldName, s"dutySuspended.received.error.invalid.totalLitres.${regime.regimeMessageKey}"),
      decimalPlacesError = FormError(
        fieldName,
        s"dutySuspended.received.error.decimalPlaces.totalLitres.${regime.regimeMessageKey}"
      )
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError = FormError(
        fieldName,
        s"dutySuspended.received.error.minimumValue.totalLitres.${regime.regimeMessageKey}",
        ArraySeq(minimum)
      )
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError = FormError(
        fieldName,
        s"dutySuspended.received.error.maximumValue.totalLitres.${regime.regimeMessageKey}",
        ArraySeq(maximum)
      )
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".pureAlcoholReceived" - {

    val fieldName          = "pureAlcoholReceived"
    val requiredKey        = "dutySuspended.received.error.noValue.pureAlcohol"
    val minimum            = 0
    val maximum            = 999999999.9999
    val decimal            = 4
    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, decimal)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "dutySuspended.received.error.invalid.pureAlcohol"),
      decimalPlacesError = FormError(fieldName, "dutySuspended.received.error.decimalPlaces.pureAlcohol")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError = FormError(fieldName, "dutySuspended.received.error.minimumValue.pureAlcohol", ArraySeq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError = FormError(fieldName, "dutySuspended.received.error.maximumValue.pureAlcohol", ArraySeq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
