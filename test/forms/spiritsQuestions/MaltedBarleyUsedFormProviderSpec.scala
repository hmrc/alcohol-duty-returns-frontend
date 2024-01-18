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
import scala.collection.immutable.ArraySeq

class MaltedBarleyUsedFormProviderSpec extends BigDecimalFieldBehaviours {

  val form = new MaltedBarleyUsedFormProvider()()

  ".value" - {

    val fieldName = "malted-barley-used-input"

    val minimum = 0.0
    val maximum = 999999999.99

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "maltedBarleyUsed.error.nonNumeric"),
      twoDecimalPlacesError = FormError(fieldName, "maltedBarleyUsed.error.twoDecimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      expectedError = FormError(fieldName, "maltedBarleyUsed.error.minimumRequired", ArraySeq(minimum))
    )
    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      expectedError = FormError(fieldName, "maltedBarleyUsed.error.maximumRequired", ArraySeq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "maltedBarleyUsed.error.required")
    )
  }
}
