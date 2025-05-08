/*
 * Copyright 2025 HM Revenue & Customs
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

package forms.dutySuspendedNew

import config.Constants
import forms.mappings.Mappings
import models.AlcoholRegime
import models.dutySuspendedNew.DutySuspendedQuantities
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class DutySuspendedQuantitiesFormProvider @Inject() extends Mappings {
  def apply(regime: AlcoholRegime): Form[DutySuspendedQuantities] = Form(
    mapping(
      "totalLitresDeliveredInsideUK"  -> bigDecimal(
        Constants.maximumTwoDecimalPlaces,
        s"dutySuspended.deliveredInsideUK.error.noValue.totalLitres.${regime.regimeMessageKey}",
        s"dutySuspended.deliveredInsideUK.error.invalid.totalLitres.${regime.regimeMessageKey}",
        s"dutySuspended.deliveredInsideUK.error.decimalPlaces.totalLitres.${regime.regimeMessageKey}"
      ).verifying(
        minimumValue(
          Constants.dutySuspendedVolumeNewMinimumValue,
          s"dutySuspended.deliveredInsideUK.error.minimumValue.totalLitres.${regime.regimeMessageKey}"
        )
      ).verifying(
        maximumValue(
          Constants.dutySuspendedVolumeMaximumValue,
          s"dutySuspended.deliveredInsideUK.error.maximumValue.totalLitres.${regime.regimeMessageKey}"
        )
      ),
      "pureAlcoholDeliveredInsideUK"  -> bigDecimal(
        Constants.lpaMaximumDecimalPlaces,
        "dutySuspended.deliveredInsideUK.error.noValue.pureAlcohol",
        "dutySuspended.deliveredInsideUK.error.invalid.pureAlcohol",
        "dutySuspended.deliveredInsideUK.error.decimalPlaces.pureAlcohol"
      ).verifying(
        minimumValue(
          Constants.dutySuspendedLpaNewMinimumValue,
          "dutySuspended.deliveredInsideUK.error.minimumValue.pureAlcohol"
        )
      ).verifying(
        maximumValue(
          Constants.dutySuspendedLpaMaximumValue,
          "dutySuspended.deliveredInsideUK.error.maximumValue.pureAlcohol"
        )
      ),
      "totalLitresDeliveredOutsideUK" -> bigDecimal(
        Constants.maximumTwoDecimalPlaces,
        s"dutySuspended.deliveredOutsideUK.error.noValue.totalLitres.${regime.regimeMessageKey}",
        s"dutySuspended.deliveredOutsideUK.error.invalid.totalLitres.${regime.regimeMessageKey}",
        s"dutySuspended.deliveredOutsideUK.error.decimalPlaces.totalLitres.${regime.regimeMessageKey}"
      ).verifying(
        minimumValue(
          Constants.dutySuspendedVolumeNewMinimumValue,
          s"dutySuspended.deliveredOutsideUK.error.minimumValue.totalLitres.${regime.regimeMessageKey}"
        )
      ).verifying(
        maximumValue(
          Constants.dutySuspendedVolumeMaximumValue,
          s"dutySuspended.deliveredOutsideUK.error.maximumValue.totalLitres.${regime.regimeMessageKey}"
        )
      ),
      "pureAlcoholDeliveredOutsideUK" -> bigDecimal(
        Constants.lpaMaximumDecimalPlaces,
        "dutySuspended.deliveredOutsideUK.error.noValue.pureAlcohol",
        "dutySuspended.deliveredOutsideUK.error.invalid.pureAlcohol",
        "dutySuspended.deliveredOutsideUK.error.decimalPlaces.pureAlcohol"
      ).verifying(
        minimumValue(
          Constants.dutySuspendedLpaNewMinimumValue,
          "dutySuspended.deliveredOutsideUK.error.minimumValue.pureAlcohol"
        )
      ).verifying(
        maximumValue(
          Constants.dutySuspendedLpaMaximumValue,
          "dutySuspended.deliveredOutsideUK.error.maximumValue.pureAlcohol"
        )
      ),
      "totalLitresReceived"           -> bigDecimal(
        Constants.maximumTwoDecimalPlaces,
        s"dutySuspended.received.error.noValue.totalLitres.${regime.regimeMessageKey}",
        s"dutySuspended.received.error.invalid.totalLitres.${regime.regimeMessageKey}",
        s"dutySuspended.received.error.decimalPlaces.totalLitres.${regime.regimeMessageKey}"
      ).verifying(
        minimumValue(
          Constants.dutySuspendedVolumeNewMinimumValue,
          s"dutySuspended.received.error.minimumValue.totalLitres.${regime.regimeMessageKey}"
        )
      ).verifying(
        maximumValue(
          Constants.dutySuspendedVolumeMaximumValue,
          s"dutySuspended.received.error.maximumValue.totalLitres.${regime.regimeMessageKey}"
        )
      ),
      "pureAlcoholReceived"           -> bigDecimal(
        Constants.lpaMaximumDecimalPlaces,
        "dutySuspended.received.error.noValue.pureAlcohol",
        "dutySuspended.received.error.invalid.pureAlcohol",
        "dutySuspended.received.error.decimalPlaces.pureAlcohol"
      ).verifying(
        minimumValue(
          Constants.dutySuspendedLpaNewMinimumValue,
          "dutySuspended.received.error.minimumValue.pureAlcohol"
        )
      ).verifying(
        maximumValue(
          Constants.dutySuspendedLpaMaximumValue,
          "dutySuspended.received.error.maximumValue.pureAlcohol"
        )
      )
    )(DutySuspendedQuantities.apply)(DutySuspendedQuantities.unapply)
  )
}
