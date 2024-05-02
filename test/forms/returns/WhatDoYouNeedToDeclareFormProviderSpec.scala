package forms.returns

import forms.behaviours.CheckboxFieldBehaviours
import models.returns.WhatDoYouNeedToDeclare
import play.api.data.FormError

class WhatDoYouNeedToDeclareFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new WhatDoYouNeedToDeclareFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "whatDoYouNeedToDeclare.error.required"

    behave like checkboxField[WhatDoYouNeedToDeclare](
      form,
      fieldName,
      validValues = WhatDoYouNeedToDeclare.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
