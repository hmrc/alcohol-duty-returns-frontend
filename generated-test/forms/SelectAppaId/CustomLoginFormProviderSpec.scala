package forms.SelectAppaId

import forms.behaviours.CheckboxFieldBehaviours
import models.SelectAppaId.CustomLogin
import play.api.data.FormError

class CustomLoginFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new CustomLoginFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "customLogin.error.required"

    behave like checkboxField[CustomLogin](
      form,
      fieldName,
      validValues  = CustomLogin.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
