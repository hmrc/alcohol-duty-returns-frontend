package forms.spiritsQuestions

import OtherSpiritsProducedFormProvider.{length => maxLength}
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class OtherSpiritsProducedFormProviderSpec extends StringFieldBehaviours {
  val requiredKey = "otherSpiritsProduced.error.required"
  val lengthKey = "otherSpiritsProduced.error.length"

  val form = new OtherSpiritsProducedFormProvider()()

  ".value" - {

    val fieldName = "otherSpiritsProduced"

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
