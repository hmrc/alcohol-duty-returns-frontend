  ".$pre_fieldname$" - {
    val fieldName   = "$pre_fieldname$"
    val requiredKey = "$className;format="decap"$.error.$pre_fieldname$.required"
    val minimum     = $field$pre_index$Min$
    val maximum     = $field$pre_index$Max$
    val decimal     = $field$pre_index$MaxDp$

    val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, decimal)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like bigDecimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "$className;format="decap"$.error.$pre_fieldname$.nonNumeric"),
      twoDecimalPlacesError = FormError(fieldName, "$className;format="decap"$.error.$pre_fieldname$.decimalPlaces")
    )

    behave like bigDecimalFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      decimal = decimal,
      expectedError =
        FormError(fieldName, "$className;format="decap"$.error.$pre_fieldname$.minimumRequired", Seq(minimum))
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      decimal = decimal,
      expectedError =
        FormError(fieldName, "$className;format="decap"$.error.$pre_fieldname$.maximumRequired", Seq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
