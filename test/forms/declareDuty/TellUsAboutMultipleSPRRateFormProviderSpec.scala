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
import models.declareDuty.VolumeAndRateByTaxType
import play.api.data.FormError
import play.api.i18n.Messages

class TellUsAboutMultipleSPRRateFormProviderSpec extends StringFieldBehaviours with ModelGenerators with SpecBase {

  val regime = regimeGen.sample.value

  val messages = mock[Messages]

  val rateBands               = genListOfRateBandForRegimeWithSPR(regime).sample.value.toSet
  val volumeAndRateByTaxTypes = arbitraryVolumeAndRateByTaxType(
    rateBands.toSeq
  ).arbitrary.sample.value

  val form = new TellUsAboutMultipleSPRRateFormProvider()(regime)(messages)

  "volumesWithRate" - {
    "must bind valid data" in {
      volumeAndRateByTaxTypes.foreach { volumeAndRateByTaxType: VolumeAndRateByTaxType =>
        val result = form.bind(
          Map(
            "volumesWithRate.rateBandRecap" -> volumeAndRateByTaxType.rateBandRecap,
            "volumesWithRate.taxType"       -> volumeAndRateByTaxType.taxType,
            "volumesWithRate.totalLitres"   -> volumeAndRateByTaxType.totalLitres.toString,
            "volumesWithRate.pureAlcohol"   -> volumeAndRateByTaxType.pureAlcohol.toString,
            "volumesWithRate.dutyRate"      -> volumeAndRateByTaxType.dutyRate.toString
          )
        )
        result.value.value mustEqual volumeAndRateByTaxType
      }
    }

    "must unbind a valid data" in {
      volumeAndRateByTaxTypes.foreach { volumeAndRateByTaxType: VolumeAndRateByTaxType =>
        val result = form.fill(volumeAndRateByTaxType).data
        result("volumesWithRate.rateBandRecap") mustBe volumeAndRateByTaxType.rateBandRecap
        result("volumesWithRate.taxType") mustBe volumeAndRateByTaxType.taxType
        result("volumesWithRate.totalLitres") mustBe volumeAndRateByTaxType.totalLitres.toString
        result("volumesWithRate.pureAlcohol") mustBe volumeAndRateByTaxType.pureAlcohol.toString
        result("volumesWithRate.dutyRate") mustBe volumeAndRateByTaxType.dutyRate.toString
      }
    }

    "must fail to bind when no data is provided" in {
      val result = form.bind(Map.empty[String, String])
      result.errors must contain allElementsOf Seq(
        "volumesWithRate_totalLitres" -> "return.journey.error.noValue.totalLitres",
        "volumesWithRate_pureAlcohol" -> "return.journey.error.noValue.pureAlcohol",
        "volumesWithRate_taxType"     -> "return.journey.error.noValue.taxType",
        "volumesWithRate_dutyRate"    -> "return.journey.error.noValue.dutyRate"
      ).map { case (k, v) => FormError(k, v, Seq("", "")) }
    }

    "must fail to bind when invalid data is provided" in {
      val result = form.bind(
        Map(
          "volumesWithRate.rateBandRecap" -> rateBandRecap,
          "volumesWithRate.taxType"       -> "aTaxType",
          "volumesWithRate.totalLitres"   -> "invalid",
          "volumesWithRate.pureAlcohol"   -> "invalid",
          "volumesWithRate.dutyRate"      -> "invalid"
        )
      )
      result.errors must contain allElementsOf Seq(
        "volumesWithRate_totalLitres" -> "return.journey.error.invalid.totalLitres",
        "volumesWithRate_pureAlcohol" -> "return.journey.error.invalid.pureAlcohol",
        "volumesWithRate_dutyRate"    -> "return.journey.error.invalid.dutyRate"
      ).map { case (k, v) => FormError(k, v, Seq(rateBandRecap, "")) }
    }

    "must fail when data with too many decimal places is provided" in {
      val result = form.bind(
        Map(
          "volumesWithRate.rateBandRecap" -> rateBandRecap,
          "volumesWithRate.taxType"       -> "123",
          "volumesWithRate.totalLitres"   -> "1.123",
          "volumesWithRate.pureAlcohol"   -> "1.12345",
          "volumesWithRate.dutyRate"      -> "1.123"
        )
      )
      result.errors must contain allElementsOf Seq(
        "volumesWithRate_totalLitres" -> "return.journey.error.tooManyDecimalPlaces.totalLitres",
        "volumesWithRate_pureAlcohol" -> "return.journey.error.tooManyDecimalPlaces.pureAlcohol",
        "volumesWithRate_dutyRate"    -> "return.journey.error.tooManyDecimalPlaces.dutyRate"
      ).map { case (k, v) => FormError(k, v, Seq(rateBandRecap, "")) }
    }

    "must fail when data exceeding maximum value is provided" in {
      val result = form.bind(
        Map(
          "volumesWithRate.rateBandRecap" -> rateBandRecap,
          "volumesWithRate.taxType"       -> "123",
          "volumesWithRate.totalLitres"   -> "100000000000",
          "volumesWithRate.pureAlcohol"   -> "100000000000.0000",
          "volumesWithRate.dutyRate"      -> "100000000000"
        )
      )

      result.errors must contain allElementsOf Seq(
        "volumesWithRate_totalLitres" -> "return.journey.error.maximumValue.totalLitres",
        "volumesWithRate_pureAlcohol" -> "return.journey.error.maximumValue.pureAlcohol",
        "volumesWithRate_dutyRate"    -> "return.journey.error.maximumValue.dutyRate"
      ).map { case (k, v) => FormError(k, v, Seq(rateBandRecap, "")) }
    }

    "must fail when data below minimum value is provided" in {
      val result = form.bind(
        Map(
          "volumesWithRate.rateBandRecap" -> rateBandRecap,
          "volumesWithRate.taxType"       -> "123",
          "volumesWithRate.totalLitres"   -> "0",
          "volumesWithRate.pureAlcohol"   -> "0.0000",
          "volumesWithRate.dutyRate"      -> "-1"
        )
      )

      result.errors must contain allElementsOf Seq(
        "volumesWithRate_totalLitres" -> "return.journey.error.minimumValue.totalLitres",
        "volumesWithRate_pureAlcohol" -> "return.journey.error.minimumValue.pureAlcohol",
        "volumesWithRate_dutyRate"    -> "return.journey.error.minimumValue.dutyRate"
      ).map { case (k, v) => FormError(k, v, Seq(rateBandRecap, "")) }
    }

    "must fail when pure alcohol is more than total litres" in {
      val result = form.bind(
        Map(
          "volumesWithRate.rateBandRecap" -> rateBandRecap,
          "volumesWithRate.taxType"       -> "123",
          "volumesWithRate.totalLitres"   -> "1.1",
          "volumesWithRate.pureAlcohol"   -> "100.1000",
          "volumesWithRate.dutyRate"      -> "1.1"
        )
      )

      result.errors must contain allElementsOf Seq(
        "volumesWithRate_pureAlcohol" -> "return.journey.error.lessThanExpected"
      ).map { case (k, v) => FormError(k, v, Seq(rateBandRecap, "")) }
    }

    "fail to bind when pure alcohol volume is empty and total litres value exceeds maximum and and dutyRate is invalid" in {
      val data = Map(
        "volumesWithRate.rateBandRecap" -> rateBandRecap,
        "volumesWithRate.taxType"       -> "123",
        "volumesWithRate.totalLitres"   -> "99999999999",
        "volumesWithRate.pureAlcohol"   -> "",
        "volumesWithRate.dutyRate"      -> "abc"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError(
          "volumesWithRate_totalLitres",
          Seq("return.journey.error.maximumValue.totalLitres"),
          Seq(rateBandRecap, "")
        ),
        FormError(
          "volumesWithRate_pureAlcohol",
          Seq("return.journey.error.noValue.pureAlcohol"),
          Seq(rateBandRecap, "")
        ),
        FormError("volumesWithRate_dutyRate", Seq("return.journey.error.invalid.dutyRate"), Seq(rateBandRecap, ""))
      )
    }

    "fail to bind with decimal places error when pure alcohol volume and total litres have more than expected decimal places and are also out of range" in {
      val data = Map(
        "volumesWithRate.rateBandRecap" -> rateBandRecap,
        "volumesWithRate.taxType"       -> "123",
        "volumesWithRate.totalLitres"   -> "111111111111.234",
        "volumesWithRate.pureAlcohol"   -> "-2.45356",
        "volumesWithRate.dutyRate"      -> "99999999999.546"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError(
          "volumesWithRate_totalLitres",
          Seq("return.journey.error.tooManyDecimalPlaces.totalLitres"),
          Seq(rateBandRecap, "")
        ),
        FormError(
          "volumesWithRate_pureAlcohol",
          Seq("return.journey.error.tooManyDecimalPlaces.pureAlcohol"),
          Seq(rateBandRecap, "")
        ),
        FormError(
          "volumesWithRate_dutyRate",
          Seq("return.journey.error.tooManyDecimalPlaces.dutyRate"),
          Seq(rateBandRecap, "")
        )
      )
    }
  }
}
