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

import forms.behaviours.BigDecimalFieldBehaviours
import play.api.data.FormError
import scala.collection.immutable.ArraySeq

class AdjustmentSmallProducerReliefDutyRateFormProviderSpec extends BigDecimalFieldBehaviours {

  val form = new AdjustmentSmallProducerReliefDutyRateFormProvider()()

  ".value" - {

    val fieldName = "adjustment-small-producer-relief-duty-rate-input"

    val minimum = 0.00
    val maximum = 999999999.99
    val decimal = 2

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, decimal)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "adjustmentSmallProducerReliefDutyRate.error.nonNumeric"),
      decimalPlacesError = FormError(fieldName, "adjustmentSmallProducerReliefDutyRate.error.decimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError =
        FormError(fieldName, "adjustmentSmallProducerReliefDutyRate.error.minimumRequired", ArraySeq(minimum))
    )
    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError =
        FormError(fieldName, "adjustmentSmallProducerReliefDutyRate.error.maximumRequired", ArraySeq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "adjustmentSmallProducerReliefDutyRate.error.required")
    )
  }
}
