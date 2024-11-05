package forms.SelectAppaId

import javax.inject.Inject

import forms.mappings.Mappings
import models.CustomLogin
import play.api.data.Form
import play.api.data.Forms.set

class CustomLoginFormProvider @Inject() extends Mappings {

  def apply(): Form[Set[CustomLogin]] =
    Form(
      "value" -> set(enumerable[CustomLogin]("customLogin.error.required")).verifying(nonEmptySet("customLogin.error.required"))
    )
}
