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
import models.adjustment.{AdjustmentEntry, AdjustmentVolumeWithSPR}
import play.api.data.FormError

class AdjustmentVolumeWithSPRFormProviderSpec extends StringFieldBehaviours with ModelGenerators with SpecBase {

  val regime = regimeGen.sample.value

  val validTotalLitres = BigDecimal(10.23)
  val validPureAlcohol = BigDecimal("9.2300")
  val validSPRDutyRate = BigDecimal(2)

  val adjustmentEntry = AdjustmentEntry(
    totalLitresVolume = Some(validTotalLitres),
    pureAlcoholVolume = Some(validPureAlcohol),
    sprDutyRate = Some(validSPRDutyRate)
  )

  val adjustmentVolumeWithSPR = AdjustmentVolumeWithSPR(validTotalLitres, validPureAlcohol, validSPRDutyRate)
  val form                    = new AdjustmentVolumeWithSPRFormProvider()()

  ".volumes" - {
    "must bind valid data" in {
      val data = Map(
        "volumes.totalLitres" -> validTotalLitres.toString(),
        "volumes.pureAlcohol" -> validPureAlcohol.toString(),
        "volumes.sprDutyRate" -> validSPRDutyRate.toString()
      )
      form.bind(data).value.value mustBe AdjustmentVolumeWithSPR(validTotalLitres, validPureAlcohol, validSPRDutyRate)
    }

    "must unbind valid data" in {
      val data = AdjustmentVolumeWithSPR(validTotalLitres, validPureAlcohol, validSPRDutyRate)
      form.fill(data).data must contain theSameElementsAs Map(
        "volumes.totalLitres" -> validTotalLitres.toString(),
        "volumes.pureAlcohol" -> validPureAlcohol.toString(),
        "volumes.sprDutyRate" -> validSPRDutyRate.toString()
      )
    }

    "fail to bind when no answers are selected" in {
      val data = Map.empty[String, String]
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitres", "adjustmentVolume.error.noValue.totalLitres", Seq()),
        FormError("volumes_pureAlcohol", "adjustmentVolume.error.noValue.pureAlcohol", Seq()),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.noValue.sprDutyRate", Seq())
      )
    }

    "fail to bind when blank answer provided" in {
      val data = Map(
        "volumes.totalLitres" -> "",
        "volumes.pureAlcohol" -> "",
        "volumes.sprDutyRate" -> ""
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitres", "adjustmentVolume.error.noValue.totalLitres", Seq()),
        FormError("volumes_pureAlcohol", "adjustmentVolume.error.noValue.pureAlcohol", Seq()),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.noValue.sprDutyRate", Seq())
      )
    }

    "fail to bind when values with too many decimal places are provided" in {
      val data = Map(
        "volumes.totalLitres" -> "1.112",
        "volumes.pureAlcohol" -> "1.11234",
        "volumes.sprDutyRate" -> "1.112"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitres", s"adjustmentVolume.error.decimalPlaces.totalLitres", Seq()),
        FormError("volumes_pureAlcohol", s"adjustmentVolume.error.decimalPlaces.pureAlcohol", Seq()),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.decimalPlaces.sprDutyRate", Seq())
      )
    }

    "fail to bind when invalid values are provided" in {
      val data = Map(
        "volumes.totalLitres" -> "invalid",
        "volumes.pureAlcohol" -> "invalid",
        "volumes.sprDutyRate" -> "invalid"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitres", "adjustmentVolume.error.invalid.totalLitres", Seq()),
        FormError("volumes_pureAlcohol", "adjustmentVolume.error.invalid.pureAlcohol", Seq()),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.invalid.sprDutyRate", Seq())
      )
    }

    "fail to bind when values below minimum are provided" in {
      val data = Map(
        "volumes.totalLitres" -> "0",
        "volumes.pureAlcohol" -> "0.0000",
        "volumes.sprDutyRate" -> "-21"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitres", "adjustmentVolume.error.minimumValue.totalLitres", Seq()),
        FormError("volumes_pureAlcohol", "adjustmentVolume.error.minimumValue.pureAlcohol", Seq()),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.minimumValue.sprDutyRate", Seq())
      )
    }

    "fail to bind when values exceed maximum are provided" in {
      val data = Map(
        "volumes.totalLitres" -> "100000000000",
        "volumes.pureAlcohol" -> "100000000000.0000",
        "volumes.sprDutyRate" -> "100000000000"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitres", "adjustmentVolume.error.maximumValue.totalLitres", Seq()),
        FormError("volumes_pureAlcohol", "adjustmentVolume.error.maximumValue.pureAlcohol", Seq()),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.maximumValue.sprDutyRate", Seq())
      )
    }

    "fail to bind when pure alcohol volume is higher than total litres value" in {
      val data = Map(
        "volumes.totalLitres" -> "1",
        "volumes.pureAlcohol" -> "2.0000",
        "volumes.sprDutyRate" -> "0"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_pureAlcohol", "adjustmentVolume.error.lessThanExpected", Seq())
      )
    }

    "fail to bind when pure alcohol volume is empty, total litres value exceeds maximum and sprDutyRate is invalid" in {
      val data = Map(
        "volumes.totalLitres" -> "9999999999999999",
        "volumes.pureAlcohol" -> "",
        "volumes.sprDutyRate" -> "abc"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitres", Seq("adjustmentVolume.error.maximumValue.totalLitres"), Seq()),
        FormError("volumes_pureAlcohol", Seq("adjustmentVolume.error.noValue.pureAlcohol"), Seq()),
        FormError("volumes_sprDutyRate", Seq("adjustmentVolume.error.invalid.sprDutyRate"), Seq())
      )
    }

    "fail to bind with decimal places error when pure alcohol, total litres and sprDutyRate have more than expected decimals and are also out of range" in {
      val data = Map(
        "volumes.totalLitres" -> "999999999999.9999",
        "volumes.pureAlcohol" -> "-12323.234423",
        "volumes.sprDutyRate" -> "99999999999.856"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError(
          "volumes_totalLitres",
          Seq("adjustmentVolume.error.decimalPlaces.totalLitres"),
          Seq()
        ),
        FormError(
          "volumes_pureAlcohol",
          Seq("adjustmentVolume.error.decimalPlaces.pureAlcohol"),
          Seq()
        ),
        FormError("volumes_sprDutyRate", Seq("adjustmentVolume.error.decimalPlaces.sprDutyRate"), Seq())
      )
    }
  }

}
