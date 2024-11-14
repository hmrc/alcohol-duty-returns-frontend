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
import models.spiritsQuestions.AlcoholUsed

class AlcoholUsedFormProvider @Inject() extends Mappings {
  def apply(): Form[AlcoholUsed] = Form(
    mapping(
      "beer"         -> bigDecimal(
        Constants.maximumDecimalPlaces,
        "alcoholUsed.error.beer.required",
        "alcoholUsed.error.beer.nonNumeric",
        "alcoholUsed.error.beer.decimalPlaces"
      )
        .verifying(minimumValue(Constants.volumeMinimumValueIncZero, "alcoholUsed.error.beer.minimumRequired"))
        .verifying(maximumValue(Constants.volumeMaximumValue, "alcoholUsed.error.beer.maximumRequired")),
      "wine"         -> bigDecimal(
        2,
        "alcoholUsed.error.wine.required",
        "alcoholUsed.error.wine.nonNumeric",
        "alcoholUsed.error.wine.decimalPlaces"
      )
        .verifying(minimumValue(Constants.volumeMinimumValueIncZero, "alcoholUsed.error.wine.minimumRequired"))
        .verifying(maximumValue(Constants.volumeMaximumValue, "alcoholUsed.error.wine.maximumRequired")),
      "madeWine"     -> bigDecimal(
        2,
        "alcoholUsed.error.madeWine.required",
        "alcoholUsed.error.madeWine.nonNumeric",
        "alcoholUsed.error.madeWine.decimalPlaces"
      )
        .verifying(minimumValue(Constants.volumeMinimumValueIncZero, "alcoholUsed.error.madeWine.minimumRequired"))
        .verifying(maximumValue(Constants.volumeMaximumValue, "alcoholUsed.error.madeWine.maximumRequired")),
      "ciderOrPerry" -> bigDecimal(
        2,
        "alcoholUsed.error.ciderOrPerry.required",
        "alcoholUsed.error.ciderOrPerry.nonNumeric",
        "alcoholUsed.error.ciderOrPerry.decimalPlaces"
      )
        .verifying(minimumValue(Constants.volumeMinimumValueIncZero, "alcoholUsed.error.ciderOrPerry.minimumRequired"))
        .verifying(maximumValue(Constants.volumeMaximumValue, "alcoholUsed.error.ciderOrPerry.maximumRequired"))
    )(AlcoholUsed.apply)(AlcoholUsed.unapply)
  )
}
