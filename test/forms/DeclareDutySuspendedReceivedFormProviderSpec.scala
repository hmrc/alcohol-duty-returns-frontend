package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class DeclareDutySuspendedReceivedFormProviderSpec extends IntFieldBehaviours {

  val form = new DeclareDutySuspendedReceivedFormProvider()()

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
      nonNumericError  = FormError(fieldName, "declareDutySuspendedReceived.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "declareDutySuspendedReceived.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "declareDutySuspendedReceived.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "declareDutySuspendedReceived.error.required")
    )
  }
}
