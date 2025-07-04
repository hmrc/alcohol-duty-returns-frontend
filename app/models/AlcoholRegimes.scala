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
import play.api.libs.json._

case class AlcoholRegimes(regimes: Set[AlcoholRegime]) {
  val hasBeer: Boolean                  = regimes.contains(Beer)
  val hasCider: Boolean                 = regimes.contains(Cider)
  val hasWine: Boolean                  = regimes.contains(Wine)
  val hasSpirits: Boolean               = regimes.contains(Spirits)
  val hasOtherFermentedProduct: Boolean = regimes.contains(OtherFermentedProduct)

  def hasRegime(regime: AlcoholRegime): Boolean = regimes.contains(regime)
}

object AlcoholRegimes {
  private[models] val reads: Reads[AlcoholRegimes] =
    (JsPath \ "regimes")
      .read[Set[AlcoholRegime]]
      .map(regimes =>
        if (regimes.nonEmpty) {
          AlcoholRegimes(regimes)
        } else {
          throw new IllegalArgumentException("Expecting at least one regime to be approved")
        }
      )

  private[models] val writes: OWrites[AlcoholRegimes] = Json.writes[AlcoholRegimes]

  implicit val alcoholRegimesFormat: OFormat[AlcoholRegimes] = OFormat[AlcoholRegimes](reads, writes)
}
