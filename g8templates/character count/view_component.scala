        @govukCharacterCount(
            CharacterCountViewModel(
                field = form("$pre_fieldname$"),
                label = LabelViewModel(messages("$className;format="decap"$.subheading$pre_fieldindex$")).asSubheading
            )
            .withWidth(TwoThirds)
            .withMaxLength($className$FormProvider.$pre_fieldname$Length)
            .withHint(Hint(content = HtmlContent(messages("$className;format="decap"$.hint$pre_fieldindex$"))))
        )
