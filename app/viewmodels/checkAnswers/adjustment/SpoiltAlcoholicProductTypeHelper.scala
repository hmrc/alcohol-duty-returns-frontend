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
import models.RateType.Core
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholRegimes, AlcoholType, RangeDetailsByRegime, RateBand}
import play.api.Logging
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import viewmodels.AlcoholRegimesViewOrder.regimesInViewOrder

import javax.inject.Inject

class SpoiltAlcoholicProductTypeHelper @Inject() (
  appConfig: FrontendAppConfig
) extends Logging {
  private val rangeMinABV = BigDecimal(0)
  private val rangeMaxABV = BigDecimal(100)

  def createRateBandFromRegime(regime: AlcoholRegime)(implicit messages: Messages): RateBand = {
    val rate = appConfig.spoiltRate

    RateBand(
      appConfig.getTaxTypeCodeByRegime(regime),
      messages(s"alcoholType.$regime"),
      Core,
      Some(rate),
      Set(
        RangeDetailsByRegime(
          regime,
          NonEmptySeq.one(
            ABVRange(AlcoholType.fromAlcoholRegime(regime), AlcoholByVolume(rangeMinABV), AlcoholByVolume(rangeMaxABV))
          )
        )
      )
    )
  }

  def radioOptions(regimes: AlcoholRegimes)(implicit messages: Messages): Seq[RadioItem] =
    regimesInViewOrder(regimes).map { regime =>
      val regimeName = regime.entryName

      RadioItem(
        content = Text(messages(s"alcoholType.$regimeName").capitalize),
        value = Some(regimeName),
        id = Some(regimeName)
      )
    }
}
