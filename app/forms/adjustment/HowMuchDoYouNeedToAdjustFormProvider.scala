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
import models.adjustment.HowMuchDoYouNeedToAdjust

class HowMuchDoYouNeedToAdjustFormProvider @Inject() extends Mappings {

  def apply(): Form[HowMuchDoYouNeedToAdjust] = Form(
    mapping(
      "totalLitersVolume"           -> bigDecimal(
        2,
        "howMuchDoYouNeedToAdjust.error.totalLitersVolume.required",
        "howMuchDoYouNeedToAdjust.error.totalLitersVolume.nonNumeric",
        "howMuchDoYouNeedToAdjust.error.totalLitersVolume.twoDecimalPlaces"
      ).verifying(minimumValue(BigDecimal(0.01), "howMuchDoYouNeedToAdjust.error.totalLitersVolume.minimumRequired"))
        .verifying(
          maximumValue(BigDecimal(999999999.99), "howMuchDoYouNeedToAdjust.error.totalLitersVolume.maximumRequired")
        ),
      "pureAlcoholVolume"           -> bigDecimal(
        2,
        "howMuchDoYouNeedToAdjust.error.pureAlcoholVolume.required",
        "howMuchDoYouNeedToAdjust.error.pureAlcoholVolume.nonNumeric",
        "howMuchDoYouNeedToAdjust.error.pureAlcoholVolume.twoDecimalPlaces"
      ).verifying(minimumValue(BigDecimal(0.01), "howMuchDoYouNeedToAdjust.error.pureAlcoholVolume.minimumRequired"))
        .verifying(
          maximumValue(BigDecimal(999999999.99), "howMuchDoYouNeedToAdjust.error.pureAlcoholVolume.maximumRequired")
        ),
      "smallProducerReliefDutyRate" -> optional(
        bigDecimal(
          2,
          "howMuchDoYouNeedToAdjust.smallProducerReliefDutyRate.error.required",
          "howMuchDoYouNeedToAdjust.smallProducerReliefDutyRate.error.nonNumeric",
          "howMuchDoYouNeedToAdjust.smallProducerReliefDutyRate.error.twoDecimalPlaces"
        )
          .verifying(
            minimumValue(BigDecimal(0.00), "howMuchDoYouNeedToAdjust.smallProducerReliefDutyRate.error.minimumRequired")
          )
          .verifying(
            maximumValue(
              BigDecimal(999999999.99),
              "howMuchDoYouNeedToAdjust.smallProducerReliefDutyRate.error.maximumRequired"
            )
          )
      )
    )(HowMuchDoYouNeedToAdjust.apply)(HowMuchDoYouNeedToAdjust.unapply)
  )
}
