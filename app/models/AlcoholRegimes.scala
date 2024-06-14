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

package models

import cats.data.NonEmptySet
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import play.api.libs.json.{Json, OFormat}

case class AlcoholRegimes(regimes: NonEmptySet[AlcoholRegime]) {
  def hasBeer()                  = regimes.contains(Beer)
  def hasCider()                 = regimes.contains(Cider)
  def hasWine()                  = regimes.contains(Wine)
  def hasSpirits()               = regimes.contains(Spirits)
  def hasOtherFermentedProduct() = hasCider() || hasWine() || regimes.contains(OtherFermentedProduct)

  def authorisedForRegime(regime: AlcoholRegime) =
    if (regime == OtherFermentedProduct) {
      hasOtherFermentedProduct()
    } else {
      hasRegime(regime)
    }

  def hasRegime(regime: AlcoholRegime) = regimes.contains(regime)
}

object AlcoholRegimes {
  implicit val alcoholRegimesFormat: OFormat[AlcoholRegimes] = Json.format[AlcoholRegimes]
}