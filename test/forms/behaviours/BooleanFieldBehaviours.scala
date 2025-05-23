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

import play.api.data.{Form, FormError}

trait BooleanFieldBehaviours extends FieldBehaviours {

  def booleanField(form: Form[_], fieldName: String, invalidError: FormError): Unit = {

    "bind true" in {
      val result = form.bind(Map(fieldName -> "true")).apply(fieldName)
      result.value.value mustBe "true"
      result.errors      mustBe empty
    }

    "bind false" in {
      val result = form.bind(Map(fieldName -> "false")).apply(fieldName)
      result.value.value mustBe "false"
      result.errors      mustBe empty
    }

    "throw an exception if non-boolean" in {
      forAll(nonBooleans -> "nonBoolean") { nonBoolean =>
        an[IllegalArgumentException] mustBe thrownBy(form.bind(Map(fieldName -> nonBoolean)).apply(fieldName))
      }
    }
  }
}
