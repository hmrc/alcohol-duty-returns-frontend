package forms.spiritsQuestions

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class AlcoholUsedFormProviderSpec extends StringFieldBehaviours {

  val form = new AlcoholUsedFormProvider()()

  ".beer" - {

    val fieldName = "beer"
    val requiredKey = "alcoholUsed.error.beer.required"
    val lengthKey = "alcoholUsed.error.beer.length"
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

  ".wine" - {

    val fieldName = "wine"
    val requiredKey = "alcoholUsed.error.wine.required"
    val lengthKey = "alcoholUsed.error.wine.length"
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
