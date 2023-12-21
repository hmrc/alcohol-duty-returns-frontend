package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import models.$className$

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[$className$] =
    Form(
      "$className;format="decap"$-yesNoValue" -> enumerable[$className$]("$className;format="decap"$.error.required")
    )
}
