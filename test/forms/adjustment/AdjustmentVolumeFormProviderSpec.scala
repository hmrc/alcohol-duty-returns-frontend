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
import forms.behaviours.BigDecimalFieldBehaviours
import generators.ModelGenerators
import models.adjustment.AdjustmentVolume
import play.api.data.FormError

class AdjustmentVolumeFormProviderSpec extends BigDecimalFieldBehaviours with ModelGenerators with SpecBase {
  val form = new AdjustmentVolumeFormProvider()()

  ".volumes" - {
    "must bind valid data" in {
      val data = Map(
        "volumes.totalLitres" -> "1",
        "volumes.pureAlcohol" -> "1.0000"
      )
      form.bind(data).value.value mustBe AdjustmentVolume(1, 1)
    }

    "must unbind valid data" in {
      val data = AdjustmentVolume(1, 1)
      form.fill(data).data must contain theSameElementsAs Map(
        "volumes.totalLitres" -> "1",
        "volumes.pureAlcohol" -> "1.0000"
      )
    }

    "fail to bind when no answers are selected" in {
      val data = Map.empty[String, String]
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitres", "adjustmentVolume.error.noValue.totalLitres", Seq()),
        FormError("volumes_pureAlcohol", "adjustmentVolume.error.noValue.pureAlcohol", Seq())
      )
    }

    "fail to bind when blank answer provided" in {
      val data = Map(
        "volumes.totalLitres" -> "",
        "volumes.pureAlcohol" -> ""
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitres", "adjustmentVolume.error.noValue.totalLitres", Seq()),
        FormError("volumes_pureAlcohol", "adjustmentVolume.error.noValue.pureAlcohol", Seq())
      )
    }

    "fail to bind when values with too many decimal places are provided" in {
      val data = Map(
        "volumes.totalLitres" -> "1.111",
        "volumes.pureAlcohol" -> "1.11234"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitres", s"adjustmentVolume.error.decimalPlaces.totalLitres", Seq()),
        FormError("volumes_pureAlcohol", s"adjustmentVolume.error.decimalPlaces.pureAlcohol", Seq())
      )
    }

    "fail to bind when invalid values are provided" in {
      val data = Map(
        "volumes.totalLitres" -> "invalid",
        "volumes.pureAlcohol" -> "invalid"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitres", "adjustmentVolume.error.invalid.totalLitres", Seq()),
        FormError("volumes_pureAlcohol", "adjustmentVolume.error.invalid.pureAlcohol", Seq())
      )
    }

    "fail to bind when values below minimum are provided" in {
      val data = Map(
        "volumes.totalLitres" -> "0",
        "volumes.pureAlcohol" -> "0.0000"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitres", "adjustmentVolume.error.minimumValue.totalLitres", Seq()),
        FormError("volumes_pureAlcohol", "adjustmentVolume.error.minimumValue.pureAlcohol", Seq())
      )
    }

    "fail to bind when values exceed maximum are provided" in {
      val data = Map(
        "volumes.totalLitres" -> "100000000000",
        "volumes.pureAlcohol" -> "100000000000.0000"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitres", "adjustmentVolume.error.maximumValue.totalLitres", Seq()),
        FormError("volumes_pureAlcohol", "adjustmentVolume.error.maximumValue.pureAlcohol", Seq())
      )
    }

    "fail to bind when pure alcohol volume is higher than total litres value" in {
      val data = Map(
        "volumes.totalLitres" -> "1",
        "volumes.pureAlcohol" -> "2.0000"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_pureAlcohol", "adjustmentVolume.error.lessThanExpected", Seq())
      )
    }

    "fail to bind when pure alcohol volume is empty and total litres value exceeds maximum" in {
      val data = Map(
        "volumes.totalLitres" -> "9999999999999999",
        "volumes.pureAlcohol" -> ""
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitres", Seq("adjustmentVolume.error.maximumValue.totalLitres"), Seq()),
        FormError("volumes_pureAlcohol", Seq("adjustmentVolume.error.noValue.pureAlcohol"), Seq())
      )
    }

    "fail to bind with decimal places error when pure alcohol volume and total litres have more than expected decimal places and are also out of range" in {
      val data = Map(
        "volumes.totalLitres" -> "999999999999.9999",
        "volumes.pureAlcohol" -> "-12323.234423"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError(
          "volumes_totalLitres",
          Seq("adjustmentVolume.error.decimalPlaces.totalLitres"),
          Seq()
        ),
        FormError("volumes_pureAlcohol", Seq("adjustmentVolume.error.decimalPlaces.pureAlcohol"), Seq())
      )
    }
  }
}
