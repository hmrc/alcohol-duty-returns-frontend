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
import models.declareDuty.VolumeAndRateByTaxType
import play.api.data.FormError
import play.api.i18n.Messages

class TellUsAboutMultipleSPRRateFormProviderSpec extends StringFieldBehaviours with SpecBase {

  val regime = regimeGen.sample.value

  val messages = mock[Messages]

  val rateBands               = genListOfRateBandForRegimeWithSPR(regime).sample.value.toSet
  val volumeAndRateByTaxTypes = arbitraryVolumeAndRateByTaxType(
    rateBands.toSeq
  ).arbitrary.sample.value

  val form = new TellUsAboutMultipleSPRRateFormProvider()()

  "volumesWithRate" - {
    "must bind valid data" in
      volumeAndRateByTaxTypes.foreach { (volumeAndRateByTaxType: VolumeAndRateByTaxType) =>
        val result = form.bind(
          Map(
            "volumesWithRate.taxType"     -> volumeAndRateByTaxType.taxType,
            "volumesWithRate.totalLitres" -> volumeAndRateByTaxType.totalLitres.toString,
            "volumesWithRate.pureAlcohol" -> volumeAndRateByTaxType.pureAlcohol.toString,
            "volumesWithRate.dutyRate"    -> volumeAndRateByTaxType.dutyRate.toString
          )
        )
        result.value.value mustEqual volumeAndRateByTaxType
      }

    "must unbind a valid data" in
      volumeAndRateByTaxTypes.foreach { (volumeAndRateByTaxType: VolumeAndRateByTaxType) =>
        val result = form.fill(volumeAndRateByTaxType).data
        result("volumesWithRate.totalLitres") mustBe volumeAndRateByTaxType.totalLitres.toString
        result("volumesWithRate.pureAlcohol") mustBe volumeAndRateByTaxType.pureAlcohol.toString
        result("volumesWithRate.dutyRate")    mustBe volumeAndRateByTaxType.dutyRate.toString
      }

    "must fail to bind when no data is provided" in {
      val result = form.bind(Map.empty[String, String])
      result.errors must contain allElementsOf Seq(
        "volumesWithRate_totalLitres" -> "return.journey.multipleSPR.error.noValue.totalLitres",
        "volumesWithRate_pureAlcohol" -> "return.journey.multipleSPR.error.noValue.pureAlcohol",
        "volumesWithRate_taxType"     -> "return.journey.multipleSPR.error.noValue.taxType",
        "volumesWithRate_dutyRate"    -> "return.journey.multipleSPR.error.noValue.dutyRate"
      ).map { case (k, v) => FormError(k, v, Seq.empty) }
    }

    "must fail to bind when invalid data is provided" in {
      val result = form.bind(
        Map(
          "volumesWithRate.taxType"     -> "311",
          "volumesWithRate.totalLitres" -> "invalid",
          "volumesWithRate.pureAlcohol" -> "invalid",
          "volumesWithRate.dutyRate"    -> "invalid"
        )
      )
      result.errors must contain allElementsOf Seq(
        "volumesWithRate_totalLitres" -> "return.journey.multipleSPR.error.invalid.totalLitres",
        "volumesWithRate_pureAlcohol" -> "return.journey.multipleSPR.error.invalid.pureAlcohol",
        "volumesWithRate_dutyRate"    -> "return.journey.multipleSPR.error.invalid.dutyRate"
      ).map { case (k, v) => FormError(k, v, Seq.empty) }
    }

    "must fail when data with too many decimal places is provided" in {
      val result = form.bind(
        Map(
          "volumesWithRate.taxType"     -> "123",
          "volumesWithRate.totalLitres" -> "1.123",
          "volumesWithRate.pureAlcohol" -> "1.12345",
          "volumesWithRate.dutyRate"    -> "1.123"
        )
      )
      result.errors must contain allElementsOf Seq(
        "volumesWithRate_totalLitres" -> "return.journey.multipleSPR.error.tooManyDecimalPlaces.totalLitres",
        "volumesWithRate_pureAlcohol" -> "return.journey.multipleSPR.error.tooManyDecimalPlaces.pureAlcohol",
        "volumesWithRate_dutyRate"    -> "return.journey.multipleSPR.error.tooManyDecimalPlaces.dutyRate"
      ).map { case (k, v) => FormError(k, v, Seq.empty) }
    }

    "must fail when data exceeding maximum value is provided" in {
      val result = form.bind(
        Map(
          "volumesWithRate.taxType"     -> "123",
          "volumesWithRate.totalLitres" -> "100000000000.00",
          "volumesWithRate.pureAlcohol" -> "100000000000.0000",
          "volumesWithRate.dutyRate"    -> "100000000000"
        )
      )

      result.errors must contain allElementsOf Seq(
        "volumesWithRate_totalLitres" -> "return.journey.multipleSPR.error.maximumValue.totalLitres",
        "volumesWithRate_pureAlcohol" -> "return.journey.multipleSPR.error.maximumValue.pureAlcohol",
        "volumesWithRate_dutyRate"    -> "return.journey.multipleSPR.error.maximumValue.dutyRate"
      ).map { case (k, v) => FormError(k, v, Seq.empty) }
    }

    "must fail when data below minimum value is provided" in {
      val result = form.bind(
        Map(
          "volumesWithRate.taxType"     -> "123",
          "volumesWithRate.totalLitres" -> "0.00",
          "volumesWithRate.pureAlcohol" -> "0.0000",
          "volumesWithRate.dutyRate"    -> "-1"
        )
      )

      result.errors must contain allElementsOf Seq(
        "volumesWithRate_totalLitres" -> "return.journey.multipleSPR.error.minimumValue.totalLitres",
        "volumesWithRate_pureAlcohol" -> "return.journey.multipleSPR.error.minimumValue.pureAlcohol",
        "volumesWithRate_dutyRate"    -> "return.journey.multipleSPR.error.minimumValue.dutyRate"
      ).map { case (k, v) => FormError(k, v, Seq.empty) }
    }

    "must fail when pure alcohol is more than total litres" in {
      val result = form.bind(
        Map(
          "volumesWithRate.taxType"     -> "123",
          "volumesWithRate.totalLitres" -> "1.10",
          "volumesWithRate.pureAlcohol" -> "100.1000",
          "volumesWithRate.dutyRate"    -> "1.1"
        )
      )

      result.errors must contain allElementsOf Seq(
        "volumesWithRate_pureAlcohol" -> "return.journey.multipleSPR.error.lessThanExpected"
      ).map { case (k, v) => FormError(k, v, Seq.empty) }
    }

    "must fail to bind when pure alcohol volume is empty and total litres value exceeds maximum and and dutyRate is invalid" in {
      val data = Map(
        "volumesWithRate.taxType"     -> "123",
        "volumesWithRate.totalLitres" -> "99999999999.00",
        "volumesWithRate.pureAlcohol" -> "",
        "volumesWithRate.dutyRate"    -> "abc"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError(
          "volumesWithRate_totalLitres",
          Seq("return.journey.multipleSPR.error.maximumValue.totalLitres"),
          Seq.empty
        ),
        FormError(
          "volumesWithRate_pureAlcohol",
          Seq("return.journey.multipleSPR.error.noValue.pureAlcohol"),
          Seq.empty
        ),
        FormError(
          "volumesWithRate_dutyRate",
          Seq("return.journey.multipleSPR.error.invalid.dutyRate"),
          Seq.empty
        )
      )
    }

    "must fail to bind with decimal places error when pure alcohol volume and total litres have more than expected decimal places and are also out of range" in {
      val data = Map(
        "volumesWithRate.taxType"     -> "123",
        "volumesWithRate.totalLitres" -> "111111111111.234",
        "volumesWithRate.pureAlcohol" -> "-2.45356",
        "volumesWithRate.dutyRate"    -> "99999999999.546"
      )
      form.bind(data).errors must contain allElementsOf Seq(
        FormError(
          "volumesWithRate_totalLitres",
          Seq("return.journey.multipleSPR.error.tooManyDecimalPlaces.totalLitres"),
          Seq.empty
        ),
        FormError(
          "volumesWithRate_pureAlcohol",
          Seq("return.journey.multipleSPR.error.tooManyDecimalPlaces.pureAlcohol"),
          Seq.empty
        ),
        FormError(
          "volumesWithRate_dutyRate",
          Seq("return.journey.multipleSPR.error.tooManyDecimalPlaces.dutyRate"),
          Seq.empty
        )
      )
    }
  }
}
