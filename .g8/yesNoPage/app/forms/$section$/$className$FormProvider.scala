package forms.$section$

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "$className;format="decap"$-yesNoValue" -> boolean("$className;format="decap"$.error.required")
    )
}
