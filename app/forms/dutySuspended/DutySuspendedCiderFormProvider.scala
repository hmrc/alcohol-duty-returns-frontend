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

package forms.dutySuspended

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.dutySuspended.DutySuspendedCider

class DutySuspendedCiderFormProvider @Inject() extends Mappings {

   def apply(): Form[DutySuspendedCider] = Form(
     mapping(
       "totalCider" -> bigDecimal(
         2,
         "dutySuspendedCider.error.totalCider.required",
         "dutySuspendedCider.error.totalCider.nonNumeric",
         "dutySuspendedCider.error.totalCider.twoDecimalPlaces"
       ).verifying(minimumValue(BigDecimal(0.01), "dutySuspendedCider.error.totalCider.minimumRequired"))
         .verifying(maximumValue(BigDecimal(999999999.99), "dutySuspendedCider.error.totalCider.maximumRequired")),
       "pureAlcoholInCider" -> bigDecimal(
         2,
         "dutySuspendedCider.error.pureAlcoholInCider.required",
         "dutySuspendedCider.error.pureAlcoholInCider.nonNumeric",
         "dutySuspendedCider.error.pureAlcoholInCider.twoDecimalPlaces"
       ).verifying(minimumValue(BigDecimal(0.01), "dutySuspendedCider.error.pureAlcoholInCider.minimumRequired"))
         .verifying(maximumValue(BigDecimal(999999999.99), "dutySuspendedCider.error.pureAlcoholInCider.maximumRequired"))
     )(DutySuspendedCider.apply)(DutySuspendedCider.unapply)
   )
 }
