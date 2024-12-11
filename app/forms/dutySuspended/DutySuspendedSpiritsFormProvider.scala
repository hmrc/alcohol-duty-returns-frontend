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

import config.Constants

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.dutySuspended.DutySuspendedSpirits

class DutySuspendedSpiritsFormProvider @Inject() extends Mappings {
  def apply(): Form[DutySuspendedSpirits] = Form(
    mapping(
      "totalSpirits"         -> bigDecimal(
        Constants.maximumDecimalPlaces,
        "dutySuspendedSpirits.error.totalSpirits.required",
        "dutySuspendedSpirits.error.totalSpirits.nonNumeric",
        "dutySuspendedSpirits.error.totalSpirits.decimalPlaces"
      ).verifying(
        minimumValue(
          Constants.dutySuspendedVolumeMinimumValue,
          "dutySuspendedSpirits.error.totalSpirits.minimumRequired"
        )
      ).verifying(
        maximumValue(
          Constants.dutySuspendedVolumeMaximumValue,
          "dutySuspendedSpirits.error.totalSpirits.maximumRequired"
        )
      ),
      "pureAlcoholInSpirits" -> bigDecimal(
        Constants.lpaMaximumDecimalPlaces,
        "dutySuspendedSpirits.error.pureAlcoholInSpirits.required",
        "dutySuspended.error.pureAlcohol.nonNumeric",
        "dutySuspended.error.pureAlcohol.decimalPlaces"
      ).verifying(
        minimumValue(
          Constants.dutySuspendedLpaMinimumValue,
          "dutySuspended.error.pureAlcohol.minimumRequired"
        )
      ).verifying(
        maximumValue(
          Constants.dutySuspendedLpaMaximumValue,
          "dutySuspended.error.pureAlcohol.maximumRequired"
        )
      )
    )(DutySuspendedSpirits.apply)(DutySuspendedSpirits.unapply)
  )
}
