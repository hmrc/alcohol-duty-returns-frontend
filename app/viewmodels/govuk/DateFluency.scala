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

import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint

object date extends DateFluency

trait DateFluency {

  implicit class FluentDate(date: DateInput) {

    def withHint(hint: Hint): DateInput =
      date.copy(hint = Some(hint))

    def withCssClass(newClass: String): DateInput =
      date.copy(classes = s"${date.classes} $newClass")

    def withAttribute(attribute: (String, String)): DateInput =
      date.copy(attributes = date.attributes + attribute)
  }
}
