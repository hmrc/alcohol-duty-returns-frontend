package forms.productEntry

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class ProductListFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "productList.error.required"
  val invalidKey = "error.boolean"

  val form = new ProductListFormProvider()()

  ".value" - {

    val fieldName = "productList-yesNoValue"

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
