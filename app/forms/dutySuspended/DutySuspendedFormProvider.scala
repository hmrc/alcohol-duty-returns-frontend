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
import models.AlcoholRegime
import play.api.data.Form
import models.dutySuspended.DutySuspendedVolume
import play.api.i18n.Messages

class DutySuspendedFormProvider @Inject() extends Mappings {
  def apply(regime: AlcoholRegime)(implicit messages: Messages): Form[DutySuspendedVolume] = Form(
    "volumes" -> dutySuspendedVolumes(
      "dutySuspendedVolume.error.invalid",
      "dutySuspendedVolume.error.noValue",
      "dutySuspendedVolume.error.decimalPlaces",
      "dutySuspendedVolume.error.minimumValue",
      "dutySuspendedVolume.error.maximumValue",
      "dutySuspendedVolume.error.lessThanExpected",
      "dutySuspendedVolume.error.incorrectSign",
      "dutySuspendedVolume.error.zeroTotalLitres",
      Seq(messages(s"return.regime.$regime"))
    )
  )
}
