package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class RyeUsedFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "ryeUsed-input" -> int(
        "ryeUsed.error.required",
        "ryeUsed.error.wholeNumber",
        "ryeUsed.error.nonNumeric")
          .verifying(inRange(0, 999999999.99, "ryeUsed.error.outOfRange"))
    )
}
