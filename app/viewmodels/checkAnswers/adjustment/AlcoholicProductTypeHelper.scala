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

package viewmodels.checkAnswers.adjustment

import cats.data.NonEmptySeq
import config.FrontendAppConfig
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.RateType.Core
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholRegimes, AlcoholType, RangeDetailsByRegime, RateBand}
import play.api.Logging
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import viewmodels.AlcoholRegimesViewOrder.regimesInViewOrder

import javax.inject.Inject

class AlcoholicProductTypeHelper @Inject() (
  appConfig: FrontendAppConfig
) extends Logging {
  def createRateBandFromRegime(regime: AlcoholRegime)(implicit messages: Messages): RateBand = {

    val alcoholType = regime match {
      case Beer                  => AlcoholType.Beer
      case Cider                 => AlcoholType.Cider
      case Wine                  => AlcoholType.Wine
      case Spirits               => AlcoholType.Spirits
      case OtherFermentedProduct => AlcoholType.OtherFermentedProduct
    }

    val rate: BigDecimal = appConfig.spoiltRate

    RateBand(
      appConfig.getTaxTypeCodeByRegime(regime),
      messages(s"alcoholType.$regime"),
      Core,
      Some(rate),
      Set(
        RangeDetailsByRegime(regime, NonEmptySeq.one(ABVRange(alcoholType, AlcoholByVolume(0), AlcoholByVolume(100))))
      )
    )
  }

  def radioOptions(regimes: AlcoholRegimes)(implicit messages: Messages): Seq[RadioItem] = {
    val orderedRegimes = regimesInViewOrder(regimes)
    orderedRegimes.zipWithIndex.map { case (value, _) =>
      RadioItem(
        content = Text(messages(s"alcoholType.$value")),
        value = Some(value.toString),
        id = Some(value.toString)
      )
    }
  }
}
