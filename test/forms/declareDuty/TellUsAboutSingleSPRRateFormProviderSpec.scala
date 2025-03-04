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

import forms.behaviours.StringFieldBehaviours
import org.mockito.MockitoSugar.mock
import play.api.data.FormError
import play.api.i18n.Messages

class TellUsAboutSingleSPRRateFormProviderSpec extends StringFieldBehaviours {

  val regime = regimeGen.sample.value

  val messages = mock[Messages]

  val form = new TellUsAboutSingleSPRRateFormProvider()(regime)(messages)

  val rateBands               = genListOfRateBandForRegimeWithSPR(regime).sample.value.toSet
  val volumeAndRateByTaxTypes = rateBands.map(genVolumeAndRateByTaxTypeRateBand(_).arbitrary.sample.value).toSeq

  "volumesWithRate array" - {
    "must bind valid data" in {
      val values: Map[String, String] = volumeAndRateByTaxTypes.foldRight(Map[String, String]()) {
        (volumeAndRateByTaxType, acc: Map[String, String]) =>
          acc ++ Map(
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].totalLitres" -> volumeAndRateByTaxType.totalLitres.toString,
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].pureAlcohol" -> volumeAndRateByTaxType.pureAlcohol.toString,
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].taxType"     -> volumeAndRateByTaxType.taxType,
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].dutyRate"    -> volumeAndRateByTaxType.dutyRate.toString
          )
      }

      val result = form.bind(values)
      result.value.value mustEqual volumeAndRateByTaxTypes
    }

    "must unbind a valid data" in {
      val result = form.fill(volumeAndRateByTaxTypes).data
      volumeAndRateByTaxTypes.foreach { volumeAndRateByTaxType =>
        result(
          s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].totalLitres"
        ) mustBe volumeAndRateByTaxType.totalLitres.toString
        result(
          s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].pureAlcohol"
        ) mustBe volumeAndRateByTaxType.pureAlcohol.toString
        result(
          s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].taxType"
        ) mustBe volumeAndRateByTaxType.taxType
        result(
          s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].dutyRate"
        ) mustBe volumeAndRateByTaxType.dutyRate.toString
      }
    }

    "must fail to bind when no data is provided" in {
      val result = form.bind(Map.empty[String, String])
      result.errors must contain(FormError("volumesWithRate", "return.journey.error.allRequired", Seq(Seq(""))))
    }

    "must fail when empty data is provided" in {
      val values = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            s"volumesWithRate[$index].rateBandDescription" -> "",
            s"volumesWithRate[$index].taxType"             -> "",
            s"volumesWithRate[$index].totalLitres"         -> "",
            s"volumesWithRate[$index].pureAlcohol"         -> "",
            s"volumesWithRate[$index].dutyRate"            -> ""
          )
      }.flatten.toMap

      val expectedErrors = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            FormError(
              s"volumesWithRate_${index}_taxType",
              "return.journey.error.noValue.taxType",
              Seq("", "")
            ),
            FormError(
              s"volumesWithRate_${index}_totalLitres",
              "return.journey.error.noValue.totalLitres",
              Seq("", "")
            ),
            FormError(
              s"volumesWithRate_${index}_pureAlcohol",
              "return.journey.error.noValue.pureAlcohol",
              Seq("", "")
            ),
            FormError(
              s"volumesWithRate_${index}_dutyRate",
              "return.journey.error.noValue.dutyRate",
              Seq("", "")
            )
          )
      }.flatten

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when invalid data is provided" in {
      val values = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            s"volumesWithRate[$index].rateBandDescription" -> s"rate_band_recap_$index",
            s"volumesWithRate[$index].taxType"             -> s"tax_type_$index",
            s"volumesWithRate[$index].totalLitres"         -> "invalid",
            s"volumesWithRate[$index].pureAlcohol"         -> "invalid",
            s"volumesWithRate[$index].dutyRate"            -> "invalid"
          )
      }.flatten.toMap

      val expectedErrors = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            FormError(
              s"volumesWithRate_${index}_totalLitres",
              "return.journey.error.invalid.totalLitres",
              Seq(s"rate_band_recap_$index", "")
            ),
            FormError(
              s"volumesWithRate_${index}_pureAlcohol",
              "return.journey.error.invalid.pureAlcohol",
              Seq(s"rate_band_recap_$index", "")
            ),
            FormError(
              s"volumesWithRate_${index}_dutyRate",
              "return.journey.error.invalid.dutyRate",
              Seq(s"rate_band_recap_$index", "")
            )
          )
      }.flatten

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when data with too many decimal places is provided" in {
      val values = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            s"volumesWithRate[$index].rateBandDescription" -> s"rate_band_recap_$index",
            s"volumesWithRate[$index].taxType"             -> s"tax_type_$index",
            s"volumesWithRate[$index].totalLitres"         -> "1.123",
            s"volumesWithRate[$index].pureAlcohol"         -> "1.12345",
            s"volumesWithRate[$index].dutyRate"            -> "1.123"
          )
      }.flatten.toMap

      val expectedErrors = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            FormError(
              s"volumesWithRate_${index}_totalLitres",
              "return.journey.error.tooManyDecimalPlaces.totalLitres",
              Seq(s"rate_band_recap_$index", "")
            ),
            FormError(
              s"volumesWithRate_${index}_pureAlcohol",
              "return.journey.error.tooManyDecimalPlaces.pureAlcohol",
              Seq(s"rate_band_recap_$index", "")
            ),
            FormError(
              s"volumesWithRate_${index}_dutyRate",
              "return.journey.error.tooManyDecimalPlaces.dutyRate",
              Seq(s"rate_band_recap_$index", "")
            )
          )
      }.flatten

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when data exceeding maximum value is provided" in {
      val values = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            s"volumesWithRate[$index].rateBandDescription" -> s"rate_band_recap_$index",
            s"volumesWithRate[$index].taxType"             -> s"tax_type_$index",
            s"volumesWithRate[$index].totalLitres"         -> "100000000000",
            s"volumesWithRate[$index].pureAlcohol"         -> "100000000000.0000",
            s"volumesWithRate[$index].dutyRate"            -> "100000000000"
          )
      }.flatten.toMap

      val expectedErrors = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            FormError(
              s"volumesWithRate_${index}_totalLitres",
              "return.journey.error.maximumValue.totalLitres",
              Seq(s"rate_band_recap_$index", "")
            ),
            FormError(
              s"volumesWithRate_${index}_pureAlcohol",
              "return.journey.error.maximumValue.pureAlcohol",
              Seq(s"rate_band_recap_$index", "")
            ),
            FormError(
              s"volumesWithRate_${index}_dutyRate",
              "return.journey.error.maximumValue.dutyRate",
              Seq(s"rate_band_recap_$index", "")
            )
          )
      }.flatten

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when data below minimum value is provided" in {
      val values = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            s"volumesWithRate[$index].rateBandDescription" -> s"rate_band_recap_$index",
            s"volumesWithRate[$index].taxType"             -> s"tax_type_$index",
            s"volumesWithRate[$index].totalLitres"         -> "0",
            s"volumesWithRate[$index].pureAlcohol"         -> "0.0000",
            s"volumesWithRate[$index].dutyRate"            -> "-1"
          )
      }.flatten.toMap

      val expectedErrors = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            FormError(
              s"volumesWithRate_${index}_totalLitres",
              "return.journey.error.minimumValue.totalLitres",
              Seq(s"rate_band_recap_$index", "")
            ),
            FormError(
              s"volumesWithRate_${index}_pureAlcohol",
              "return.journey.error.minimumValue.pureAlcohol",
              Seq(s"rate_band_recap_$index", "")
            ),
            FormError(
              s"volumesWithRate_${index}_dutyRate",
              "return.journey.error.minimumValue.dutyRate",
              Seq(s"rate_band_recap_$index", "")
            )
          )
      }.flatten

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when pure alcohol is more than total litres" in {
      val values = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            s"volumesWithRate[$index].rateBandDescription" -> s"rate_band_recap_$index",
            s"volumesWithRate[$index].taxType"             -> s"tax_type_$index",
            s"volumesWithRate[$index].totalLitres"         -> "1.1",
            s"volumesWithRate[$index].pureAlcohol"         -> "100.1000",
            s"volumesWithRate[$index].dutyRate"            -> "1.1"
          )
      }.flatten.toMap

      val expectedErrors = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            FormError(
              s"volumesWithRate_${index}_pureAlcohol",
              "return.journey.error.lessThanExpected",
              Seq(s"rate_band_recap_$index", "")
            )
          )
      }.flatten

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when pure alcohol volume is empty and total litres value exceeds maximum and and dutyRate is invalid" in {
      val values = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            s"volumesWithRate[$index].rateBandDescription" -> s"rate_band_recap_$index",
            s"volumesWithRate[$index].taxType"             -> s"tax_type_$index",
            s"volumesWithRate[$index].totalLitres"         -> "999999999999",
            s"volumesWithRate[$index].pureAlcohol"         -> "",
            s"volumesWithRate[$index].dutyRate"            -> "1.1abc"
          )
      }.flatten.toMap

      val expectedErrors = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            FormError(
              s"volumesWithRate_${index}_totalLitres",
              "return.journey.error.maximumValue.totalLitres",
              Seq(s"rate_band_recap_$index", "")
            ),
            FormError(
              s"volumesWithRate_${index}_pureAlcohol",
              "return.journey.error.noValue.pureAlcohol",
              Seq(s"rate_band_recap_$index", "")
            ),
            FormError(
              s"volumesWithRate_${index}_dutyRate",
              "return.journey.error.invalid.dutyRate",
              Seq(s"rate_band_recap_$index", "")
            )
          )
      }.flatten

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "fail to bind with decimal places error when pure alcohol volume and total litres have more than expected decimal places and are also out of range" in {
      val values = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            s"volumesWithRate[$index].rateBandDescription" -> s"rate_band_recap_$index",
            s"volumesWithRate[$index].taxType"             -> s"tax_type_$index",
            s"volumesWithRate[$index].totalLitres"         -> "9999999999.123",
            s"volumesWithRate[$index].pureAlcohol"         -> "-671.12345",
            s"volumesWithRate[$index].dutyRate"            -> "99999999999.546"
          )
      }.flatten.toMap

      val expectedErrors = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            FormError(
              s"volumesWithRate_${index}_totalLitres",
              "return.journey.error.tooManyDecimalPlaces.totalLitres",
              Seq(s"rate_band_recap_$index", "")
            ),
            FormError(
              s"volumesWithRate_${index}_pureAlcohol",
              "return.journey.error.tooManyDecimalPlaces.pureAlcohol",
              Seq(s"rate_band_recap_$index", "")
            ),
            FormError(
              s"volumesWithRate_${index}_dutyRate",
              "return.journey.error.tooManyDecimalPlaces.dutyRate",
              Seq(s"rate_band_recap_$index", "")
            )
          )
      }.flatten

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }
  }
}
