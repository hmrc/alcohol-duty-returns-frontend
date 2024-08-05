package forms.$section$

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[BigDecimal] =
    Form(
      "$className;format="decap"$-input" -> bigDecimal(
         2,
        "$className;format="decap"$.error.required",
        "$className;format="decap"$.error.nonNumeric",
        "$className;format="decap"$.error.decimalPlaces"
        )
          .verifying(minimumValue(BigDecimal($minimum$), "$className;format="decap"$.error.minimumRequired"))
          .verifying(maximumValue(BigDecimal($maximum$), "$className;format="decap"$.error.maximumRequired"))
    )
}
