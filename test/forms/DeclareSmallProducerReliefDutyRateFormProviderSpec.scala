package forms

import forms.behaviours.BigDecimalFieldBehaviours
import play.api.data.FormError
import scala.collection.immutable.ArraySeq

class DeclareSmallProducerReliefDutyRateFormProviderSpec extends BigDecimalFieldBehaviours {

  val form = new DeclareSmallProducerReliefDutyRateFormProvider()()

  ".value" - {

    val fieldName = "declareSmallProducerReliefDutyRate-input"

    val minimum = 0.01
    val maximum = 999999999.99

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "declareSmallProducerReliefDutyRate.error.nonNumeric"),
      twoDecimalPlacesError = FormError(fieldName, "declareSmallProducerReliefDutyRate.error.twoDecimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum       = minimum,
      expectedError = FormError(fieldName, "declareSmallProducerReliefDutyRate.error.minimumRequired", ArraySeq(minimum))
    )
    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      expectedError = FormError(fieldName, "declareSmallProducerReliefDutyRate.error.maximumRequired", ArraySeq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "declareSmallProducerReliefDutyRate.error.required")
    )
  }
}
