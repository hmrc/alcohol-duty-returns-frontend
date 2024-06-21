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

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import generators.ModelGenerators
import models.returns.VolumeAndRateByTaxType
import play.api.data.FormError
import play.api.i18n.Messages

class TellUsAboutMultipleSPRRateFormProviderSpec extends StringFieldBehaviours with ModelGenerators with SpecBase {

  val regime = regimeGen.sample.value

  val messages = mock[Messages]

  val rateBands               = genListOfRateBandForRegime(regime).sample.value.toSet
  val volumeAndRateByTaxTypes = rateBands.map(genVolumeAndRateByTaxTypeRateBand(_).arbitrary.sample.value).toSeq

  val form = new TellUsAboutMultipleSPRRateFormProvider()(regime)(messages)

  "volumesWithRate" - {
    "must bind valid data" in {
      volumeAndRateByTaxTypes.foreach { volumeAndRateByTaxType: VolumeAndRateByTaxType =>
        val result = form.bind(
          Map(
            "volumesWithRate.totalLitres" -> volumeAndRateByTaxType.totalLitres.toString,
            "volumesWithRate.pureAlcohol" -> volumeAndRateByTaxType.pureAlcohol.toString,
            "volumesWithRate.taxType"     -> volumeAndRateByTaxType.taxType,
            "volumesWithRate.dutyRate"    -> volumeAndRateByTaxType.dutyRate.toString
          )
        )
        result.value.value mustEqual volumeAndRateByTaxType
      }
    }

    "must unbind a valid data" in {
      volumeAndRateByTaxTypes.foreach { volumeAndRateByTaxType: VolumeAndRateByTaxType =>
        val result = form.fill(volumeAndRateByTaxType).data
        result("volumesWithRate.totalLitres") mustBe volumeAndRateByTaxType.totalLitres.toString
        result("volumesWithRate.pureAlcohol") mustBe volumeAndRateByTaxType.pureAlcohol.toString
        result("volumesWithRate.taxType") mustBe volumeAndRateByTaxType.taxType
        result("volumesWithRate.dutyRate") mustBe volumeAndRateByTaxType.dutyRate.toString
      }
    }

    "must fail to bind when no data is provided" in {
      val result = form.bind(Map.empty[String, String])
      result.errors must contain allElementsOf List(
        "volumesWithRate_totalLitres" -> "return.journey.error.noValue.totalLitres",
        "volumesWithRate_pureAlcohol" -> "return.journey.error.noValue.pureAlcohol",
        "volumesWithRate_taxType"     -> "return.journey.error.noValue.taxType",
        "volumesWithRate_dutyRate"    -> "return.journey.error.noValue.dutyRate"
      ).map { case (k, v) => FormError(k, v, List("")) }
    }

    "must fail to bind when invalid data is provided" in {
      val result = form.bind(
        Map(
          "volumesWithRate.taxType"     -> "aTaxType",
          "volumesWithRate.totalLitres" -> "invalid",
          "volumesWithRate.pureAlcohol" -> "invalid",
          "volumesWithRate.dutyRate"    -> "invalid"
        )
      )
      result.errors must contain allElementsOf List(
        "volumesWithRate_totalLitres" -> "return.journey.error.invalid.totalLitres",
        "volumesWithRate_pureAlcohol" -> "return.journey.error.invalid.pureAlcohol",
        "volumesWithRate_dutyRate"    -> "return.journey.error.invalid.dutyRate"
      ).map { case (k, v) => FormError(k, v, List("")) }
    }


  }

}
