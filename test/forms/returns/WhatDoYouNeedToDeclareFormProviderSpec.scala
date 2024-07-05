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

package forms.returns

import forms.behaviours.CheckboxFieldBehaviours
import generators.ModelGenerators

class WhatDoYouNeedToDeclareFormProviderSpec extends CheckboxFieldBehaviours with ModelGenerators {

  val regime = regimeGen.sample.value

  val form = new WhatDoYouNeedToDeclareFormProvider()(regime)

  ".rateBand" - {
    val fieldName   = "rateBand"
    val requiredKey = s"whatDoYouNeedToDeclare.error.required.${regime.toString}"
    val values      = Seq("A", "B", "C")

    for {
      (value, i) <- values.zipWithIndex
    } yield s"binds `$value` successfully" in {
      val data   = Map(
        s"$fieldName[$i]" -> value
      )
      val result = form.bind(data)
      result.get mustEqual Set(value)
      result.errors mustBe empty
    }

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
