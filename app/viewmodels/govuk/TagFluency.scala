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

import config.Constants.Css
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag

object tag extends TagFluency

trait TagFluency {

  object TagViewModel {

    def apply(content: Content): Tag =
      Tag(content = content)
  }

  implicit class FluentTag(tag: Tag) {

    def withCssClass(newClass: String): Tag =
      tag.copy(classes = s"${tag.classes} $newClass")

    def withAttribute(attribute: (String, String)): Tag =
      tag.copy(attributes = tag.attributes + attribute)

    def grey(): Tag =
      withCssClass(Css.greyTagCssClass)

    def green(): Tag =
      withCssClass(Css.greenTagCssClass)

    def turquoise(): Tag =
      withCssClass(Css.turquoiseCssClass)

    def lightBlue(): Tag =
      withCssClass(Css.lightBlueTagCssClass)

    def blue(): Tag =
      withCssClass(Css.blueTagCssClass)

    def purple(): Tag =
      withCssClass(Css.purpleCssClass)

    def pink(): Tag =
      withCssClass(Css.pinkCssClass)

    def red(): Tag =
      withCssClass(Css.redTagCssClass)

    def orange(): Tag =
      withCssClass(Css.orangeCssClass)

    def yellow(): Tag =
      withCssClass(Css.yellowTagCssClass)
  }
}
