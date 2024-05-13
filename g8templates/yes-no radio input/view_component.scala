        @govukRadios(
            RadiosViewModel.yesNo(
                field = form("$pre_fieldname$"),
                legend = LegendViewModel(messages("$className;format="decap"$.subheading$pre_fieldindex$")).asSubheading
            )
        )
