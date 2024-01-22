package forms.$section$

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import models.$section$.$className$

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[$className$] =
    Form(
      "$className;format="decap"$-value" -> enumerable[$className$]("$className;format="decap"$.error.required")
    )
}
