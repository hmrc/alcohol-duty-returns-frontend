package forms.spiritsQuestions

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class EthyleneGasOrMolassesUsedFormProviderSpec extends StringFieldBehaviours {

  val form = new EthyleneGasOrMolassesUsedFormProvider()()

  ".ethyleneGas" - {

    val fieldName = "ethyleneGas"
    val requiredKey = "ethyleneGasOrMolassesUsed.error.ethyleneGas.required"
    val lengthKey = "ethyleneGasOrMolassesUsed.error.ethyleneGas.length"
    val maxLength = 100

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

  ".molasses" - {

    val fieldName = "molasses"
    val requiredKey = "ethyleneGasOrMolassesUsed.error.molasses.required"
    val lengthKey = "ethyleneGasOrMolassesUsed.error.molasses.length"
    val maxLength = 100

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
