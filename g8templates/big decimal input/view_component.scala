        @govukInput(
            InputViewModel(
                field = form("$pre_fieldname$"),
                label = LabelViewModel(messages("$className;format="decap"$.subheading$pre_fieldindex$")).asSubheading
            )
            .asNumeric()
            .withWidth(Fixed10)
            .withHint(Hint(content = HtmlContent(messages("$className;format="decap"$.hint$pre_fieldindex$"))))
        )
