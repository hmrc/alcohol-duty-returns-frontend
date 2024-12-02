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

package models.dutySuspended

import models.AlcoholRegime

trait DutySuspendedRegimeKeyProvider {
  def totalVolumeKey(regime: AlcoholRegime): String

  def pureAlcoholKey(regime: AlcoholRegime): String
}

object DutySuspendedRegimeSpecificKey extends DutySuspendedRegimeKeyProvider {
  override def totalVolumeKey(regime: AlcoholRegime): String = regime match {
    case AlcoholRegime.Beer                  => "totalBeer"
    case AlcoholRegime.Cider                 => "totalCider"
    case AlcoholRegime.OtherFermentedProduct => "totalOtherFermented"
    case AlcoholRegime.Wine                  => "totalWine"
    case AlcoholRegime.Spirits               => "totalSpirits"
  }

  override def pureAlcoholKey(regime: AlcoholRegime): String = regime match {
    case AlcoholRegime.Beer                  => "pureAlcoholInBeer"
    case AlcoholRegime.Cider                 => "pureAlcoholInCider"
    case AlcoholRegime.OtherFermentedProduct => "pureAlcoholInOtherFermented"
    case AlcoholRegime.Wine                  => "pureAlcoholInWine"
    case AlcoholRegime.Spirits               => "pureAlcoholInSpirits"
  }
}
