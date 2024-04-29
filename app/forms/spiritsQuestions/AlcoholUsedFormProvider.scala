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
import models.spiritsQuestions.AlcoholUsed

class AlcoholUsedFormProvider @Inject() extends Mappings {
  import AlcoholUsedFormProvider._

  def apply(): Form[AlcoholUsed] = Form(
    mapping(
      "beer"         -> bigDecimal(
        quantityMaxDecimalPlaces,
        "alcoholUsed.error.beer.required",
        "alcoholUsed.error.beer.nonNumeric",
        "alcoholUsed.error.beer.twoDecimalPlaces"
      )
        .verifying(minimumValue(quantityMinValue, "alcoholUsed.error.beer.minimumRequired"))
        .verifying(maximumValue(quantityMaxValue, "alcoholUsed.error.beer.maximumRequired")),
      "wine"         -> bigDecimal(
        2,
        "alcoholUsed.error.wine.required",
        "alcoholUsed.error.wine.nonNumeric",
        "alcoholUsed.error.wine.twoDecimalPlaces"
      )
        .verifying(minimumValue(quantityMinValue, "alcoholUsed.error.wine.minimumRequired"))
        .verifying(maximumValue(quantityMaxValue, "alcoholUsed.error.wine.maximumRequired")),
      "madeWine"     -> bigDecimal(
        2,
        "alcoholUsed.error.madeWine.required",
        "alcoholUsed.error.madeWine.nonNumeric",
        "alcoholUsed.error.madeWine.twoDecimalPlaces"
      )
        .verifying(minimumValue(quantityMinValue, "alcoholUsed.error.madeWine.minimumRequired"))
        .verifying(maximumValue(quantityMaxValue, "alcoholUsed.error.madeWine.maximumRequired")),
      "ciderOrPerry" -> bigDecimal(
        2,
        "alcoholUsed.error.ciderOrPerry.required",
        "alcoholUsed.error.ciderOrPerry.nonNumeric",
        "alcoholUsed.error.ciderOrPerry.twoDecimalPlaces"
      )
        .verifying(minimumValue(quantityMinValue, "alcoholUsed.error.ciderOrPerry.minimumRequired"))
        .verifying(maximumValue(quantityMaxValue, "alcoholUsed.error.ciderOrPerry.maximumRequired"))
    )(AlcoholUsed.apply)(AlcoholUsed.unapply)
  )
}
object AlcoholUsedFormProvider {
  val quantityMaxDecimalPlaces = 2
  val quantityMinValue         = BigDecimal(0.00)
  val quantityMaxValue         = BigDecimal(999999999.99)
}
