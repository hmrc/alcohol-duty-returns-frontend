package forms.adjustment

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class AdjustmentTaxTypeFormProviderSpec extends IntFieldBehaviours {

  val form = new AdjustmentTaxTypeFormProvider()()

  ".value" - {

    val fieldName = "adjustmentTaxType-input"

    val minimum = 100
    val maximum = 999

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "adjustmentTaxType.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "adjustmentTaxType.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum = minimum,
      maximum = maximum,
      expectedError = FormError(fieldName, "adjustmentTaxType.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "adjustmentTaxType.error.required")
    )
  }
}
