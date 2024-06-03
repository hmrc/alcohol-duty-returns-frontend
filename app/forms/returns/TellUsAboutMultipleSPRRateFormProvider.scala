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

import config.Constants._

import javax.inject.Inject
import forms.mappings.Mappings
import models.AlcoholRegime
import play.api.data.Form
import play.api.data.Forms._
import models.returns.DutyByTaxType

class TellUsAboutMultipleSPRRateFormProvider @Inject() extends Mappings {

  def apply(regime: AlcoholRegime): Form[DutyByTaxType] = Form(
    mapping(
      "taxType"     -> text(s"tellUsAboutMultipleSPRRate.error.taxType.$regime.required"),
      "totalLitres" -> bigDecimal(
        maximumDecimalPlaces,
        s"tellUsAboutSPRRate.error.required.$regime.totalLitres",
        s"tellUsAboutSPRRate.error.invalid.$regime.totalLitres",
        s"tellUsAboutSPRRate.error.decimalPlacesKey.$regime.totalLitres"
      ).verifying(
        minimumValue(volumeMinimumValue, s"tellUsAboutSPRRate.error.minimumValueKey.$regime.totalLitres")
      ).verifying(
        maximumValue(volumeMaximumValue, s"tellUsAboutSPRRate.error.maximumValueKey.$regime.totalLitres")
      ),
      "pureAlcohol" -> bigDecimal(
        maximumDecimalPlaces,
        s"tellUsAboutSPRRate.error.required.$regime.pureAlcohol",
        s"tellUsAboutSPRRate.error.invalid.$regime.pureAlcohol",
        s"tellUsAboutSPRRate.error.decimalPlacesKey.$regime.pureAlcohol"
      ).verifying(
        minimumValue(volumeMinimumValue, s"tellUsAboutSPRRate.error.minimumValueKey.$regime.pureAlcohol")
      ).verifying(
        maximumValue(volumeMaximumValue, s"tellUsAboutSPRRate.error.maximumValueKey.$regime.pureAlcohol")
      ),
      "dutyRate"    -> bigDecimal(
        maximumDecimalPlaces,
        s"tellUsAboutSPRRate.error.required.$regime.dutyRate",
        s"tellUsAboutSPRRate.error.invalid.$regime.dutyRate",
        s"tellUsAboutSPRRate.error.decimalPlacesKey.$regime.dutyRate"
      ).verifying(minimumValue(dutyMinimumValue, s"tellUsAboutSPRRate.error.minimumValueKey.$regime.dutyRate"))
        .verifying(
          maximumValue(dutyMaximumValue, s"tellUsAboutSPRRate.error.maximumValueKey.$regime.dutyRate")
        )
    )(DutyByTaxType.apply)(DutyByTaxType.unapply)
  )
}
