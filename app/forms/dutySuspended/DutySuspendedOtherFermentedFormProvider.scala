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
import models.dutySuspended.DutySuspendedOtherFermented

class DutySuspendedOtherFermentedFormProvider @Inject() extends Mappings {

  def apply(): Form[DutySuspendedOtherFermented] = Form(
    mapping(
      "totalOtherFermented"         -> bigDecimal(
        2,
        "dutySuspendedOtherFermented.error.totalOtherFermented.required",
        "dutySuspendedOtherFermented.error.totalOtherFermented.nonNumeric",
        "dutySuspendedOtherFermented.error.totalOtherFermented.twoDecimalPlaces"
      ).verifying(
        minimumValue(BigDecimal(-999999999.99), "dutySuspendedOtherFermented.error.totalOtherFermented.minimumRequired")
      ).verifying(
        maximumValue(BigDecimal(999999999.99), "dutySuspendedOtherFermented.error.totalOtherFermented.maximumRequired")
      ),
      "pureAlcoholInOtherFermented" -> bigDecimal(
        4,
        "dutySuspendedOtherFermented.error.pureAlcoholInOtherFermented.required",
        "dutySuspendedOtherFermented.error.pureAlcoholInOtherFermented.nonNumeric",
        "dutySuspendedOtherFermented.error.pureAlcoholInOtherFermented.twoDecimalPlaces"
      ).verifying(
        minimumValue(
          BigDecimal(-999999999.9999),
          "dutySuspendedOtherFermented.error.pureAlcoholInOtherFermented.minimumRequired"
        )
      ).verifying(
        maximumValue(
          BigDecimal(999999999.9999),
          "dutySuspendedOtherFermented.error.pureAlcoholInOtherFermented.maximumRequired"
        )
      )
    )(DutySuspendedOtherFermented.apply)(DutySuspendedOtherFermented.unapply)
  )
}
