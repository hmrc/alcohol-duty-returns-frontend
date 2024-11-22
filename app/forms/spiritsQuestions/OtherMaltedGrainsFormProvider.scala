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
import models.spiritsQuestions.OtherMaltedGrains

class OtherMaltedGrainsFormProvider @Inject() extends Mappings {
  def apply(): Form[OtherMaltedGrains] = Form(
    mapping(
      "otherMaltedGrainsTypes"    -> text("otherMaltedGrains.error.otherMaltedGrainsTypes.required")
        .verifying(
          maxLength(Constants.otherMaltedGrainsTypeMaxLength, "otherMaltedGrains.error.otherMaltedGrainsTypes.length")
        ),
      "otherMaltedGrainsQuantity" -> bigDecimal(
        Constants.maximumDecimalPlaces,
        "otherMaltedGrains.error.otherMaltedGrainsQuantity.required",
        "otherMaltedGrains.error.otherMaltedGrainsQuantity.nonNumeric",
        "otherMaltedGrains.error.otherMaltedGrainsQuantity.decimalPlaces"
      ).verifying(
        minimumValue(
          Constants.quantityMinimumValue,
          "otherMaltedGrains.error.otherMaltedGrainsQuantity.minimumRequired"
        )
      ).verifying(
        maximumValue(
          Constants.quantityMaximumValue,
          "otherMaltedGrains.error.otherMaltedGrainsQuantity.maximumRequired"
        )
      )
    )(OtherMaltedGrains.apply)(OtherMaltedGrains.unapply)
  )
}