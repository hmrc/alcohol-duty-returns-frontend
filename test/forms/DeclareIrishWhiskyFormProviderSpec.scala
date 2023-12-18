package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class DeclareIrishWhiskyFormProviderSpec extends IntFieldBehaviours {

  val form = new DeclareIrishWhiskyFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 0
    val maximum = 999999999

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "declareIrishWhisky.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "declareIrishWhisky.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "declareIrishWhisky.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "declareIrishWhisky.error.required")
    )
  }
}
