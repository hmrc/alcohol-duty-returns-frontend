package forms.returns

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class MultipleSPRListFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "multipleSPRList.error.required"
  val invalidKey  = "error.boolean"

  val form = new MultipleSPRListFormProvider()()

  ".value" - {

    val fieldName = "multipleSPRList-yesNoValue"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
