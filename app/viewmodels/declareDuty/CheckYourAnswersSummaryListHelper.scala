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

package viewmodels.declareDuty

import models.{AlcoholRegime, ErrorModel, RateBand, UserAnswers}
import pages.declareDuty._
import play.api.http.Status.BAD_REQUEST
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

import javax.inject.Inject

case class ReturnSummaryList(
  whatDoYouNeedToDeclareSummary: SummaryList,
  howMuchDoYouNeedToDeclareSummary: Option[SummaryList],
  smallProducerReliefSummary: Option[SummaryList]
)

class CheckYourAnswersSummaryListHelper @Inject() (
  howMuchDoYouNeedToDeclareSummary: HowMuchDoYouNeedToDeclareSummary,
  smallProducerReliefSummary: SmallProducerReliefSummary,
  whatDoYouNeedToDeclareSummary: WhatDoYouNeedToDeclareSummary
) {

  def createSummaryList(regime: AlcoholRegime, userAnswers: UserAnswers)(implicit
    messages: Messages
  ): Either[ErrorModel, ReturnSummaryList] =
    checkDeclarationDetailsArePresent(regime, userAnswers).map { declaredRateBands =>
      ReturnSummaryList(
        whatDoYouNeedToDeclareSummary = whatDoYouNeedToDeclareSummary.summaryList(regime, declaredRateBands),
        howMuchDoYouNeedToDeclareSummary =
          howMuchDoYouNeedToDeclareSummary.summaryList(regime, declaredRateBands, userAnswers),
        smallProducerReliefSummary = smallProducerReliefSummary.summaryList(regime, userAnswers)
      )
    }

  def checkDeclarationDetailsArePresent(
    regime: AlcoholRegime,
    userAnswers: UserAnswers
  ): Either[ErrorModel, Set[RateBand]] =
    for {
      declaredRateBands <- getDeclaredRateBands(regime, userAnswers)
      _                 <- checkNonSPRDetails(regime, declaredRateBands, userAnswers)
      _                 <- checkSPRDetails(regime, declaredRateBands, userAnswers)
    } yield declaredRateBands

  private def getDeclaredRateBands(regime: AlcoholRegime, userAnswers: UserAnswers): Either[ErrorModel, Set[RateBand]] =
    userAnswers
      .getByKey(WhatDoYouNeedToDeclarePage, regime)
      .toRight(ErrorModel(BAD_REQUEST, "No declared rate bands in user answers"))

  private def checkNonSPRDetails(
    regime: AlcoholRegime,
    declaredRateBands: Set[RateBand],
    userAnswers: UserAnswers
  ): Either[ErrorModel, Unit] = {
    val declaredNonSPRTaxTypes  = declaredRateBands.filter(!_.rateType.isSPR).map(_.taxTypeCode)
    val nonSPRTaxTypesInAnswers =
      userAnswers.getByKey(HowMuchDoYouNeedToDeclarePage, regime).map(_.map(_.taxType).toSet).getOrElse(Set.empty)
    if (declaredNonSPRTaxTypes equals nonSPRTaxTypesInAnswers) { Right((): Unit) }
    else { Left(ErrorModel(BAD_REQUEST, "Missing declaration details for non-SPR rate bands")) }
  }

  private def checkSPRDetails(
    regime: AlcoholRegime,
    declaredRateBands: Set[RateBand],
    userAnswers: UserAnswers
  ): Either[ErrorModel, Unit] = {
    val declaredSPRTaxTypes = declaredRateBands.filter(_.rateType.isSPR).map(_.taxTypeCode)
    userAnswers.getByKey(DoYouHaveMultipleSPRDutyRatesPage, regime) match {
      case Some(true)  =>
        val sprTaxTypesInAnswers =
          userAnswers.getByKey(MultipleSPRListPage, regime).map(_.map(_.taxType).toSet).getOrElse(Set.empty)
        if (sprTaxTypesInAnswers.nonEmpty) { Right((): Unit) }
        else { Left(ErrorModel(BAD_REQUEST, "Multiple SPR list is empty")) }
      case Some(false) =>
        val sprTaxTypesInAnswers =
          userAnswers.getByKey(TellUsAboutSingleSPRRatePage, regime).map(_.map(_.taxType).toSet).getOrElse(Set.empty)
        if (declaredSPRTaxTypes equals sprTaxTypesInAnswers) { Right((): Unit) }
        else { Left(ErrorModel(BAD_REQUEST, "Missing declaration details for single SPR")) }
      case None        =>
        if (declaredSPRTaxTypes.isEmpty) { Right((): Unit) }
        else { Left(ErrorModel(BAD_REQUEST, "SPR rate bands declared but single/multiple SPR question not answered")) }
    }
  }
}
