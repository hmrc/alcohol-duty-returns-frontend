package forms.$section$

import javax.inject.Inject
import forms.mappings.Mappings
import models.$section$.$className$$pre_imports$
import play.api.data.Form
import play.api.data.Forms._

class $className$FormProvider @Inject() extends Mappings {
  import $className$FormProvider._

  def apply(): Form[$className$] = Form(
    mapping($pre_mappings$
    )($className$.apply)($className$.unapply)
  )
}

object $className$FormProvider {$pre_limits$
}
