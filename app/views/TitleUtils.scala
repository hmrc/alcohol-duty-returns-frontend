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

package views

import play.api.data.Form
import play.api.i18n.Messages

object TitleUtils {

  /**
    * Call this one if you have a validated form on your page since it'll prefix the browser title with Error: if one
    *
    * title should already been looked up
    */
  def title(form: Form[_], title: String)(implicit messages: Messages): String =
    titleNoForm(s"${errorPrefix(form)} $title")

  /**
    * Call this one on a page without a validated form
    *
    * title should already been looked up
    */
  def titleNoForm(title: String)(implicit messages: Messages): String =
    s"$title - ${messages("service.name")} - ${messages("site.govuk")}"

  private def errorPrefix(form: Form[_])(implicit messages: Messages): String =
    if (form.hasErrors || form.hasGlobalErrors) messages("error.browser.title.prefix") else ""
}
