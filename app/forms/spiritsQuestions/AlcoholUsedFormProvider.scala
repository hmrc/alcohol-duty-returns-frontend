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

  def apply(): Form[AlcoholUsed] = Form(
    mapping(
      "beer"         -> bigDecimal(
        2,
        "alcoholUsed.error.required",
        "alcoholUsed.error.nonNumeric",
        "alcoholUsed.error.twoDecimalPlaces",
        Seq("Beer")
      )
        .verifying(minimumValue(BigDecimal(0.01), "alcoholUsed.error.beer.minimumRequired"))
        .verifying(maximumValue(BigDecimal(999999999.99), "alcoholUsed.error.beer.maximumRequired")),
      "wine"         -> bigDecimal(
        2,
        "alcoholUsed.error.required",
        "alcoholUsed.error.nonNumeric",
        "alcoholUsed.error.twoDecimalPlaces",
        Seq("Wine")
      )
        .verifying(minimumValue(BigDecimal(0.01), "alcoholUsed.error.wine.minimumRequired"))
        .verifying(maximumValue(BigDecimal(999999999.99), "alcoholUsed.error.wine.maximumRequired")),
      "madeWine"     -> bigDecimal(
        2,
        "alcoholUsed.error.required",
        "alcoholUsed.error.nonNumeric",
        "alcoholUsed.error.twoDecimalPlaces",
        Seq("Made-wine")
      )
        .verifying(minimumValue(BigDecimal(0.01), "alcoholUsed.error.madeWine.minimumRequired"))
        .verifying(maximumValue(BigDecimal(999999999.99), "alcoholUsed.error.madeWine.maximumRequired")),
      "ciderOrPerry" -> bigDecimal(
        2,
        "alcoholUsed.error.required",
        "alcoholUsed.error.nonNumeric",
        "alcoholUsed.error.twoDecimalPlaces",
        Seq("Cider or perry")
      )
        .verifying(minimumValue(BigDecimal(0.01), "alcoholUsed.error.ciderOrPerry.minimumRequired"))
        .verifying(maximumValue(BigDecimal(999999999.99), "alcoholUsed.error.ciderOrPerry.maximumRequired"))
    )(AlcoholUsed.apply)(AlcoholUsed.unapply)
  )
}
