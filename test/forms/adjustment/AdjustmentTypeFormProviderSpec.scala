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

import forms.behaviours.EnumFieldBehaviours
import models.adjustment.AdjustmentType
import play.api.data.FormError

class AdjustmentTypeFormProviderSpec extends EnumFieldBehaviours {

  val form = new AdjustmentTypeFormProvider()()

  ".value" - {

    val fieldName   = "adjustment-type-value"
    val requiredKey = "adjustmentType.error.required"

    behave like enumField[AdjustmentType](
      form,
      fieldName,
      validValues = AdjustmentType.values
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
