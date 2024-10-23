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
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, RangeDetailsByRegime, RateBand}
import play.api.i18n.Messages

import javax.inject.Inject

class AlcoholicProductTypeHelper @Inject() (
  appConfig: FrontendAppConfig
) {
  def createRateBandFromRegime(regime: AlcoholRegime)(implicit messages: Messages): RateBand = {

    val (taxTypeCode, alcoholType) = regime match {
      case Beer                  => (appConfig.spoiltBeerTaxTypeCode, AlcoholType.Beer)
      case Cider                 => (appConfig.spoiltCiderTaxTypeCode, AlcoholType.Cider)
      case Wine                  => (appConfig.spoiltWineTaxTypeCode, AlcoholType.Wine)
      case Spirits               => (appConfig.spoiltSpiritsTaxTypeCode, AlcoholType.Spirits)
      case OtherFermentedProduct =>
        (appConfig.spoiltOtherFermentedProductsTaxTypeCode, AlcoholType.OtherFermentedProduct)
      //      case _                     => Need this?
      //        logger.warn("Couldn't match regime value selected on Alcoholic Product Type Screen")
      //        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

    val rate: BigDecimal = appConfig.spoiltRate

    RateBand(
      taxTypeCode,
      messages(s"alcoholType.$regime"),
      Core,
      Some(rate),
      Set(
        RangeDetailsByRegime(regime, NonEmptySeq.one(ABVRange(alcoholType, AlcoholByVolume(0), AlcoholByVolume(100))))
      )
    )
    //confirm rateType, and other rateband details Rate Type AlcoholByVolume
  }
}
