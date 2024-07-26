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
import play.api.i18n.Messages

class AdjustmentVolumeWithSPRFormProviderSpec extends StringFieldBehaviours with ModelGenerators with SpecBase {

  val regime = regimeGen.sample.value

  val messages = mock[Messages]

  val validTotalLitres = BigDecimal(10.23)
  val validPureAlcohol = BigDecimal(9.23)
  val validSPRDutyRate = BigDecimal(2)

  val adjustmentEntry = AdjustmentEntry(
    totalLitresVolume = Some(validTotalLitres),
    pureAlcoholVolume = Some(validPureAlcohol),
    sprDutyRate = Some(validSPRDutyRate)
  )

  val adjustmentVolumeWithSPR = AdjustmentVolumeWithSPR(validTotalLitres, validPureAlcohol, validSPRDutyRate)
  val form                    = new AdjustmentVolumeWithSPRFormProvider()(regime)(messages)

  ".volumes" - {
    /*"must bind valid data" in {
      val data = Map(
        "volumes.totalLitresVolume" -> validTotalLitres.toString(),
        "volumes.pureAlcoholVolume" -> validPureAlcohol.toString(),
      "volumes.sprDutyRate" -> validSPRDutyRate.toString()
      )
      form.bind(data).data mustBe AdjustmentVolumeWithSPR(validTotalLitres,validPureAlcohol,validSPRDutyRate)
    }
     */

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
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitresVolume", "adjustmentVolume.error.noValue.totalLitresVolume", Seq("")),
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.noValue.pureAlcoholVolume", Seq("")),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.noValue.sprDutyRate", Seq(""))
      )
    }

    "fail to bind when blank answer provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "",
        "volumes.pureAlcoholVolume" -> "",
        "volumes.sprDutyRate"       -> ""
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitresVolume", "adjustmentVolume.error.noValue.totalLitresVolume", Seq("")),
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.noValue.pureAlcoholVolume", Seq("")),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.noValue.sprDutyRate", Seq(""))
      )
    }

    "fail to bind when values with too many decimal places are provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "1.1123",
        "volumes.pureAlcoholVolume" -> "1.1123",
        "volumes.sprDutyRate"       -> "1.1123"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitresVolume", s"adjustmentVolume.error.twoDecimalPlaces.totalLitresVolume", Seq("")),
        FormError("volumes_pureAlcoholVolume", s"adjustmentVolume.error.twoDecimalPlaces.pureAlcoholVolume", Seq("")),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.twoDecimalPlaces.sprDutyRate", Seq(""))
      )
    }

    "fail to bind when invalid values are provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "invalid",
        "volumes.pureAlcoholVolume" -> "invalid",
        "volumes.sprDutyRate"       -> "invalid"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitresVolume", "adjustmentVolume.error.invalid.totalLitresVolume", List("")),
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.invalid.pureAlcoholVolume", List("")),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.invalid.sprDutyRate", Seq(""))
      )
    }

    "fail to bind when values below minimum are provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "0",
        "volumes.pureAlcoholVolume" -> "0",
        "volumes.sprDutyRate"       -> "-21"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitresVolume", "adjustmentVolume.error.minimumValue.totalLitresVolume", List("")),
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.minimumValue.pureAlcoholVolume", List("")),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.minimumValue.sprDutyRate", Seq(""))
      )
    }

    "fail to bind when values exceed maximum are provided" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "100000000000",
        "volumes.pureAlcoholVolume" -> "100000000000",
        "volumes.sprDutyRate"       -> "100000000000"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_totalLitresVolume", "adjustmentVolume.error.maximumValue.totalLitresVolume", List("")),
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.maximumValue.pureAlcoholVolume", List("")),
        FormError("volumes_sprDutyRate", "adjustmentVolume.error.maximumValue.sprDutyRate", Seq(""))
      )
    }

    "fail to bind when pure alcohol volume is higher than total litres value" in {
      val data = Map(
        "volumes.totalLitresVolume" -> "1",
        "volumes.pureAlcoholVolume" -> "2",
        "volumes.sprDutyRate"       -> "0"
      )
      form.bind(data).errors must contain allElementsOf List(
        FormError("volumes_pureAlcoholVolume", "adjustmentVolume.error.lessThanExpected", List(""))
      )
    }
  }

}
