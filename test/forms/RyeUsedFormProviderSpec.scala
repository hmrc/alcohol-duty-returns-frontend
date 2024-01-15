package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class RyeUsedFormProviderSpec extends IntFieldBehaviours {

  val form = new RyeUsedFormProvider()()

  ".value" - {

    val fieldName = "ryeUsed-input"

    val minimum = 0
    val maximum = 999999999.99

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "ryeUsed.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "ryeUsed.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "ryeUsed.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "ryeUsed.error.required")
    )
  }
}
