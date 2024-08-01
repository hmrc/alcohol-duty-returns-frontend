package forms.adjustment

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class UnderDeclarationReasonFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "underDeclarationReason.error.required"
  val lengthKey = "underDeclarationReason.error.length"
  val maxLength = 250

  val form = new UnderDeclarationReasonFormProvider()()

  ".value" - {

    val fieldName = "underDeclarationReason-input"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
