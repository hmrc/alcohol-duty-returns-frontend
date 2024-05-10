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

package forms.behaviours

import enumeratum.EnumEntry
import play.api.data.{Form, FormError}

trait EnumBehaviours extends FieldBehaviours {

  def validEnumValues[E <: EnumEntry](
    form: Form[_],
    fieldName: String,
    enums: Seq[EnumEntry],
    enumError: FormError
  ): Unit =
    "not bind non-enum values" in {
      forAll(nonEnumStrings(enums) -> "nonEnum") { notValidEnum =>
        val result = form.bind(Map(fieldName -> notValidEnum)).apply(fieldName)
        result.errors mustEqual Seq(enumError)
      }
    }

  def mandatoryEnumField(form: Form[_], fieldName: String, requiredError: FormError, enumError: FormError): Unit = {

    "not bind when key is not present at all" in {

      val result = form.bind(emptyForm).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    "not bind blank values" in {

      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors mustEqual Seq(enumError)
    }
  }
}
