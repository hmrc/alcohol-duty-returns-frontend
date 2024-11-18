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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class OtherSpiritsProducedFormProviderSpec extends StringFieldBehaviours {
  val requiredKey = "otherSpiritsProduced.error.required"
  val lengthKey   = "otherSpiritsProduced.error.length"
  val maxLength   = 120

  val form = new OtherSpiritsProducedFormProvider()()

  ".value" - {

    val fieldName = "otherSpiritsProduced"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      alphaNumericStringsWithMaxLength(maxLength)
    )

    behave like alphanumericFieldWithMaxLength(
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
}
