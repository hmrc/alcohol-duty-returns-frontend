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

package forms.adjustment

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import generators.ModelGenerators
import models.adjustment.{AdjustmentEntry, SpoiltVolumeWithDuty}
import play.api.data.FormError
import play.api.i18n.Messages

class SpoiltVolumeWithDutyFormProviderSpec extends StringFieldBehaviours with ModelGenerators with SpecBase {

  val regime = regimeGen.sample.value

  val messages = mock[Messages]

  val validTotalLitres = BigDecimal(10.23)
  val validPureAlcohol = BigDecimal("9.2300")
  val validDuty        = BigDecimal(2)

  val adjustmentEntry = AdjustmentEntry(
    totalLitresVolume = Some(validTotalLitres),
    pureAlcoholVolume = Some(validPureAlcohol),
    duty = Some(validDuty)
  )

  val adjustmentVolumeWithSPR = SpoiltVolumeWithDuty(validTotalLitres, validPureAlcohol, validDuty)
  val form                    = new SpoiltVolumeWithDutyFormProvider()(regime)(messages)

  ".volumes" - {
    "must bind valid data" in {
      val data = Map(
        "volumes.totalLitres" -> validTotalLitres.toString(),
        "volumes.pureAlcohol" -> validPureAlcohol.toString(),
        "volumes.duty"        -> validDuty.toString()
      )
      form.bind(data).value.value mustBe SpoiltVolumeWithDuty(validTotalLitres, validPureAlcohol, validDuty)
    }

    "must unbind valid data" in {
      val data = SpoiltVolumeWithDuty(validTotalLitres, validPureAlcohol, validDuty)
      form.fill(data).data must contain theSameElementsAs Map(
        "volumes.totalLitres" -> validTotalLitres.toString(),
        "volumes.pureAlcohol" -> validPureAlcohol.toString(),
        "volumes.duty"        -> validDuty.toString()
      )
    }

    "fail to bind when no answers are selected" in {
      val data = Map.empty[String, String]
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitres", "spoiltVolumeWithDuty.error.noValue.totalLitres", Seq("")),
        FormError("volumes_pureAlcohol", "spoiltVolumeWithDuty.error.noValue.pureAlcohol", Seq("")),
        FormError("volumes_duty", "spoiltVolumeWithDuty.error.noValue.duty", Seq(""))
      )
    }

    "fail to bind when blank answer provided" in {
      val data = Map(
        "volumes.totalLitres" -> "",
        "volumes.pureAlcohol" -> "",
        "volumes.duty"        -> ""
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitres", "spoiltVolumeWithDuty.error.noValue.totalLitres", Seq("")),
        FormError("volumes_pureAlcohol", "spoiltVolumeWithDuty.error.noValue.pureAlcohol", Seq("")),
        FormError("volumes_duty", "spoiltVolumeWithDuty.error.noValue.duty", Seq(""))
      )
    }

    "fail to bind when values with too many decimal places are provided" in {
      val data = Map(
        "volumes.totalLitres" -> "1.112",
        "volumes.pureAlcohol" -> "1.11234",
        "volumes.duty"        -> "1.112"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitres", s"spoiltVolumeWithDuty.error.decimalPlaces.totalLitres", Seq("")),
        FormError("volumes_pureAlcohol", s"spoiltVolumeWithDuty.error.decimalPlaces.pureAlcohol", Seq("")),
        FormError("volumes_duty", "spoiltVolumeWithDuty.error.decimalPlaces.duty", Seq(""))
      )
    }

    "fail to bind when invalid values are provided" in {
      val data = Map(
        "volumes.totalLitres" -> "invalid",
        "volumes.pureAlcohol" -> "invalid",
        "volumes.duty"        -> "invalid"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitres", "spoiltVolumeWithDuty.error.invalid.totalLitres", List("")),
        FormError("volumes_pureAlcohol", "spoiltVolumeWithDuty.error.invalid.pureAlcohol", List("")),
        FormError("volumes_duty", "spoiltVolumeWithDuty.error.invalid.duty", Seq(""))
      )
    }

    "fail to bind when values below minimum are provided" in {
      val data = Map(
        "volumes.totalLitres" -> "0",
        "volumes.pureAlcohol" -> "0.0000",
        "volumes.duty"        -> "0"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitres", "spoiltVolumeWithDuty.error.minimumValue.totalLitres", List("")),
        FormError("volumes_pureAlcohol", "spoiltVolumeWithDuty.error.minimumValue.pureAlcohol", List("")),
        FormError("volumes_duty", "spoiltVolumeWithDuty.error.minimumValue.duty", Seq(""))
      )
    }

    "fail to bind when values exceed maximum are provided" in {
      val data = Map(
        "volumes.totalLitres" -> "100000000000",
        "volumes.pureAlcohol" -> "100000000000.0000",
        "volumes.duty"        -> "100000000000"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitres", "spoiltVolumeWithDuty.error.maximumValue.totalLitres", List("")),
        FormError("volumes_pureAlcohol", "spoiltVolumeWithDuty.error.maximumValue.pureAlcohol", List("")),
        FormError("volumes_duty", "spoiltVolumeWithDuty.error.maximumValue.duty", Seq(""))
      )
    }

    "fail to bind when pure alcohol volume is higher than total litres value" in {
      val data = Map(
        "volumes.totalLitres" -> "1",
        "volumes.pureAlcohol" -> "2.0000",
        "volumes.duty"        -> "0.01"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_pureAlcohol", "spoiltVolumeWithDuty.error.lessThanExpected", List(""))
      )
    }

    "fail to bind when pure alcohol volume is empty, total litres value exceeds maximum and duty is invalid" in {
      val data = Map(
        "volumes.totalLitres" -> "9999999999999999",
        "volumes.pureAlcohol" -> "",
        "volumes.duty"        -> "abc"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError(
          "volumes_totalLitres",
          List("spoiltVolumeWithDuty.error.maximumValue.totalLitres"),
          List("")
        ),
        FormError("volumes_pureAlcohol", List("spoiltVolumeWithDuty.error.noValue.pureAlcohol"), List("")),
        FormError("volumes_duty", List("spoiltVolumeWithDuty.error.invalid.duty"), List(""))
      )
    }

    "fail to bind with decimal places error when pure alcohol, total litres and duty have more than expected decimals and are also out of range" in {
      val data = Map(
        "volumes.totalLitres" -> "999999999999.9999",
        "volumes.pureAlcohol" -> "-12323.234423",
        "volumes.duty"        -> "99999999999.856"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError(
          "volumes_totalLitres",
          List("spoiltVolumeWithDuty.error.decimalPlaces.totalLitres"),
          List("")
        ),
        FormError(
          "volumes_pureAlcohol",
          List("spoiltVolumeWithDuty.error.decimalPlaces.pureAlcohol"),
          List("")
        ),
        FormError("volumes_duty", List("spoiltVolumeWithDuty.error.decimalPlaces.duty"), List(""))
      )
    }
  }

}
