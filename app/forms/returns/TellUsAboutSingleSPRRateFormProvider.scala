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

package forms.returns

import javax.inject.Inject
import forms.mappings.Mappings
import models.AlcoholRegime
import play.api.data.Form
import play.api.data.Forms._
import models.returns.DutyByTaxType

class TellUsAboutSingleSPRRateFormProvider @Inject() extends Mappings {

  def apply(regime: AlcoholRegime): Form[Seq[DutyByTaxType]] = Form(
    "volumesWithRate" -> seq(
      volumesWithRate(
        s"tellUsAboutSingleSPRRate.error.invalid.$regime",
        s"tellUsAboutSingleSPRRate.error.allRequired.$regime",
        s"tellUsAboutSingleSPRRate.error.required.$regime",
        s"tellUsAboutSingleSPRRate.error.decimalPlacesKey.$regime",
        s"tellUsAboutSingleSPRRate.error.minimumValueKey.$regime",
        s"tellUsAboutSingleSPRRate.error.maximumValueKey.$regime"
      )
    ).verifying(nonEmptySeq(s"tellUsAboutSingleSPRRate.error.allRequired.$regime"))
  )
}
