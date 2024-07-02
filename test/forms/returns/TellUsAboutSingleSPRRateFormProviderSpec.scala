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

package forms.returns

import forms.behaviours.StringFieldBehaviours
import models.returns.VolumeAndRateByTaxType
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
      result.errors must contain(FormError("volumesWithRate", "return.journey.error.allRequired", Seq(List(""))))
    }

    "must fail when empty data is provided" in {
      val values: Map[String, String] = volumeAndRateByTaxTypes.foldRight(Map[String, String]()) {
        (volumeAndRateByTaxType, acc: Map[String, String]) =>
          acc ++ Map(
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].taxType"     -> "",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].totalLitres" -> "",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].pureAlcohol" -> "",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].dutyRate"    -> ""
          )
      }

      val expectedErrors = volumeAndRateByTaxTypes.foldRight(List[FormError]()) {
        (volumeAndRateByTaxType, acc: List[FormError]) =>
          acc ++ List(
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_taxType",
              "return.journey.error.noValue.taxType",
              List("")
            ),
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_totalLitres",
              "return.journey.error.noValue.totalLitres",
              List("")
            ),
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_pureAlcohol",
              "return.journey.error.noValue.pureAlcohol",
              List("")
            ),
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_dutyRate",
              "return.journey.error.noValue.dutyRate",
              List("")
            )
          )
      }

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when invalid data is provided" in {
      val values: Map[String, String] = volumeAndRateByTaxTypes.foldRight(Map[String, String]()) {
        (volumeAndRateByTaxType, acc: Map[String, String]) =>
          acc ++ Map(
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].taxType"     -> volumeAndRateByTaxType.taxType,
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].totalLitres" -> "invalid",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].pureAlcohol" -> "invalid",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].dutyRate"    -> "invalid"
          )
      }

      val expectedErrors = volumeAndRateByTaxTypes.foldRight(List[FormError]()) {
        (volumeAndRateByTaxType, acc: List[FormError]) =>
          acc ++ List(
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_totalLitres",
              "return.journey.error.invalid.totalLitres",
              List("")
            ),
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_pureAlcohol",
              "return.journey.error.invalid.pureAlcohol",
              List("")
            ),
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_dutyRate",
              "return.journey.error.invalid.dutyRate",
              List("")
            )
          )
      }

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when data with too many decimal places is provided" in {
      val values: Map[String, String] = volumeAndRateByTaxTypes.foldRight(Map[String, String]()) {
        (volumeAndRateByTaxType, acc: Map[String, String]) =>
          acc ++ Map(
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].taxType"     -> volumeAndRateByTaxType.taxType,
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].totalLitres" -> "1.123",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].pureAlcohol" -> "1.123",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].dutyRate"    -> "1.123"
          )
      }

      val expectedErrors = volumeAndRateByTaxTypes.foldRight(List[FormError]()) {
        (volumeAndRateByTaxType, acc: List[FormError]) =>
          acc ++ List(
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_totalLitres",
              "return.journey.error.tooManyDecimalPlaces.totalLitres",
              List("")
            ),
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_pureAlcohol",
              "return.journey.error.tooManyDecimalPlaces.pureAlcohol",
              List("")
            ),
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_dutyRate",
              "return.journey.error.tooManyDecimalPlaces.dutyRate",
              List("")
            )
          )
      }

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when data exceeding maximum value is provided" in {
      val values: Map[String, String] = volumeAndRateByTaxTypes.foldRight(Map[String, String]()) {
        (volumeAndRateByTaxType, acc: Map[String, String]) =>
          acc ++ Map(
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].taxType"     -> volumeAndRateByTaxType.taxType,
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].totalLitres" -> "100000000000",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].pureAlcohol" -> "100000000000",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].dutyRate"    -> "100000000000"
          )
      }

      val expectedErrors = volumeAndRateByTaxTypes.foldRight(List[FormError]()) {
        (volumeAndRateByTaxType, acc: List[FormError]) =>
          acc ++ List(
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_totalLitres",
              "return.journey.error.maximumValue.totalLitres",
              List("")
            ),
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_pureAlcohol",
              "return.journey.error.maximumValue.pureAlcohol",
              List("")
            ),
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_dutyRate",
              "return.journey.error.maximumValue.dutyRate",
              List("")
            )
          )
      }

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when data below minimum value is provided" in {
      val values: Map[String, String] = volumeAndRateByTaxTypes.foldRight(Map[String, String]()) {
        (volumeAndRateByTaxType, acc: Map[String, String]) =>
          acc ++ Map(
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].taxType"     -> volumeAndRateByTaxType.taxType,
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].totalLitres" -> "0",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].pureAlcohol" -> "0",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].dutyRate"    -> "-1"
          )
      }

      val expectedErrors = volumeAndRateByTaxTypes.foldRight(List[FormError]()) {
        (volumeAndRateByTaxType, acc: List[FormError]) =>
          acc ++ List(
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_totalLitres",
              "return.journey.error.minimumValue.totalLitres",
              List("")
            ),
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_pureAlcohol",
              "return.journey.error.minimumValue.pureAlcohol",
              List("")
            ),
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_dutyRate",
              "return.journey.error.minimumValue.dutyRate",
              List("")
            )
          )
      }

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "must fail when pure alcohol is more than total litres" in {
      val values: Map[String, String] = volumeAndRateByTaxTypes.foldRight(Map[String, String]()) {
        (volumeAndRateByTaxType, acc: Map[String, String]) =>
          acc ++ Map(
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].taxType"     -> volumeAndRateByTaxType.taxType,
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].totalLitres" -> "1.1",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].pureAlcohol" -> "100.1",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].dutyRate"    -> "1.1"
          )
      }

      val expectedErrors = volumeAndRateByTaxTypes.foldRight(List[FormError]()) {
        (volumeAndRateByTaxType, acc: List[FormError]) =>
          acc ++ List(
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_totalLitres",
              "return.journey.error.moreThanExpected",
              List("")
            ),
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_pureAlcohol",
              "return.journey.error.lessThanExpected",
              List("")
            )
          )
      }

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }
  }
}
