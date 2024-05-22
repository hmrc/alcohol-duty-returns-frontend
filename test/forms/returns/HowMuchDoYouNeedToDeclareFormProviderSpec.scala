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
import play.api.data.FormError

class HowMuchDoYouNeedToDeclareFormProviderSpec extends StringFieldBehaviours {

  val form = new HowMuchDoYouNeedToDeclareFormProvider()()

  ".volumes" - {

    val fieldName            = "volumes"
    val requiredKey          = "howMuchDoYouNeedToDeclare.error.required"
    val allRequired          = "howMuchDoYouNeedToDeclare.error.allRequired"
    val lengthKeyTotalLitres = "howMuchDoYouNeedToDeclare.error.decimalPlacesKey.totalLitres"
    val lengthKeyPureAlcohol = "howMuchDoYouNeedToDeclare.error.decimalPlacesKey.pureAlcohol"

    "fail to bind when no answers are selected" in {
      val data = Map.empty[String, String]
      form.bind(data).errors must contain(FormError(fieldName, requiredKey))
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
        form.bind(data).errors must contain(FormError(s"$fieldName[0].taxType", requiredKey, Seq("taxType")))
      }

      "fail to bind when blank answer provided as total litres" in {
        val data = Map(
          s"$fieldName[0].taxType"     -> "1",
          s"$fieldName[0].totalLitres" -> "",
          s"$fieldName[0].pureAlcohol" -> "1.1"
        )
        form.bind(data).errors must contain(FormError(s"$fieldName[0].totalLitres", requiredKey, Seq("totalLitres")))
      }

      "fail to bind when blank answer provided as pure alcohol" in {
        val data = Map(
          s"$fieldName[0].taxType"     -> "1",
          s"$fieldName[0].totalLitres" -> "1.1",
          s"$fieldName[0].pureAlcohol" -> ""
        )
        form.bind(data).errors must contain(FormError(s"$fieldName[0].pureAlcohol", requiredKey, Seq("pureAlcohol")))
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
          FormError(s"$fieldName[0]", "howMuchDoYouNeedToDeclare.error.invalid.totalLitres")
        )
      }

      "fail to bind when the answer is invalid for pure alcohol" in {
        val data = Map(
          s"$fieldName[0].taxType"     -> "1",
          s"$fieldName[0].totalLitres" -> "1.1",
          s"$fieldName[0].pureAlcohol" -> "invalid"
        )
        form.bind(data).errors must contain(
          FormError(s"$fieldName[0]", "howMuchDoYouNeedToDeclare.error.invalid.pureAlcohol")
        )
      }

    }
  }
}
