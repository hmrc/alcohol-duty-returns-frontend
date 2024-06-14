package forms.adjustment

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class AdjustmentRepackagedTaxTypeFormProviderSpec extends IntFieldBehaviours {

  val form = new AdjustmentRepackagedTaxTypeFormProvider()()

  ".value" - {

    val fieldName = "adjustmentRepackagedTaxType-input"

    val minimum = 0
    val maximum = 100

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "adjustmentRepackagedTaxType.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "adjustmentRepackagedTaxType.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "adjustmentRepackagedTaxType.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "adjustmentRepackagedTaxType.error.required")
    )
  }
}
