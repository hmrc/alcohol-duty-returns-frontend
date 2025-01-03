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
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].sprDutyRate" -> volumeAndRateByTaxType.dutyRate.toString
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
          s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].sprDutyRate"
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
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].sprDutyRate" -> ""
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
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_sprDutyRate",
              "return.journey.error.noValue.sprDutyRate",
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
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].taxType"     -> s"tax_type_${volumeAndRateByTaxTypes
              .indexOf(volumeAndRateByTaxType)}",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].totalLitres" -> "invalid",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].pureAlcohol" -> "invalid",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].sprDutyRate" -> "invalid"
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
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_sprDutyRate",
              "return.journey.error.invalid.sprDutyRate",
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
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].taxType"     -> s"tax_type_${volumeAndRateByTaxTypes
              .indexOf(volumeAndRateByTaxType)}",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].totalLitres" -> "1.123",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].pureAlcohol" -> "1.12345",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].sprDutyRate" -> "1.123"
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
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_sprDutyRate",
              "return.journey.error.tooManyDecimalPlaces.sprDutyRate",
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
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].taxType"     -> s"tax_type_${volumeAndRateByTaxTypes
              .indexOf(volumeAndRateByTaxType)}",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].totalLitres" -> "100000000000",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].pureAlcohol" -> "100000000000.0000",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].sprDutyRate" -> "100000000000"
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
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_sprDutyRate",
              "return.journey.error.maximumValue.sprDutyRate",
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
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].taxType"     -> s"tax_type_${volumeAndRateByTaxTypes
              .indexOf(volumeAndRateByTaxType)}",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].totalLitres" -> "0",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].pureAlcohol" -> "0.0000",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].sprDutyRate" -> "-1"
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
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_sprDutyRate",
              "return.journey.error.minimumValue.sprDutyRate",
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
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].taxType"     -> s"tax_type_${volumeAndRateByTaxTypes
              .indexOf(volumeAndRateByTaxType)}",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].totalLitres" -> "1.1",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].pureAlcohol" -> "100.1000",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].sprDutyRate" -> "1.1"
          )
      }

      val expectedErrors = volumeAndRateByTaxTypes.foldRight(List[FormError]()) {
        (volumeAndRateByTaxType, acc: List[FormError]) =>
          acc ++ List(
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

    "must fail when pure alcohol volume is empty and total litres value exceeds maximum and and sprDutyRate is invalid" in {
      val values: Map[String, String] = volumeAndRateByTaxTypes.foldRight(Map[String, String]()) {
        (volumeAndRateByTaxType, acc: Map[String, String]) =>
          acc ++ Map(
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].taxType"     -> s"tax_type_${volumeAndRateByTaxTypes
              .indexOf(volumeAndRateByTaxType)}",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].totalLitres" -> "999999999999",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].pureAlcohol" -> "",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].sprDutyRate" -> "1.1abc"
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
              "return.journey.error.noValue.pureAlcohol",
              List("")
            ),
            FormError(
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_sprDutyRate",
              "return.journey.error.invalid.sprDutyRate",
              List("")
            )
          )
      }

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }

    "fail to bind with decimal places error when pure alcohol volume and total litres have more than expected decimal places and are also out of range" in {
      val values: Map[String, String] = volumeAndRateByTaxTypes.foldRight(Map[String, String]()) {
        (volumeAndRateByTaxType, acc: Map[String, String]) =>
          acc ++ Map(
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].taxType"     -> s"tax_type_${volumeAndRateByTaxTypes
              .indexOf(volumeAndRateByTaxType)}",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].totalLitres" -> "9999999999.123",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].pureAlcohol" -> "-671.12345",
            s"volumesWithRate[${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}].sprDutyRate" -> "99999999999.546"
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
              s"volumesWithRate_${volumeAndRateByTaxTypes.indexOf(volumeAndRateByTaxType)}_sprDutyRate",
              "return.journey.error.tooManyDecimalPlaces.sprDutyRate",
              List("")
            )
          )
      }

      val result = form.bind(values)
      result.errors must contain allElementsOf expectedErrors
    }
  }
}
