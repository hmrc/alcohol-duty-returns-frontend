  ".$pre_fieldname$" - {
    val fieldName   = "$pre_fieldname$"
    val requiredKey = "$className;format="decap"$.error.$pre_fieldname$.required"
    val lengthKey = "$className;format="decap"$.error.$pre_fieldname$.length"
    val maxLength = $field$pre_index$Length$

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
