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
        "volumes.totalLitresVolume" -> "1",
        "volumes.pureAlcoholVolume" -> "1"
      )
      form.bind(data).value.value mustBe AdjustmentVolume(1, 1)
    }

    "must unbind valid data" in {
      val data = AdjustmentVolume(1, 1)
      form.fill(data).data must contain theSameElementsAs Map(
        "volumes.totalLitresVolume" -> "1",
        "volumes.pureAlcoholVolume" -> "1"
      )
    }

    "fail to bind when no answers are selected" in {
      val data = Map.empty[String, String]
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitresVolume", "adjustmentVolume.error.noValue.totalLitresVolume", Seq()),
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.noValue.pureAlcoholVolume", Seq())
      )
    }

    "fail to bind when blank answer provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "",
        "volumes.pureAlcoholVolume" -> ""
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitresVolume", "adjustmentVolume.error.noValue.totalLitresVolume", Seq()),
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.noValue.pureAlcoholVolume", Seq())
      )
    }

    "fail to bind when values with too many decimal places are provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "1.111",
        "volumes.pureAlcoholVolume" -> "1.11234"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitresVolume", s"adjustmentVolume.error.decimalPlaces.totalLitresVolume", Seq()),
        FormError("volumes_pureAlcoholVolume", s"adjustmentVolume.error.decimalPlaces.pureAlcoholVolume", Seq())
      )
    }

    "fail to bind when invalid values are provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "invalid",
        "volumes.pureAlcoholVolume" -> "invalid"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitresVolume", "adjustmentVolume.error.invalid.totalLitresVolume", Seq()),
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.invalid.pureAlcoholVolume", Seq())
      )
    }

    "fail to bind when values below minimum are provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "0",
        "volumes.pureAlcoholVolume" -> "0"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitresVolume", "adjustmentVolume.error.minimumValue.totalLitresVolume", Seq()),
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.minimumValue.pureAlcoholVolume", Seq())
      )
    }

    "fail to bind when values exceed maximum are provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "100000000000",
        "volumes.pureAlcoholVolume" -> "100000000000"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitresVolume", "adjustmentVolume.error.maximumValue.totalLitresVolume", Seq()),
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.maximumValue.pureAlcoholVolume", Seq())
      )
    }

    "fail to bind when pure alcohol volume is higher than total litres value" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "1",
        "volumes.pureAlcoholVolume" -> "2"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.lessThanExpected", Seq())
      )
    }

    "fail to bind when pure alcohol volume is empty and total litres value exceeds maximum" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "9999999999999999",
        "volumes.pureAlcoholVolume" -> ""
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError("volumes_totalLitresVolume", Seq("adjustmentVolume.error.maximumValue.totalLitresVolume"), Seq()),
        FormError("volumes_pureAlcoholVolume", Seq("adjustmentVolume.error.noValue.pureAlcoholVolume"), Seq())
      )
    }

    "fail to bind with decimal places error when pure alcohol volume and total litres have more than expected decimal places and are also out of range" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "999999999999.9999",
        "volumes.pureAlcoholVolume" -> "-12323.234423"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError(
          "volumes_totalLitresVolume",
          Seq("adjustmentVolume.error.decimalPlaces.totalLitresVolume"),
          Seq()
        ),
        FormError("volumes_pureAlcoholVolume", Seq("adjustmentVolume.error.decimalPlaces.pureAlcoholVolume"), Seq())
      )
    }
  }
}
