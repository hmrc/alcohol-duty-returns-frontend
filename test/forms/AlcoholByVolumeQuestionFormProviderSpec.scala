/*
 * Copyright 2023 HM Revenue & Customs
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

package forms

import forms.behaviours.BigDecimalFieldBehaviours
import play.api.data.FormError

class AlcoholByVolumeQuestionFormProviderSpec extends BigDecimalFieldBehaviours {

  val form = new AlcoholByVolumeQuestionFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 0
    val maximum = 100

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "alcoholByVolumeQuestion.error.nonNumeric"),
      twoDecimalPlacesError = FormError(fieldName, "alcoholByVolumeQuestion.error.twoDecimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      expectedError = FormError(fieldName, "alcoholByVolumeQuestion.error.minimumRequired")
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      expectedError = FormError(fieldName, "alcoholByVolumeQuestion.error.maximumRequired")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "alcoholByVolumeQuestion.error.required")
    )
  }
}
