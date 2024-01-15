package forms.spiritsQuestions

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class RyeIngredientFormProviderSpec extends IntFieldBehaviours {

  val form = new RyeIngredientFormProvider()()

  ".value" - {

    val fieldName = "ryeIngredient-input"

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
      nonNumericError  = FormError(fieldName, "ryeIngredient.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "ryeIngredient.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "ryeIngredient.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "ryeIngredient.error.required")
    )
  }
}
