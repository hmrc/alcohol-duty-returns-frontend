package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[BigDecimal] =
    Form(
      "$className;format="decap"$-input" -> bigDecimal(
        "$className;format="decap"$.error.required",
        "$className;format="decap"$.error.nonNumeric",
        "$className;format="decap"$.error.twoDecimalPlaces"
        )
          .verifying(minimumValue(BigDecimal(0.00), "$className;format="decap"$.error.minimumRequired"))
          .verifying(maximumValue(BigDecimal(999999999.99), "$className;format="decap"$.error.maximumRequired"))
    )
}
