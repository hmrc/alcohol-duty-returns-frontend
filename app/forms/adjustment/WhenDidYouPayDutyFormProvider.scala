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

import config.Constants

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form

import java.time.YearMonth

class WhenDidYouPayDutyFormProvider @Inject() extends Mappings {
  def apply(returnPeriod: YearMonth): Form[YearMonth] = Form(
    "when-did-you-pay-duty-input" -> yearMonth(
      "whenDidYouPayDuty.date.error.invalid",
      "whenDidYouPayDuty.date.error.required.all",
      "whenDidYouPayDuty.date.error.required",
      "whenDidYouPayDuty.date.error.invalidYear"
    )
      .verifying("whenDidYouPayDuty.date.error.invalid.future", value => value.isBefore(returnPeriod))
      .verifying(
        "whenDidYouPayDuty.date.error.invalid.past",
        value => value.isAfter(YearMonth.of(Constants.adjustmentTooEarlyYear, Constants.adjustmentTooEarlyMonth))
      )
  )

}
