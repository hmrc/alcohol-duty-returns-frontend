  ".$pre_fieldname$" - {
    val fieldName   = "$pre_fieldname$"
    val requiredKey = "$className;format="decap"$.error.$pre_fieldname$.required"
    val invalidKey  = "error.boolean"

    behave like booleanField(
      form = form,
      fieldName = fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form = form,
      fieldName = fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
