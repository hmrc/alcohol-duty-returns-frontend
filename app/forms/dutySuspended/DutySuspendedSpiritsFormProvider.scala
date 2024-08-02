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
import models.dutySuspended.DutySuspendedSpirits

class DutySuspendedSpiritsFormProvider @Inject() extends Mappings {

  def apply(): Form[DutySuspendedSpirits] = Form(
    mapping(
      "totalSpirits"         -> bigDecimal(
        2,
        "dutySuspendedSpirits.error.totalSpirits.required",
        "dutySuspendedSpirits.error.totalSpirits.nonNumeric",
        "dutySuspendedSpirits.error.totalSpirits.twoDecimalPlaces"
      ).verifying(minimumValue(BigDecimal(-999999999.99), "dutySuspendedSpirits.error.totalSpirits.minimumRequired"))
        .verifying(maximumValue(BigDecimal(999999999.99), "dutySuspendedSpirits.error.totalSpirits.maximumRequired")),
      "pureAlcoholInSpirits" -> bigDecimal(
        4,
        "dutySuspendedSpirits.error.pureAlcoholInSpirits.required",
        "dutySuspendedSpirits.error.pureAlcoholInSpirits.nonNumeric",
        "dutySuspendedSpirits.error.pureAlcoholInSpirits.twoDecimalPlaces"
      ).verifying(
        minimumValue(BigDecimal(-999999999.9999), "dutySuspendedSpirits.error.pureAlcoholInSpirits.minimumRequired")
      ).verifying(
        maximumValue(BigDecimal(999999999.9999), "dutySuspendedSpirits.error.pureAlcoholInSpirits.maximumRequired")
      )
    )(DutySuspendedSpirits.apply)(DutySuspendedSpirits.unapply)
  )
}
