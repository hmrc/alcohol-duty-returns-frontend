package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class DeclareDutySuspendedDeliveriesOutsideUkFormProviderSpec extends IntFieldBehaviours {

  val form = new DeclareDutySuspendedDeliveriesOutsideUkFormProvider()()

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
      nonNumericError  = FormError(fieldName, "declareDutySuspendedDeliveriesOutsideUk.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "declareDutySuspendedDeliveriesOutsideUk.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "declareDutySuspendedDeliveriesOutsideUk.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "declareDutySuspendedDeliveriesOutsideUk.error.required")
    )
  }
}
