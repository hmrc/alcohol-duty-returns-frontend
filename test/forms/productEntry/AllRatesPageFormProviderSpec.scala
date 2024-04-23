package forms.productEntry

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class AllRatesPageFormProviderSpec extends StringFieldBehaviours {

  val form = new AllRatesPageFormProvider()()

  ".bulkVolume" - {

    val fieldName   = "bulkVolume"
    val requiredKey = "allRatesPage.error.bulkVolume.required"
    val lengthKey   = "allRatesPage.error.bulkVolume.length"
    val maxLength   = 90

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

  ".pureAlcoholVolume" - {

    val fieldName   = "pureAlcoholVolume"
    val requiredKey = "allRatesPage.error.pureAlcoholVolume.required"
    val lengthKey   = "allRatesPage.error.pureAlcoholVolume.length"
    val maxLength   = 9

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
