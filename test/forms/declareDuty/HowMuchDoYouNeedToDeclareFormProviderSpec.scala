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

package forms.declareDuty

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import generators.ModelGenerators
import models.declareDuty.VolumesByTaxType
import play.api.data.FormError
import play.api.i18n.Messages

import scala.collection.immutable.List

class HowMuchDoYouNeedToDeclareFormProviderSpec extends StringFieldBehaviours with ModelGenerators with SpecBase {

  val regime = regimeGen.sample.value

  val messages = mock[Messages]
  val form     = new HowMuchDoYouNeedToDeclareFormProvider()(regime)(messages)

  ".volumes" - {

    "must bind valid data" in {
      val data = Map(
        "volumes[0].taxType"     -> "taxType",
        "volumes[0].totalLitres" -> "1",
        "volumes[0].pureAlcohol" -> "1.0000"
      )
      form.bind(data).value.value must contain theSameElementsAs List(
        VolumesByTaxType("taxType", 1, 1)
      )
    }

    "must unbind valid data" in {
      val data = List(
        VolumesByTaxType("taxType", 1, 1)
      )
      form.fill(data).data must contain theSameElementsAs Map(
        "volumes[0].taxType"     -> "taxType",
        "volumes[0].totalLitres" -> "1",
        "volumes[0].pureAlcohol" -> "1.0000"
      )
    }

    "fail to bind when no answers are selected" in {
      val data = Map.empty[String, String]
      form.bind(data).errors must contain(FormError("volumes", "return.journey.error.allRequired", Seq(List(""))))
    }

    "fail to bind when blank answer provided" in {
      val data = Map(
        "volumes[0].taxType"     -> "taxType",
        "volumes[0].totalLitres" -> "",
        "volumes[0].pureAlcohol" -> ""
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_0_totalLitres", "return.journey.error.noValue.totalLitres", Seq("")),
        FormError("volumes_0_pureAlcohol", "return.journey.error.noValue.pureAlcohol", Seq(""))
      )
    }

    "fail to bind when values with too many decimal places are provided" in {
      val data = Map(
        "volumes[0].taxType"     -> "taxType",
        "volumes[0].totalLitres" -> "1.112",
        "volumes[0].pureAlcohol" -> "1.11234"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_0_totalLitres", s"return.journey.error.tooManyDecimalPlaces.totalLitres", Seq("")),
        FormError("volumes_0_pureAlcohol", s"return.journey.error.tooManyDecimalPlaces.pureAlcohol", Seq(""))
      )
    }

    "fail to bind when invalid values are provided" in {
      val data = Map(
        "volumes[0].taxType"     -> "taxType",
        "volumes[0].totalLitres" -> "invalid",
        "volumes[0].pureAlcohol" -> "invalid"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_0_totalLitres", "return.journey.error.invalid.totalLitres", List("")),
        FormError("volumes_0_pureAlcohol", "return.journey.error.invalid.pureAlcohol", List(""))
      )
    }

    "fail to bind when values below minimum are provided" in {
      val data = Map(
        "volumes[0].taxType"     -> "taxType",
        "volumes[0].totalLitres" -> "0",
        "volumes[0].pureAlcohol" -> "0.0000"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_0_totalLitres", "return.journey.error.minimumValue.totalLitres", List("")),
        FormError("volumes_0_pureAlcohol", "return.journey.error.minimumValue.pureAlcohol", List(""))
      )
    }

    "fail to bind when values exceed maximum are provided" in {
      val data = Map(
        "volumes[0].taxType"     -> "taxType",
        "volumes[0].totalLitres" -> "100000000000",
        "volumes[0].pureAlcohol" -> "100000000000.0000"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_0_totalLitres", "return.journey.error.maximumValue.totalLitres", List("")),
        FormError("volumes_0_pureAlcohol", "return.journey.error.maximumValue.pureAlcohol", List(""))
      )
    }

    "fail to bind when pure alcohol volume is higher than total litres value" in {
      val data = Map(
        "volumes[0].taxType"     -> "taxType",
        "volumes[0].totalLitres" -> "1",
        "volumes[0].pureAlcohol" -> "2.0000"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_0_totalLitres", "return.journey.error.moreThanExpected", List("")),
        FormError("volumes_0_pureAlcohol", "return.journey.error.lessThanExpected", List(""))
      )
    }

    "fail to bind when pure alcohol volume is empty and total litres value exceeds maximum" in {
      val data = Map(
        "volumes[0].taxType"     -> "taxType",
        "volumes[0].totalLitres" -> "99999999999",
        "volumes[0].pureAlcohol" -> ""
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_0_totalLitres", List("return.journey.error.maximumValue.totalLitres"), List("")),
        FormError("volumes_0_pureAlcohol", List("return.journey.error.noValue.pureAlcohol"), List(""))
      )
    }

    "fail to bind with decimal places error when pure alcohol volume and total litres have more than expected decimal places and are also out of range" in {
      val data = Map(
        "volumes[0].taxType"     -> "taxType",
        "volumes[0].totalLitres" -> "111111111111.234",
        "volumes[0].pureAlcohol" -> "-2.45356"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError(
          "volumes_0_totalLitres",
          List("return.journey.error.tooManyDecimalPlaces.totalLitres"),
          List("")
        ),
        FormError("volumes_0_pureAlcohol", List("return.journey.error.tooManyDecimalPlaces.pureAlcohol"), List(""))
      )
    }
  }
}
