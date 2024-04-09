package forms.$section$

import forms.behaviours.BigDecimalFieldBehaviours
import play.api.data.FormError
import scala.collection.immutable.ArraySeq

class $className$FormProviderSpec extends BigDecimalFieldBehaviours {

  val form = new $className$FormProvider()()

  ".value" - {

    val fieldName = "$className;format="decap"$-input"

    val minimum = $minimum$
    val maximum = $maximum$
    val decimal = 2

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, decimal)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "$className;format="decap"$.error.nonNumeric"),
      twoDecimalPlacesError = FormError(fieldName, "$className;format="decap"$.error.twoDecimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum       = minimum,
      decimal = decimal,
      expectedError = FormError(fieldName, "$className;format="decap"$.error.minimumRequired", ArraySeq(minimum))
    )
    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError = FormError(fieldName, "$className;format="decap"$.error.maximumRequired", ArraySeq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "$className;format="decap"$.error.required")
    )
  }
}
