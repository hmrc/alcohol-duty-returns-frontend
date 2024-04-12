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
import models.dutySuspended.DutySuspendedWine

class DutySuspendedWineFormProvider @Inject() extends Mappings {

   def apply(): Form[DutySuspendedWine] = Form(
     mapping(
       "totalWine" -> bigDecimal(
         2,
         "dutySuspendedWine.error.totalWine.required",
         "dutySuspendedWine.error.totalWine.nonNumeric",
         "dutySuspendedWine.error.totalWine.twoDecimalPlaces"
       ).verifying(minimumValue(BigDecimal(0.01), "dutySuspendedWine.error.totalWine.minimumRequired"))
         .verifying(maximumValue(BigDecimal(999999999.99), "dutySuspendedWine.error.totalWine.maximumRequired")),
       "pureAlcoholInWine" -> bigDecimal(
         2,
         "dutySuspendedWine.error.pureAlcoholInWine.required",
         "dutySuspendedWine.error.pureAlcoholInWine.nonNumeric",
         "dutySuspendedWine.error.pureAlcoholInWine.twoDecimalPlaces"
       ).verifying(minimumValue(BigDecimal(0.01), "dutySuspendedWine.error.pureAlcoholInWine.minimumRequired"))
         .verifying(maximumValue(BigDecimal(999999999.99), "dutySuspendedWine.error.pureAlcoholInWine.maximumRequired"))
     )(DutySuspendedWine.apply)(DutySuspendedWine.unapply)
   )
 }
