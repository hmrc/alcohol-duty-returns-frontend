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

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.spiritsQuestions.EthyleneGasOrMolassesUsed

class EthyleneGasOrMolassesUsedFormProvider @Inject() extends Mappings {

  def apply(): Form[EthyleneGasOrMolassesUsed] = Form(
    mapping(
      "ethyleneGas"                 -> bigDecimal(
        2,
        "ethyleneGasOrMolassesUsed.error.ethyleneGas.required",
        "ethyleneGasOrMolassesUsed.error.ethyleneGas.nonNumeric",
        "ethyleneGasOrMolassesUsed.error.ethyleneGas.twoDecimalPlaces"
      ).verifying(minimumValue(BigDecimal(0.00), "ethyleneGasOrMolassesUsed.error.ethyleneGas.minimumRequired"))
        .verifying(
          maximumValue(BigDecimal(999999999.99), "ethyleneGasOrMolassesUsed.error.ethyleneGas.maximumRequired")
        ),
      "molasses"                    -> bigDecimal(
        2,
        "ethyleneGasOrMolassesUsed.error.molasses.required",
        "ethyleneGasOrMolassesUsed.error.molasses.nonNumeric",
        "ethyleneGasOrMolassesUsed.error.molasses.twoDecimalPlaces"
      ).verifying(minimumValue(BigDecimal(0.00), "ethyleneGasOrMolassesUsed.error.molasses.minimumRequired"))
        .verifying(maximumValue(BigDecimal(999999999.99), "ethyleneGasOrMolassesUsed.error.molasses.maximumRequired")),
      "otherIngredients-yesNoValue" -> boolean("ethyleneGasOrMolassesUsed.error.required")
    )(EthyleneGasOrMolassesUsed.apply)(EthyleneGasOrMolassesUsed.unapply)
  )
}
