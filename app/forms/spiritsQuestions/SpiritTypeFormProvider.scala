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

package forms.spiritsQuestions

import forms.mappings.Mappings
import forms.spiritsQuestions.SpiritTypeFormProvider.{ifOtherSelectedThenBoxHasToBePopulated, otherTypesLength, permittedChars}
import models.SpiritType
import models.SpiritType.Other
import models.spiritsQuestions.SpiritTypePageAnswers
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, set}
import play.api.data.validation.Constraints.nonEmpty
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

import javax.inject.Inject

class SpiritTypeFormProvider @Inject() extends Mappings {

  def apply(): Form[SpiritTypePageAnswers] =
    Form(
      mapping(
        "value"          -> set(enumerable[SpiritType]("spiritType.error.required"))
          .verifying(nonEmptySet("spiritType.error.required")),
        "otherTextInput" -> optional(
          text()
            .verifying(maxLength(otherTypesLength, "spiritType.other.error.length"))
            .verifying(nonEmpty(errorMessage = "spiritType.error.other.required"))
            .verifying(regexp(permittedChars, "spiritType.other.error.permitted-chars"))
        )
      )(SpiritTypePageAnswers.apply)(SpiritTypePageAnswers.unapply)
        .verifying(ifOtherSelectedThenBoxHasToBePopulated("spiritType.error.other.required"))
    )
}
object SpiritTypeFormProvider {
  val otherTypesLength: Int  = 120
  val permittedChars: String = "^[A-Za-z0-9 ]+$"

  def ifOtherSelectedThenBoxHasToBePopulated(errorMessageKey: String): Constraint[SpiritTypePageAnswers] =
    Constraint("If Other is ticked, text input cannot be empty") { formAnswers =>
      val otherSelected: Boolean      = formAnswers.spiritTypes.contains(Other)
      val otherInputNotEmpty: Boolean = formAnswers.maybeOtherSpiritTypes.exists(_.trim.nonEmpty)

      if (!otherSelected || otherInputNotEmpty) {
        Valid
      } else {
        Invalid(ValidationError(errorMessageKey, "value"))
      }
    }
}
