package forms.spiritsQuestions

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class UnmaltedGrainUsedFormProviderSpec extends IntFieldBehaviours {

  val form = new UnmaltedGrainUsedFormProvider()()

  ".value" - {

    val fieldName = "unmaltedGrainUsed-input"

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
      nonNumericError  = FormError(fieldName, "unmaltedGrainUsed.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "unmaltedGrainUsed.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "unmaltedGrainUsed.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "unmaltedGrainUsed.error.required")
    )
  }
}
