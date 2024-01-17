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

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class UnmaltedGrainUsedFormProvider @Inject() extends Mappings {

  def apply(): Form[BigDecimal] =
    Form(
      "unmaltedGrainUsed-input" -> bigDecimal(
        "unmaltedGrainUsed.error.required",
        "unmaltedGrainUsed.error.nonNumeric",
        "unmaltedGrainUsed.error.twoDecimalPlaces"
      )
        .verifying(minimumValue(BigDecimal(0.00), "unmaltedGrainUsed.error.minimumRequired"))
        .verifying(maximumValue(BigDecimal(999999999.99), "unmaltedGrainUsed.error.maximumRequired"))
    )
}
