/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package viewmodels.govuk

import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.textarea.Textarea
import viewmodels.InputWidth

trait TextareaFluency {

  implicit class FluentTextarea(textarea: Textarea) {

    def describedBy(value: String): Textarea =
      textarea.copy(describedBy = Some(value))

    def withHint(hint: Hint): Textarea =
      textarea.copy(hint = Some(hint))

    def withCssClass(newClass: String): Textarea =
      textarea.copy(classes = s"${textarea.classes} $newClass")

    def withAutocomplete(value: String): Textarea =
      textarea.copy(autocomplete = Some(value))

    def withAttribute(attribute: (String, String)): Textarea =
      textarea.copy(attributes = textarea.attributes + attribute)

    def withWidth(inputWidth: InputWidth): Textarea =
      textarea.withCssClass(inputWidth.toString)

    def withSpellcheck(on: Boolean = true): Textarea =
      textarea.copy(spellcheck = Some(on))
  }
}
