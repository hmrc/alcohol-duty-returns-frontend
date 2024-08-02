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
import models.UnitsOfMeasure
import play.api.data.Form
import play.api.data.Forms._
import models.spiritsQuestions.OtherIngredientsUsed

class OtherIngredientsUsedFormProvider @Inject() extends Mappings {
  import OtherIngredientsUsedFormProvider._

  def apply(): Form[OtherIngredientsUsed] = Form(
    mapping(
      "otherIngredientsUsedTypes"    -> text("otherIngredientsUsed.error.otherIngredientsUsedTypes.required")
        .verifying(
          maxLength(otherIngredientTypesMaxLength, "otherIngredientsUsed.error.otherIngredientsUsedTypes.length")
        ),
      "otherIngredientsUsedUnit"     -> UnitsOfMeasure.formField,
      "otherIngredientsUsedQuantity" -> bigDecimal(
        quantityMaxDecimalPlaces,
        "otherIngredientsUsed.error.otherIngredientsUsedQuantity.required",
        "otherIngredientsUsed.error.otherIngredientsUsedQuantity.nonNumeric",
        "otherIngredientsUsed.error.otherIngredientsUsedQuantity.decimalPlaces"
      ).verifying(
        minimumValue(quantityMinValue, "otherIngredientsUsed.error.otherIngredientsUsedQuantity.minimumRequired")
      ).verifying(
        maximumValue(quantityMaxValue, "otherIngredientsUsed.error.otherIngredientsUsedQuantity.maximumRequired")
      )
    )(OtherIngredientsUsed.apply)(OtherIngredientsUsed.unapply)
  )
}

object OtherIngredientsUsedFormProvider {
  val otherIngredientTypesMaxLength = 120
  val quantityMaxDecimalPlaces      = 2
  val quantityMinValue              = BigDecimal(0.01)
  val quantityMaxValue              = BigDecimal(999999999.99)
}
