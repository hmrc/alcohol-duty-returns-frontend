package forms.$section$

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "$className;format="decap"$-input" -> text("$className;format="decap"$.error.required")
        .verifying(maxLength($maxLength$, "$className;format="decap"$.error.length"))
    )
}
