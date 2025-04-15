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

import forms.mappings.Mappings
import models.AlcoholRegime
import models.adjustment.SpoiltVolumeWithDuty
import play.api.data.Form
import play.api.i18n.Messages

import javax.inject.Inject

class SpoiltVolumeWithDutyFormProvider @Inject() extends Mappings {

  def apply(regime: AlcoholRegime)(implicit messages: Messages): Form[SpoiltVolumeWithDuty] =
    Form(
      "volumes" -> spoiltVolumesWithDuty(
        "spoiltVolumeWithDuty.error.invalid",
        "spoiltVolumeWithDuty.error.noValue",
        "spoiltVolumeWithDuty.error.decimalPlaces",
        "spoiltVolumeWithDuty.error.minimumValue",
        "spoiltVolumeWithDuty.error.maximumValue",
        "spoiltVolumeWithDuty.error.lessThanExpected",
        Seq(messages(regime.regimeMessageKey))
      )
    )
}
