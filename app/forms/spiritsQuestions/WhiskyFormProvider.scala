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

package forms.spiritsQuestions

import config.Constants

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.spiritsQuestions.Whisky

class WhiskyFormProvider @Inject() extends Mappings {
  def apply(): Form[Whisky] = Form(
    mapping(
      "scotchWhisky" -> bigDecimal(
        Constants.maximumTwoDecimalPlaces,
        "whisky.error.scotchWhisky.required",
        "whisky.error.scotchWhisky.nonNumeric",
        "whisky.error.scotchWhisky.decimalPlaces"
      ).verifying(minimumValue(Constants.volumeMinimumValueIncZero, "whisky.error.scotchWhisky.minimumRequired"))
        .verifying(maximumValue(Constants.volumeMaximumValue, "whisky.error.scotchWhisky.maximumRequired")),
      "irishWhiskey" -> bigDecimal(
        Constants.maximumTwoDecimalPlaces,
        "whisky.error.irishWhiskey.required",
        "whisky.error.irishWhiskey.nonNumeric",
        "whisky.error.irishWhiskey.decimalPlaces"
      ).verifying(minimumValue(Constants.volumeMinimumValueIncZero, "whisky.error.irishWhiskey.minimumRequired"))
        .verifying(maximumValue(Constants.volumeMaximumValue, "whisky.error.irishWhiskey.maximumRequired"))
    )(Whisky.apply)(Whisky.unapply)
  )
}
