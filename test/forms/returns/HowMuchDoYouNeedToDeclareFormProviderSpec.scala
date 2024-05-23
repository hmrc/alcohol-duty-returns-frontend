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

import forms.behaviours.StringFieldBehaviours
import generators.ModelGenerators
import play.api.data.FormError

class HowMuchDoYouNeedToDeclareFormProviderSpec extends StringFieldBehaviours with ModelGenerators {

  val regime = regimeGen.sample.value
  val form   = new HowMuchDoYouNeedToDeclareFormProvider()(regime)

  ".volumes" - {

    val fieldName            = "volumes"
    val requiredKey          = s"howMuchDoYouNeedToDeclare.error.required.${regime.toString}"
    val allRequired          = s"howMuchDoYouNeedToDeclare.error.allRequired.${regime.toString}"
    val lengthKeyTotalLitres = s"howMuchDoYouNeedToDeclare.error.decimalPlacesKey.${regime.toString}.totalLitres"
    val lengthKeyPureAlcohol = s"howMuchDoYouNeedToDeclare.error.decimalPlacesKey.${regime.toString}.pureAlcohol"

    val invalidKey = s"howMuchDoYouNeedToDeclare.error.invalid.${regime.toString}"

    "fail to bind when no answers are selected" in {
      val data = Map.empty[String, String]
      form.bind(data).errors must contain(FormError(fieldName, allRequired))
    }

    "fail to bind when blank answer provided" in {
      val data = Map(
        s"$fieldName[0].taxType"     -> "",
        s"$fieldName[0].totalLitres" -> "",
        s"$fieldName[0].pureAlcohol" -> ""
      )
      form.bind(data).errors must contain(FormError(s"$fieldName[0]", allRequired))
    }

    "required fields" - {

      "fail to bind when blank answer provided as tax type" in {
        val data = Map(
          s"$fieldName[0].taxType"     -> "",
          s"$fieldName[0].totalLitres" -> "1.1",
          s"$fieldName[0].pureAlcohol" -> "1.1"
        )
        form.bind(data).errors must contain(
          FormError(s"${fieldName}_0.taxType", s"$requiredKey.taxType", Seq("taxType"))
        )
      }

      "fail to bind when blank answer provided as total litres" in {
        val data = Map(
          s"$fieldName[0].taxType"     -> "1",
          s"$fieldName[0].totalLitres" -> "",
          s"$fieldName[0].pureAlcohol" -> "1.1"
        )
        form.bind(data).errors must contain(
          FormError(s"${fieldName}_0.totalLitres", s"$requiredKey.totalLitres", Seq("totalLitres"))
        )
      }

      "fail to bind when blank answer provided as pure alcohol" in {
        val data = Map(
          s"$fieldName[0].taxType"     -> "1",
          s"$fieldName[0].totalLitres" -> "1.1",
          s"$fieldName[0].pureAlcohol" -> ""
        )
        form.bind(data).errors must contain(
          FormError(s"${fieldName}_0.pureAlcohol", s"$requiredKey.pureAlcohol", Seq("pureAlcohol"))
        )
      }

    }

    "decimal places" - {
      "fail to bind when the value for total litres is not valid" in {
        val data = Map(
          s"$fieldName[0].taxType"     -> "1",
          s"$fieldName[0].totalLitres" -> "1.1123",
          s"$fieldName[0].pureAlcohol" -> "1.1"
        )
        form.bind(data).errors must contain(FormError(s"$fieldName[0]", lengthKeyTotalLitres))
      }

      "fail to bind when the value for pure alcohol is not valid" in {
        val data = Map(
          s"$fieldName[0].taxType"     -> "1",
          s"$fieldName[0].totalLitres" -> "1.1",
          s"$fieldName[0].pureAlcohol" -> "1.11234"
        )
        form.bind(data).errors must contain(FormError(s"$fieldName[0]", lengthKeyPureAlcohol))
      }
    }

    "non numeric values" - {
      "fail to bind when the answer is invalid for total litres" in {
        val data = Map(
          s"$fieldName[0].taxType"     -> "1",
          s"$fieldName[0].totalLitres" -> "invalid",
          s"$fieldName[0].pureAlcohol" -> "1.1"
        )
        form.bind(data).errors must contain(
          FormError(s"$fieldName[0]", s"$invalidKey.totalLitres")
        )
      }

      "fail to bind when the answer is invalid for pure alcohol" in {
        val data = Map(
          s"$fieldName[0].taxType"     -> "1",
          s"$fieldName[0].totalLitres" -> "1.1",
          s"$fieldName[0].pureAlcohol" -> "invalid"
        )
        form.bind(data).errors must contain(
          FormError(s"$fieldName[0]", s"$invalidKey.pureAlcohol")
        )
      }

    }
  }
}
