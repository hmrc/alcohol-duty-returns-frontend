package forms.adjustment

import forms.behaviours.OptionFieldBehaviours
import models.adjustment.AdjustmentType
import play.api.data.FormError

class AdjustmentTypeFormProviderSpec extends OptionFieldBehaviours {

  val form = new AdjustmentTypeFormProvider()()

  ".value" - {

    val fieldName = "adjustment-type-value"
    val requiredKey = "adjustmentType.error.required"

    behave like optionsField[AdjustmentType](
      form,
      fieldName,
      validValues  = AdjustmentType.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
