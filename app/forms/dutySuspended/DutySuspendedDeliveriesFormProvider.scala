/*
 * Copyright 2023 HM Revenue & Customs
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

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class DutySuspendedDeliveriesFormProvider @Inject() extends Mappings {

  def apply(): Form[BigDecimal] =
    Form(
      "duty-suspended-deliveries-input" -> bigDecimal(
        2,
        "dutySuspendedDeliveries.error.required",
        "dutySuspendedDeliveries.error.nonNumeric",
        "dutySuspendedDeliveries.error.twoDecimalPlaces"
      )
        .verifying(minimumValue(BigDecimal(0.00), "dutySuspendedDeliveries.error.minimumRequired"))
        .verifying(maximumValue(BigDecimal(999999999.99), "dutySuspendedDeliveries.error.maximumRequired"))
    )
}
