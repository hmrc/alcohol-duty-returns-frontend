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

package forms.declareDuty

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class MultipleSPRMissingDetailsConfirmationFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKeyPlural   = "multipleSPRMissingDetailsConfirmation.error.required.plural"
  val requiredKeySingular = "multipleSPRMissingDetailsConfirmation.error.required.singular"
  val invalidKey          = "error.boolean"

  val formPlural   = new MultipleSPRMissingDetailsConfirmationFormProvider()(true)
  val formSingular = new MultipleSPRMissingDetailsConfirmationFormProvider()(false)

  ".value" - {

    val fieldName = "deleteMissingDeclarations"

    "plural form must" - {
      behave like booleanField(
        formPlural,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        formPlural,
        fieldName,
        requiredError = FormError(fieldName, requiredKeyPlural)
      )
    }

    "singular form must" - {
      behave like booleanField(
        formSingular,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        formSingular,
        fieldName,
        requiredError = FormError(fieldName, requiredKeySingular)
      )
    }
  }
}
