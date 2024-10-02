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

package common

import models.AlcoholRegime._
import models.{AlcoholRegime, RateBand, UserAnswers}
import models.returns.{AlcoholDuty, DutyByTaxType, VolumeAndRateByTaxType}
import pages.adjustment.{AdjustmentTotalPage, DeclareAdjustmentQuestionPage}
import pages.returns.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage, DoYouHaveMultipleSPRDutyRatesPage, MultipleSPRListPage, TellUsAboutMultipleSPRRatePage, TellUsAboutSingleSPRRatePage, WhatDoYouNeedToDeclarePage}

import scala.collection.immutable.SortedMap

trait TestPages extends TestData {
  def declareAlcoholDutyQuestionPage(userAnswers: UserAnswers, declared: Boolean): UserAnswers =
    userAnswers.set(DeclareAlcoholDutyQuestionPage, declared).get

  def specifyAlcoholDuties(userAnswers: UserAnswers, duties: Map[AlcoholRegime, AlcoholDuty]): UserAnswers =
    userAnswers.set(AlcoholDutyPage, duties).get

  val beerDuty = AlcoholDuty(
    dutiesByTaxType = Seq(
      DutyByTaxType(
        taxType = "311",
        totalLitres = BigDecimal(1000),
        pureAlcohol = BigDecimal(20),
        dutyRate = BigDecimal(9.27),
        dutyDue = BigDecimal(185.4)
      ),
      DutyByTaxType(
        taxType = "351",
        totalLitres = BigDecimal(3500),
        pureAlcohol = BigDecimal(105),
        dutyRate = BigDecimal(8.42),
        dutyDue = BigDecimal(884.1)
      ),
      DutyByTaxType(
        taxType = "361",
        totalLitres = BigDecimal(14),
        pureAlcohol = BigDecimal(13),
        dutyRate = BigDecimal(12),
        dutyDue = BigDecimal(156)
      )
    ),
    totalDuty = BigDecimal(1225.5)
  )

  val ciderDuty = AlcoholDuty(
    dutiesByTaxType = Seq(
      DutyByTaxType(
        taxType = "312",
        totalLitres = BigDecimal(2000),
        pureAlcohol = BigDecimal(30),
        dutyRate = BigDecimal(9.27),
        dutyDue = BigDecimal(278.1)
      )
    ),
    totalDuty = BigDecimal(278.1)
  )

  val wineDuty = AlcoholDuty(
    dutiesByTaxType = Seq(
      DutyByTaxType(
        taxType = "333",
        totalLitres = BigDecimal(10000),
        pureAlcohol = BigDecimal(1400),
        dutyRate = BigDecimal(28.5),
        dutyDue = BigDecimal(39900)
      )
    ),
    totalDuty = BigDecimal(39900)
  )

  val spiritsDuty = AlcoholDuty(
    dutiesByTaxType = Seq(
      DutyByTaxType(
        taxType = "345",
        totalLitres = BigDecimal(10000),
        pureAlcohol = BigDecimal(4000),
        dutyRate = BigDecimal(31.64),
        dutyDue = BigDecimal(126560)
      )
    ),
    totalDuty = BigDecimal(12650)
  )

  val otherFermentedProductDuty = AlcoholDuty(
    dutiesByTaxType = Seq(
      DutyByTaxType(
        taxType = "324",
        totalLitres = BigDecimal(5000),
        pureAlcohol = BigDecimal(350),
        dutyRate = BigDecimal(24.77),
        dutyDue = BigDecimal(8669.5)
      ),
      DutyByTaxType(
        taxType = "364",
        totalLitres = BigDecimal(100),
        pureAlcohol = BigDecimal(2),
        dutyRate = BigDecimal(0.25),
        dutyDue = BigDecimal(0.5)
      )
    ),
    totalDuty = BigDecimal(8670)
  )

  val allAlcoholDuties: Map[AlcoholRegime, AlcoholDuty] = Map(
    Beer                  -> beerDuty,
    Cider                 -> ciderDuty,
    Wine                  -> wineDuty,
    Spirits               -> spiritsDuty,
    OtherFermentedProduct -> otherFermentedProductDuty
  )

