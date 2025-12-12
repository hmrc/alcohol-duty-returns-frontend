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
import play.api.data.FormError
import play.api.i18n.Messages

class TellUsAboutSingleSPRRateFormProviderSpec extends StringFieldBehaviours with SpecBase {

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
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].rateBandDescription" -> rateBandDescription,
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].totalLitres"         -> volumeAndRateByTaxType.totalLitres.toString,
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].pureAlcohol"         -> volumeAndRateByTaxType.pureAlcohol.toString,
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].taxType"             -> volumeAndRateByTaxType.taxType,
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].dutyRate"            -> volumeAndRateByTaxType.dutyRate.toString
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

    "must fail when empty data is provided" in {
      val values = {
        for (index <- 0 until volumeAndRateByTaxTypes.length)
          yield Seq(
            s"volumesWithRate[$index].rateBandDescription" -> s"${rateBandDescription}_$index",
            s"volumesWithRate[$index].taxType"             -> "311",
            s"volumesWithRate[$index].totalLitres"         -> "",
            s"volumesWithRate[$index].pureAlcohol"         -> "",
            s"volumesWithRate[$index].dutyRate"            -> ""
          )
      }.flatten.toMap

      val expectedErrors = {
        for (index <- volumeAndRateByTaxTypes.indices)
          yield Seq(
            FormError(
              s"volumesWithRate_${index}_totalLitres",
              "return.journey.error.noValue.totalLitres",
              Seq(s"${rateBandDescription}_$index", null)
            ),
            FormError(
              s"volumesWithRate_${index}_pureAlcohol",
              "return.journey.error.noValue.pureAlcohol",
              Seq(s"${rateBandDescription}_$index", null)
            ),
            FormError(
              s"volumesWithRate_${index}_dutyRate",
              "return.journey.error.noValue.dutyRate",
              Seq(s"${rateBandDescription}_$index", null)
            )
          )
      }.flatten

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when invalid data is provided" in {
      val values = {
        for (index <- volumeAndRateByTaxTypes.indices)
          yield Seq(
            s"volumesWithRate[$index].rateBandDescription" -> s"${rateBandDescription}_$index",
            s"volumesWithRate[$index].taxType"             -> s"tax_type_$index",
            s"volumesWithRate[$index].totalLitres"         -> "invalid",
            s"volumesWithRate[$index].pureAlcohol"         -> "invalid",
            s"volumesWithRate[$index].dutyRate"            -> "invalid"
          )
      }.flatten.toMap

      val expectedErrors = {
        for (index <- volumeAndRateByTaxTypes.indices)
          yield Seq(
            FormError(
              s"volumesWithRate_${index}_totalLitres",
              "return.journey.error.invalid.totalLitres",
              Seq(s"${rateBandDescription}_$index", null)
            ),
            FormError(
              s"volumesWithRate_${index}_pureAlcohol",
              "return.journey.error.invalid.pureAlcohol",
              Seq(s"${rateBandDescription}_$index", null)
            ),
            FormError(
              s"volumesWithRate_${index}_dutyRate",
              "return.journey.error.invalid.dutyRate",
              Seq(s"${rateBandDescription}_$index", null)
            )
          )
      }.flatten

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when data with too many decimal places is provided" in {
      val values = {
        for (index <- volumeAndRateByTaxTypes.indices)
          yield Seq(
            s"volumesWithRate[$index].rateBandDescription" -> s"${rateBandDescription}_$index",
            s"volumesWithRate[$index].taxType"             -> s"tax_type_$index",
            s"volumesWithRate[$index].totalLitres"         -> "1.123",
            s"volumesWithRate[$index].pureAlcohol"         -> "1.12345",
            s"volumesWithRate[$index].dutyRate"            -> "1.123"
          )
      }.flatten.toMap

      val expectedErrors = {
        for (index <- volumeAndRateByTaxTypes.indices)
          yield Seq(
            FormError(
              s"volumesWithRate_${index}_totalLitres",
              "return.journey.error.tooManyDecimalPlaces.totalLitres",
              Seq(s"${rateBandDescription}_$index", null)
            ),
            FormError(
              s"volumesWithRate_${index}_pureAlcohol",
              "return.journey.error.tooManyDecimalPlaces.pureAlcohol",
              Seq(s"${rateBandDescription}_$index", null)
            ),
            FormError(
              s"volumesWithRate_${index}_dutyRate",
              "return.journey.error.tooManyDecimalPlaces.dutyRate",
              Seq(s"${rateBandDescription}_$index", null)
            )
          )
      }.flatten

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when data exceeding maximum value is provided" in {
      val values = {
        for (index <- volumeAndRateByTaxTypes.indices)
          yield Seq(
            s"volumesWithRate[$index].rateBandDescription" -> s"${rateBandDescription}_$index",
            s"volumesWithRate[$index].taxType"             -> s"tax_type_$index",
            s"volumesWithRate[$index].totalLitres"         -> "100000000000.00",
            s"volumesWithRate[$index].pureAlcohol"         -> "100000000000.0000",
            s"volumesWithRate[$index].dutyRate"            -> "100000000000"
          )
      }.flatten.toMap

      val expectedErrors = {
        for (index <- volumeAndRateByTaxTypes.indices)
          yield Seq(
            FormError(
              s"volumesWithRate_${index}_totalLitres",
              "return.journey.error.maximumValue.totalLitres",
              Seq(s"${rateBandDescription}_$index", null)
            ),
            FormError(
              s"volumesWithRate_${index}_pureAlcohol",
              "return.journey.error.maximumValue.pureAlcohol",
              Seq(s"${rateBandDescription}_$index", null)
            ),
            FormError(
              s"volumesWithRate_${index}_dutyRate",
              "return.journey.error.maximumValue.dutyRate",
              Seq(s"${rateBandDescription}_$index", null)
            )
          )
      }.flatten

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when data below minimum value is provided" in {
      val values = {
        for (index <- volumeAndRateByTaxTypes.indices)
          yield Seq(
            s"volumesWithRate[$index].rateBandDescription" -> s"${rateBandDescription}_$index",
            s"volumesWithRate[$index].taxType"             -> s"tax_type_$index",
            s"volumesWithRate[$index].totalLitres"         -> "0.00",
            s"volumesWithRate[$index].pureAlcohol"         -> "0.0000",
            s"volumesWithRate[$index].dutyRate"            -> "-1"
          )
      }.flatten.toMap

      val expectedErrors = {
        for (index <- volumeAndRateByTaxTypes.indices)
          yield Seq(
            FormError(
              s"volumesWithRate_${index}_totalLitres",
              "return.journey.error.minimumValue.totalLitres",
              Seq(s"${rateBandDescription}_$index", null)
            ),
            FormError(
              s"volumesWithRate_${index}_pureAlcohol",
              "return.journey.error.minimumValue.pureAlcohol",
              Seq(s"${rateBandDescription}_$index", null)
            ),
            FormError(
              s"volumesWithRate_${index}_dutyRate",
              "return.journey.error.minimumValue.dutyRate",
              Seq(s"${rateBandDescription}_$index", null)
            )
          )
      }.flatten

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when pure alcohol is more than total litres" in {
      val values = {
        for (index <- volumeAndRateByTaxTypes.indices)
          yield Seq(
            s"volumesWithRate[$index].rateBandDescription" -> s"${rateBandDescription}_$index",
            s"volumesWithRate[$index].taxType"             -> s"tax_type_$index",
            s"volumesWithRate[$index].totalLitres"         -> "1.10",
            s"volumesWithRate[$index].pureAlcohol"         -> "100.1000",
            s"volumesWithRate[$index].dutyRate"            -> "1.1"
          )
      }.flatten.toMap

      val expectedErrors = {
        for (index <- volumeAndRateByTaxTypes.indices)
          yield Seq(
            FormError(
              s"volumesWithRate_${index}_pureAlcohol",
              "return.journey.error.lessThanExpected",
              Seq(s"${rateBandDescription}_$index", null)
            )
          )
      }.flatten

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when pure alcohol volume is empty and total litres value exceeds maximum and and dutyRate is invalid" in {
      val values = {
        for (index <- volumeAndRateByTaxTypes.indices)
          yield Seq(
            s"volumesWithRate[$index].rateBandDescription" -> s"${rateBandDescription}_$index",
            s"volumesWithRate[$index].taxType"             -> s"tax_type_$index",
            s"volumesWithRate[$index].totalLitres"         -> "999999999999.00",
            s"volumesWithRate[$index].pureAlcohol"         -> "",
            s"volumesWithRate[$index].dutyRate"            -> "1.1abc"
          )
      }.flatten.toMap

      val expectedErrors = {
        for (index <- volumeAndRateByTaxTypes.indices)
          yield Seq(
            FormError(
              s"volumesWithRate_${index}_totalLitres",
              "return.journey.error.maximumValue.totalLitres",
              Seq(s"${rateBandDescription}_$index", null)
            ),
            FormError(
              s"volumesWithRate_${index}_pureAlcohol",
              "return.journey.error.noValue.pureAlcohol",
              Seq(s"${rateBandDescription}_$index", null)
            ),
            FormError(
              s"volumesWithRate_${index}_dutyRate",
              "return.journey.error.invalid.dutyRate",
              Seq(s"${rateBandDescription}_$index", null)
            )
          )
      }.flatten

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail to bind with decimal places error when pure alcohol volume and total litres have more than expected decimal places and are also out of range" in {
      val values = {
        for (index <- volumeAndRateByTaxTypes.indices)
          yield Seq(
            s"volumesWithRate[$index].rateBandDescription" -> s"${rateBandDescription}_$index",
            s"volumesWithRate[$index].taxType"             -> s"tax_type_$index",
            s"volumesWithRate[$index].totalLitres"         -> "9999999999.123",
            s"volumesWithRate[$index].pureAlcohol"         -> "-671.12345",
            s"volumesWithRate[$index].dutyRate"            -> "99999999999.546"
          )
      }.flatten.toMap

      val expectedErrors = {
        for (index <- volumeAndRateByTaxTypes.indices)
          yield Seq(
            FormError(
              s"volumesWithRate_${index}_totalLitres",
              "return.journey.error.tooManyDecimalPlaces.totalLitres",
              Seq(s"${rateBandDescription}_$index", null)
            ),
            FormError(
              s"volumesWithRate_${index}_pureAlcohol",
              "return.journey.error.tooManyDecimalPlaces.pureAlcohol",
              Seq(s"${rateBandDescription}_$index", null)
            ),
            FormError(
              s"volumesWithRate_${index}_dutyRate",
              "return.journey.error.tooManyDecimalPlaces.dutyRate",
              Seq(s"${rateBandDescription}_$index", null)
            )
          )
      }.flatten

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must throw an exception when rateBandDescription is not provided" in {
      val data = Map(
        "volumesWithRate[0].taxType"     -> "311",
        "volumesWithRate[0].totalLitres" -> "111111111111.234",
        "volumesWithRate[0].pureAlcohol" -> "-2.45356",
        "volumesWithRate[0].dutyRate"    -> "1.123"
      )

      an[IllegalArgumentException] mustBe thrownBy(form.bind(data).errors)
    }

    "must throw an exception when taxType is not provided" in {
      val data = Map(
        "volumesWithRate[0].rateBandDescription" -> rateBandDescription,
        "volumesWithRate[0].totalLitres"         -> "111111111111.234",
        "volumesWithRate[0].pureAlcohol"         -> "-2.45356",
        "volumesWithRate[0].dutyRate"            -> "1.123"
      )

      an[IllegalArgumentException] mustBe thrownBy(form.bind(data).errors)
    }
  }
}
