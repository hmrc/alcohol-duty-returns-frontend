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

import forms.behaviours.BigDecimalFieldBehaviours
import play.api.data.FormError

import scala.collection.compat.immutable.ArraySeq

class AlcoholUsedFormProviderSpec extends BigDecimalFieldBehaviours {

  val form = new AlcoholUsedFormProvider()()

  ".beer" - {

    val fieldName = "beer"
    val minimum   = 0.00
    val maximum   = 999999999.99
    val decimal   = 2

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, decimal)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "alcoholUsed.error.beer.nonNumeric"),
      decimalPlacesError = FormError(fieldName, "alcoholUsed.error.beer.decimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError = FormError(fieldName, "alcoholUsed.error.beer.minimumRequired", ArraySeq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError = FormError(fieldName, "alcoholUsed.error.beer.maximumRequired", ArraySeq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "alcoholUsed.error.beer.required")
    )
  }

  ".wine" - {

    val fieldName = "wine"
    val minimum   = 0.00
    val maximum   = 999999999.99
    val decimal   = 2

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, decimal)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "alcoholUsed.error.wine.nonNumeric"),
      decimalPlacesError = FormError(fieldName, "alcoholUsed.error.wine.decimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError = FormError(fieldName, "alcoholUsed.error.wine.minimumRequired", ArraySeq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError = FormError(fieldName, "alcoholUsed.error.wine.maximumRequired", ArraySeq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "alcoholUsed.error.wine.required")
    )
  }

  ".madeWine" - {

    val fieldName = "madeWine"
    val minimum   = 0.00
    val maximum   = 999999999.99
    val decimal   = 2

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, decimal)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "alcoholUsed.error.madeWine.nonNumeric"),
      decimalPlacesError = FormError(fieldName, "alcoholUsed.error.madeWine.decimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError = FormError(fieldName, "alcoholUsed.error.madeWine.minimumRequired", ArraySeq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError = FormError(fieldName, "alcoholUsed.error.madeWine.maximumRequired", ArraySeq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "alcoholUsed.error.madeWine.required")
    )
  }

  ".ciderOrPerry" - {

    val fieldName = "ciderOrPerry"
    val minimum   = 0.00
    val maximum   = 999999999.99
    val decimal   = 2

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, decimal)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "alcoholUsed.error.ciderOrPerry.nonNumeric"),
      decimalPlacesError = FormError(fieldName, "alcoholUsed.error.ciderOrPerry.decimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError = FormError(fieldName, "alcoholUsed.error.ciderOrPerry.minimumRequired", ArraySeq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError = FormError(fieldName, "alcoholUsed.error.ciderOrPerry.maximumRequired", ArraySeq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "alcoholUsed.error.ciderOrPerry.required")
    )
  }
}
