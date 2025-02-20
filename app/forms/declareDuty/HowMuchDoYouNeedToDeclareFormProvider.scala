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

package forms.declareDuty

import javax.inject.Inject
import forms.mappings.Mappings
import models.AlcoholRegime
import play.api.data.Form
import play.api.data.Forms._
import models.declareDuty.VolumesByTaxType
import play.api.i18n.Messages

class HowMuchDoYouNeedToDeclareFormProvider @Inject() extends Mappings {

  def apply(regime: AlcoholRegime)(implicit messages: Messages): Form[Seq[VolumesByTaxType]] = Form(
    "volumes" -> seq(
      volumes(
        "return.journey.error.invalid",
        "return.journey.error.noValue",
        "return.journey.error.tooManyDecimalPlaces",
        "return.journey.error.minimumValue",
        "return.journey.error.maximumValue",
        "return.journey.error.lessThanExpected",
        messages(s"return.regime.$regime")
      )
    ).verifying(nonEmptySeq("return.journey.error.allRequired", Seq(messages(s"return.regime.$regime"))))
  )
}
