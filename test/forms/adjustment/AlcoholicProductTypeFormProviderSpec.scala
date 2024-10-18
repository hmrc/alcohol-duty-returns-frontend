package forms.adjustment

import forms.behaviours.OptionFieldBehaviours
import models.adjustment.AlcoholicProductType
import play.api.data.FormError

class AlcoholicProductTypeFormProviderSpec extends OptionFieldBehaviours {

  val form = new AlcoholicProductTypeFormProvider()()

  ".value" - {

    val fieldName = "alcoholic-product-type-value"
    val requiredKey = "alcoholicProductType.error.required"

    behave like optionsField[AlcoholicProductType](
      form,
      fieldName,
      validValues  = AlcoholicProductType.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