  // Used for testing sorting
  val allAlcoholDutiesUnsorted: Map[AlcoholRegime, AlcoholDuty] = SortedMap[AlcoholRegime, AlcoholDuty]() {
    (x: AlcoholRegime, y: AlcoholRegime) =>
      val order: Map[AlcoholRegime, Int] =
        Map(Wine -> 1, Beer -> 2, Spirits -> 3, OtherFermentedProduct -> 4, Cider -> 5)

      order(x) - order(y)
  } ++ allAlcoholDuties

  def alcoholDutyPage(userAnswers: UserAnswers, duties: Map[AlcoholRegime, AlcoholDuty]): UserAnswers =
    userAnswers.set(AlcoholDutyPage, duties).get

  def specifyAllAlcoholDuties(userAnswers: UserAnswers): UserAnswers =
    userAnswers.set(AlcoholDutyPage, allAlcoholDuties).get

  def specifyAllAlcoholDutiesUnsorted(userAnswers: UserAnswers): UserAnswers =
    userAnswers.set(AlcoholDutyPage, allAlcoholDutiesUnsorted).get

  def declareAdjustmentQuestionPage(userAnswers: UserAnswers, declared: Boolean): UserAnswers =
    userAnswers.set(DeclareAdjustmentQuestionPage, declared).get

  def declareAdjustmentTotalPage(userAnswers: UserAnswers, total: BigDecimal): UserAnswers =
    userAnswers.set(AdjustmentTotalPage, total).get

  def whatDoYouNeedToDeclarePage(
    userAnswers: UserAnswers,
    regime: AlcoholRegime,
    rateBands: Set[RateBand]
  ): UserAnswers =
    userAnswers.setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands).get

  def doYouHaveMultipleSPRDutyRatesPage(
    userAnswers: UserAnswers,
    regime: AlcoholRegime,
    hasMultiple: Boolean
  ): UserAnswers =
    userAnswers.setByKey(DoYouHaveMultipleSPRDutyRatesPage, regime, hasMultiple).get

  def tellUsAboutSingleSPRRatePage(
    userAnswers: UserAnswers,
    regime: AlcoholRegime,
    dutyByTaxType: Seq[VolumeAndRateByTaxType]
  ): UserAnswers =
    userAnswers.setByKey(TellUsAboutSingleSPRRatePage, regime, dutyByTaxType).get

  def multipleSPRListPage(
    userAnswers: UserAnswers,
    regime: AlcoholRegime,
    dutyByTaxType: Seq[VolumeAndRateByTaxType]
  ): UserAnswers =
    userAnswers.setByKey(MultipleSPRListPage, regime, dutyByTaxType).get

  def tellUsAboutMultipleSPRRatePage(
    userAnswers: UserAnswers,
    regime: AlcoholRegime,
    dutyByTaxType: VolumeAndRateByTaxType
  ): UserAnswers =
    userAnswers.setByKey(TellUsAboutMultipleSPRRatePage, regime, dutyByTaxType).get

  val allVolumeAndRateByTaxTypeUnsorted: Seq[VolumeAndRateByTaxType] =
    allVolumeAndRateByTaxType.sorted((x: VolumeAndRateByTaxType, y: VolumeAndRateByTaxType) =>
      y.taxType.toInt - x.taxType.toInt
    )

  def specifyTellUsAboutAllSingleSPRRate(userAnswers: UserAnswers, regime: AlcoholRegime): UserAnswers =
    tellUsAboutSingleSPRRatePage(userAnswers, regime, allVolumeAndRateByTaxType)

  def specifyAllMultipleSPRList(userAnswers: UserAnswers, regime: AlcoholRegime): UserAnswers =
    multipleSPRListPage(userAnswers, regime, allVolumeAndRateByTaxType)

  def specifyAllMultipleSPRListUnsorted(userAnswers: UserAnswers, regime: AlcoholRegime): UserAnswers =
    multipleSPRListPage(userAnswers, regime, allVolumeAndRateByTaxTypeUnsorted)
}
