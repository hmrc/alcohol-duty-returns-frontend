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

package forms.adjustment

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.adjustment.AdjustmentRepackagedTaxTypeWithSPR

class AdjustmentRepackagedTaxTypeWithSPRFormProvider @Inject() extends Mappings {

   def apply(): Form[AdjustmentRepackagedTaxTypeWithSPR] = Form(
     mapping(
      "new-tax-type-code" -> int(
        "adjustmentRepackagedTaxType.error.required",
        "adjustmentRepackagedTaxType.error.valid",
        "adjustmentRepackagedTaxType.error.valid")
        .verifying(inRange(0, 999, "adjustmentRepackagedTaxType.error.valid")),
      "new-spr-duty-rate" ->
        bigDecimal (
        2,
        "adjustmentRepackagedTaxTypeWithSPR.smallProducerReliefDutyRate.error.required",
        "adjustmentRepackagedTaxTypeWithSPR.smallProducerReliefDutyRate.error.nonNumeric",
        "adjustmentRepackagedTaxTypeWithSPR.smallProducerReliefDutyRate.error.twoDecimalPlaces"
      )
        .verifying(
          minimumValue(BigDecimal(0.00), "adjustmentRepackagedTaxTypeWithSPR.smallProducerReliefDutyRate.error.minimumRequired")
        )
        .verifying(
          maximumValue(
            BigDecimal(999999999.99),
            "adjustmentRepackagedTaxTypeWithSPR.smallProducerReliefDutyRate.error.maximumRequired"
          )
        )
    )(AdjustmentRepackagedTaxTypeWithSPR.apply)(AdjustmentRepackagedTaxTypeWithSPR.unapply)
   )
 }
