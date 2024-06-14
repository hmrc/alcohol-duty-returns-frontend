package forms.adjustment

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class AdjustmentRepackagedTaxTypeWithSPRFormProviderSpec extends StringFieldBehaviours {

  val form = new AdjustmentRepackagedTaxTypeWithSPRFormProvider()()

  ".tax-type-code" - {

    val fieldName = "tax-type-code"
    val requiredKey = "adjustmentRepackagedTaxTypeWithSPR.error.tax-type-code.required"
    val lengthKey = "adjustmentRepackagedTaxTypeWithSPR.error.tax-type-code.length"
    val maxLength = 999

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

  ".new-spr-duty-rate" - {

    val fieldName = "new-spr-duty-rate"
    val requiredKey = "adjustmentRepackagedTaxTypeWithSPR.error.new-spr-duty-rate.required"
    val lengthKey = "adjustmentRepackagedTaxTypeWithSPR.error.new-spr-duty-rate.length"
    val maxLength = 999

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
