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

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.returns.{DutyByTaxType, TellUsAboutMultipleSPRRate}

class TellUsAboutMultipleSPRRateFormProvider @Inject() extends Mappings {

   def apply(): Form[DutyByTaxType] = Form(
     mapping(
      "taxType" -> text("tellUsAboutMultipleSPRRate.error.field1.required"),
      "totalLitres"      -> bigDecimal(
       2,
       "tellUsAboutMultipleSPRRate.error.totalLitres.required",
       "tellUsAboutMultipleSPRRate.error.totalLitres.nonNumeric",
       "tellUsAboutMultipleSPRRate.error.totalLitres.twoDecimalPlaces"
     ).verifying(minimumValue(BigDecimal(0.00), "tellUsAboutMultipleSPRRate.error.totalLitres.minimumRequired"))
       .verifying(
         maximumValue(BigDecimal(999999999.99), "tellUsAboutMultipleSPRRate.error.totalLitres.maximumRequired")
       ),
       "pureAlcohol"      -> bigDecimal(
         2,
         "tellUsAboutMultipleSPRRate.error.pureAlcohol.required",
         "tellUsAboutMultipleSPRRate.error.pureAlcohol.nonNumeric",
         "tellUsAboutMultipleSPRRate.error.pureAlcohol.twoDecimalPlaces"
       ).verifying(minimumValue(BigDecimal(0.00), "tellUsAboutMultipleSPRRate.error.pureAlcohol.minimumRequired"))
         .verifying(
           maximumValue(BigDecimal(999999999.99), "tellUsAboutMultipleSPRRate.error.pureAlcohol.maximumRequired")
         ),
       "dutyRate"      -> bigDecimal(
         2,
         "tellUsAboutMultipleSPRRate.error.dutyRate.required",
         "tellUsAboutMultipleSPRRate.error.dutyRate.nonNumeric",
         "tellUsAboutMultipleSPRRate.error.dutyRate.twoDecimalPlaces"
       ).verifying(minimumValue(BigDecimal(0.00), "tellUsAboutMultipleSPRRate.error.dutyRate.minimumRequired"))
         .verifying(
           maximumValue(BigDecimal(999999999.99), "tellUsAboutMultipleSPRRate.error.dutyRate.maximumRequired")
         ),
    )(DutyByTaxType.apply)(DutyByTaxType.unapply)
   )
 }
