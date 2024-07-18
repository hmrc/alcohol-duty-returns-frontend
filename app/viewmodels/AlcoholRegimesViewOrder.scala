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

package viewmodels

import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.{AlcoholRegime, AlcoholRegimes}
import utils.ListHelpers._

object AlcoholRegimesViewOrder {
  private[viewmodels] val viewOrder: Seq[AlcoholRegime] = Seq(Beer, Cider, Wine, Spirits, OtherFermentedProduct)

  def regimesInViewOrder(regimes: AlcoholRegimes): Seq[AlcoholRegime] = viewOrder.filter(regimes.regimes.contains)

  def nextViewRegime(regimes: AlcoholRegimes, current: Option[AlcoholRegime]): Option[AlcoholRegime] =
    current.fold(regimesInViewOrder(regimes).headOption)(regimesInViewOrder(regimes).toList.nextItem(_))
}