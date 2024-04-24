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

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.spiritsQuestions.Whisky

class WhiskyFormProvider @Inject() extends Mappings {

  def apply(): Form[Whisky] = Form(
      mapping (
      "ScotchWhisky" -> bigDecimal(
        2,
        "whisky.error.ScotchWhisky.required",
        "whisky.error.ScotchWhisky.nonNumeric",
        "whisky.error.ScotchWhisky.twoDecimalPlaces"
      ).verifying(minimumValue(BigDecimal(0.00), "whisky.error.ScotchWhisky.minimumRequired"))
        .verifying(maximumValue(BigDecimal(999999999.99), "whisky.error.ScotchWhisky.maximumRequired")),
      "IrishWhiskey" -> bigDecimal(
        2,
        "whisky.error.IrishWhiskey.required",
        "whisky.error.IrishWhiskey.nonNumeric",
        "whisky.error.IrishWhiskey.twoDecimalPlaces"
      ).verifying(minimumValue(BigDecimal(0.00), "whisky.error.IrishWhiskey.minimumRequired"))
        .verifying(maximumValue(BigDecimal(999999999.99), "whisky.error.IrishWhiskey.maximumRequired"))
    )
    (Whisky.apply)(Whisky.unapply)
  )
}
