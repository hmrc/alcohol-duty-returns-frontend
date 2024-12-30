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
import models.dutySuspended.DutySuspendedWine

class DutySuspendedWineFormProvider @Inject() extends Mappings {
  def apply(): Form[DutySuspendedWine] = Form(
    mapping(
      "totalWine"         -> bigDecimal(
        Constants.maximumTwoDecimalPlaces,
        "dutySuspendedWine.error.totalWine.required",
        "dutySuspendedWine.error.totalWine.nonNumeric",
        "dutySuspendedWine.error.totalWine.decimalPlaces"
      ).verifying(
        minimumValue(Constants.dutySuspendedVolumeMinimumValue, "dutySuspendedWine.error.totalWine.minimumRequired")
      ).verifying(
        maximumValue(Constants.dutySuspendedVolumeMaximumValue, "dutySuspendedWine.error.totalWine.maximumRequired")
      ),
      "pureAlcoholInWine" -> bigDecimal(
        Constants.lpaMaximumDecimalPlaces,
        "dutySuspendedWine.error.pureAlcoholInWine.required",
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
    )(DutySuspendedWine.apply)(DutySuspendedWine.unapply)
  )
}
