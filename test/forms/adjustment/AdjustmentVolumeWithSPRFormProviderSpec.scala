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
        "volumes.totalLitresVolume" -> validTotalLitres.toString(),
        "volumes.pureAlcoholVolume" -> validPureAlcohol.toString(),
        "volumes.sprDutyRate"       -> validSPRDutyRate.toString()
      )
      form.bind(data).value.value mustBe AdjustmentVolumeWithSPR(validTotalLitres, validPureAlcohol, validSPRDutyRate)
    }

    "must unbind valid data" in {
      val data = AdjustmentVolumeWithSPR(validTotalLitres, validPureAlcohol, validSPRDutyRate)
      form.fill(data).data must contain theSameElementsAs Map(
        "volumes.totalLitresVolume" -> validTotalLitres.toString(),
        "volumes.pureAlcoholVolume" -> validPureAlcohol.toString(),
        "volumes.sprDutyRate"       -> validSPRDutyRate.toString()
      )
    }

    "fail to bind when no answers are selected" in {
      val data = Map.empty[String, String]
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitresVolume", "adjustmentVolume.error.noValue.totalLitresVolume", Seq()),
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.noValue.pureAlcoholVolume", Seq()),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.noValue.sprDutyRate", Seq())
      )
    }

    "fail to bind when blank answer provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "",
        "volumes.pureAlcoholVolume" -> "",
        "volumes.sprDutyRate"       -> ""
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitresVolume", "adjustmentVolume.error.noValue.totalLitresVolume", Seq()),
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.noValue.pureAlcoholVolume", Seq()),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.noValue.sprDutyRate", Seq())
      )
    }

    "fail to bind when values with too many decimal places are provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "1.112",
        "volumes.pureAlcoholVolume" -> "1.11234",
        "volumes.sprDutyRate"       -> "1.112"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitresVolume", "adjustmentVolume.error.decimalPlaces.totalLitresVolume", Seq()),
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.decimalPlaces.pureAlcoholVolume", Seq()),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.decimalPlaces.sprDutyRate", Seq())
      )
    }

    "fail to bind when invalid values are provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "invalid",
        "volumes.pureAlcoholVolume" -> "invalid",
        "volumes.sprDutyRate"       -> "invalid"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitresVolume", "adjustmentVolume.error.invalid.totalLitresVolume", Seq()),
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.invalid.pureAlcoholVolume", Seq()),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.invalid.sprDutyRate", Seq())
      )
    }

    "fail to bind when values below minimum are provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "0",
        "volumes.pureAlcoholVolume" -> "0.0000",
        "volumes.sprDutyRate"       -> "-21"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitresVolume", "adjustmentVolume.error.minimumValue.totalLitresVolume", Seq()),
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.minimumValue.pureAlcoholVolume", Seq()),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.minimumValue.sprDutyRate", Seq())
      )
    }

    "fail to bind when values exceed maximum are provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "100000000000",
        "volumes.pureAlcoholVolume" -> "100000000000.0000",
        "volumes.sprDutyRate"       -> "100000000000"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitresVolume", "adjustmentVolume.error.maximumValue.totalLitresVolume", Seq()),
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.maximumValue.pureAlcoholVolume", Seq()),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.maximumValue.sprDutyRate", Seq())
      )
    }

    "fail to bind when pure alcohol volume is higher than total litres value" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "1",
        "volumes.pureAlcoholVolume" -> "2.0000",
        "volumes.sprDutyRate"       -> "0"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.lessThanExpected", Seq())
      )
    }

    "fail to bind when pure alcohol volume is empty, total litres value exceeds maximum and sprDutyRate is invalid" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "9999999999999999",
        "volumes.pureAlcoholVolume" -> "",
        "volumes.sprDutyRate"       -> "abc"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitresVolume", Seq("adjustmentVolume.error.maximumValue.totalLitresVolume"), Seq()),
        FormError("volumes_pureAlcoholVolume", Seq("adjustmentVolume.error.noValue.pureAlcoholVolume"), Seq()),
        FormError("volumes_sprDutyRate", Seq("adjustmentVolume.error.invalid.sprDutyRate"), Seq())
      )
    }

    "fail to bind with decimal places error when pure alcohol, total litres and sprDutyRate have more than expected decimals and are also out of range" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "999999999999.9999",
        "volumes.pureAlcoholVolume" -> "-12323.234423",
        "volumes.sprDutyRate"       -> "99999999999.856"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError(
          "volumes_totalLitresVolume",
          Seq("adjustmentVolume.error.decimalPlaces.totalLitresVolume"),
          Seq()
        ),
        FormError(
          "volumes_pureAlcoholVolume",
          Seq("adjustmentVolume.error.decimalPlaces.pureAlcoholVolume"),
          Seq()
        ),
        FormError("volumes_sprDutyRate", Seq("adjustmentVolume.error.decimalPlaces.sprDutyRate"), Seq())
      )
    }
  }

}
