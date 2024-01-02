package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class DeclareAlcoholDutyQuestionFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "declareAlcoholDutyQuestion.error.required"
  val invalidKey  = "error.boolean"

  val form = new DeclareAlcoholDutyQuestionFormProvider()()

  ".value" - {

    val fieldName = "declareAlcoholDutyQuestion-yesNoValue"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
