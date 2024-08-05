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

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class AdjustmentSmallProducerReliefDutyRateFormProvider @Inject() extends Mappings {

  def apply(): Form[BigDecimal] =
    Form(
      "adjustment-small-producer-relief-duty-rate-input" -> bigDecimal(
        2,
        "adjustmentSmallProducerReliefDutyRate.error.required",
        "adjustmentSmallProducerReliefDutyRate.error.nonNumeric",
        "adjustmentSmallProducerReliefDutyRate.error.decimalPlaces"
      )
        .verifying(minimumValue(BigDecimal(0.00), "adjustmentSmallProducerReliefDutyRate.error.minimumRequired"))
        .verifying(
          maximumValue(BigDecimal(999999999.99), "adjustmentSmallProducerReliefDutyRate.error.maximumRequired")
        )
    )
}
