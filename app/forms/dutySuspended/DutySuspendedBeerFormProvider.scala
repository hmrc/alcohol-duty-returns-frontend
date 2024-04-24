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
import models.dutySuspended.DutySuspendedBeer

class DutySuspendedBeerFormProvider @Inject() extends Mappings {

  def apply(): Form[DutySuspendedBeer] = Form(
    mapping(
      "totalBeer"         -> bigDecimal(
        2,
        "dutySuspendedBeer.error.totalBeer.required",
        "dutySuspendedBeer.error.totalBeer.nonNumeric",
        "dutySuspendedBeer.error.totalBeer.twoDecimalPlaces"
      ).verifying(minimumValue(BigDecimal(0.00), "dutySuspendedBeer.error.totalBeer.minimumRequired"))
        .verifying(maximumValue(BigDecimal(999999999.99), "dutySuspendedBeer.error.totalBeer.maximumRequired")),
      "pureAlcoholInBeer" -> bigDecimal(
        2,
        "dutySuspendedBeer.error.pureAlcoholInBeer.required",
        "dutySuspendedBeer.error.pureAlcoholInBeer.nonNumeric",
        "dutySuspendedBeer.error.pureAlcoholInBeer.twoDecimalPlaces"
      ).verifying(minimumValue(BigDecimal(0.00), "dutySuspendedBeer.error.pureAlcoholInBeer.minimumRequired"))
        .verifying(maximumValue(BigDecimal(999999999.99), "dutySuspendedBeer.error.pureAlcoholInBeer.maximumRequired"))
    )(DutySuspendedBeer.apply)(DutySuspendedBeer.unapply)
  )
}
