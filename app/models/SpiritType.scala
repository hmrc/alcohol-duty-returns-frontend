/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.textarea.Textarea
import viewmodels.govuk.checkbox._
import viewmodels.govuk.textarea

sealed trait SpiritType

object SpiritType extends Enumerable.Implicits {

  case object Maltspirits extends WithName("maltSpirits") with SpiritType
  case object Grainspirits extends WithName("grainSpirits") with SpiritType
  case object NeutralAgriculturalOrigin extends WithName("neutralAgriculturalOrigin") with SpiritType
  case object NeutralIndustrialOrigin extends WithName("neutralIndustrialOrigin") with SpiritType
  case object Beer extends WithName("beer") with SpiritType
  case object WineOrMadeWine extends WithName("wineOrMadeWine") with SpiritType
  case object CiderOrPerry extends WithName("ciderOrPerry") with SpiritType
  case object Other extends WithName("other") with SpiritType

  val values: Seq[SpiritType] = Seq(
    Maltspirits,
    Grainspirits,
    NeutralAgriculturalOrigin,
    NeutralIndustrialOrigin,
    Beer,
    WineOrMadeWine,
    CiderOrPerry,
    Other
  )

  private val valuesExcludingOther: Seq[SpiritType] = Seq(
    Maltspirits,
    Grainspirits,
    NeutralAgriculturalOrigin,
    NeutralIndustrialOrigin,
    Beer,
    WineOrMadeWine,
    CiderOrPerry
  )

  def checkboxItems(errors: Seq[FormError])(implicit messages: Messages): Seq[CheckboxItem] =
    valuesExcludingOther.zipWithIndex.map { case (value, index) =>
      CheckboxItemViewModel(
        content = Text(messages(s"spiritType.${value.toString}")),
        fieldId = "value",
        index = index,
        value = value.toString
      )
    } :+ CheckboxItemViewModel(
      content = Text(messages(s"spiritType.${Other.toString}")),
      fieldId = "value",
      index = valuesExcludingOther.size + 1,
      value = Other.toString
    ).withConditionalHtml(textBoxForOtherCheckbox(errors))

  private def textBoxForOtherCheckbox(errors: Seq[FormError])(implicit messages: Messages): Html = {
    println("AAAAAAAAA" + errors)
    errors.headOption match {
      case Some(value) if isOtherInputBoxError(value.messages) =>
        println()
        Html(
          s"""
           <div class="govuk-form-group">
            <label class="govuk-label" for="more-detail">
              ${messages("spiritType.other.input.label")}
            </label>
            <p id="event-name-error" class="govuk-error-message">
              <span class="govuk-visually-hidden">Error:</span> ${messages(value.messages.headOption.getOrElse(""))}
            </p>
            <input class="govuk-input govuk-input--error govuk-!-width-one-half" id="event-name" name="otherTextInput" type="text" aria-describedby="event-name-hint event-name-error">
          </div>
          """
        )
      case _                                                   =>
        Html(
          s"""
           <div class="govuk-form-group">
            <label class="govuk-label" for="more-detail">
              ${messages("spiritType.other.input.label")}
            </label>
            <input class="govuk-input govuk-!-width-one-half" id="event-name" name="otherTextInput" type="text">
          </div>
          """
        )
    }
  }

  def isOtherInputBoxError(errorMessages: Seq[String]): Boolean =
    errorMessages.headOption match {
      case Some(errorKey) =>
        val a = errorKey.equals("spiritType.other.error.length") || errorKey.equals(
          "spiritType.other.error.permitted-chars"
        ) || errorKey.equals("spiritType.error.other.required")
        println("BBBBBBBBBB" + a)
        a
      case None           => false
    }

  implicit val enumerable: Enumerable[SpiritType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
