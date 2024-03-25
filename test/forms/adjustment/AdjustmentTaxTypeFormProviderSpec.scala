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

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class AdjustmentTaxTypeFormProviderSpec extends IntFieldBehaviours {

  val form = new AdjustmentTaxTypeFormProvider()()

  ".value" - {

    val fieldName = "adjustmentTaxType-input"

    val minimum = 100
    val maximum = 999

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "adjustmentTaxType.error.valid"),
      wholeNumberError = FormError(fieldName, "adjustmentTaxType.error.valid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "adjustmentTaxType.error.required")
    )
  }
}
