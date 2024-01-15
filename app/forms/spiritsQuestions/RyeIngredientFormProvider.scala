package forms.spiritsQuestions

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class RyeIngredientFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "ryeIngredient-input" -> int(
        "ryeIngredient.error.required",
        "ryeIngredient.error.wholeNumber",
        "ryeIngredient.error.nonNumeric")
          .verifying(inRange(0, 999999999.99, "ryeIngredient.error.outOfRange"))
    )
}
