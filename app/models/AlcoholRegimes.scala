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

import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import play.api.libs.json.JsPath

case class AlcoholRegimes(regimes: Set[AlcoholRegime]) {
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

  private def hasRegime(regime: AlcoholRegime) = regimes.contains(regime)

  def nextRegime(current: Option[AlcoholRegime]): Option[AlcoholRegime] = current match {
    case None                                        => Seq(Beer, Cider, Wine, Spirits, OtherFermentedProduct).find(hasRegime)
    case Some(Beer)                                  => Seq(Cider, Wine, Spirits, OtherFermentedProduct).find(hasRegime)
    case Some(Cider)                                 => Seq(Wine, Spirits).find(hasRegime).orElse(Some(OtherFermentedProduct))
    case Some(Wine)                                  => Seq(Spirits).find(hasRegime).orElse(Some(OtherFermentedProduct))
    case Some(Spirits) if hasOtherFermentedProduct() => Some(OtherFermentedProduct)
    case _                                           => None
  }
}

object AlcoholRegimes {
  def fromUserAnswers(userAnswers: UserAnswers): Option[AlcoholRegimes] =
    userAnswers.get[Set[AlcoholRegime]](JsPath \ "alcoholRegime").map(AlcoholRegimes(_))
}
