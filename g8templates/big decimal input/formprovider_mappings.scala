      "$pre_fieldname$"     -> bigDecimal(
        $pre_fieldname$MaxDecimalPlaces,
        "$className;format="decap"$.error.$pre_fieldname$.required",
        "$className;format="decap"$.error.$pre_fieldname$.nonNumeric",
        "$className;format="decap"$.error.$pre_fieldname$.decimalPlaces"
      ).verifying(minimumValue($pre_fieldname$MinValue, "$className;format="decap"$.error.$pre_fieldname$.minimumRequired"))
        .verifying(maximumValue($pre_fieldname$MaxValue, "$className;format="decap"$.error.$pre_fieldname$.maximumRequired"))
