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

package navigation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import models.RateType.{Core, DraughtAndSmallProducerRelief, DraughtRelief, SmallProducerRelief}
import pages._
import models._
import pages.declareDuty._
import viewmodels.declareDuty.MissingSPRRateBandHelper

@Singleton
class ReturnsNavigator @Inject() (
  missingSPRRateBandHelper: MissingSPRRateBandHelper
) {

  private val normalRoutes: Page => UserAnswers => Call = {
    case DeclareAlcoholDutyQuestionPage => userAnswers => declareAlcoholQuestionRoute(userAnswers, NormalMode)
    case _                              => _ => routes.TaskListController.onPageLoad
  }

  private val normalRoutesReturnJourney: Page => UserAnswers => AlcoholRegime => Boolean => Option[Int] => Call = {
    case WhatDoYouNeedToDeclarePage =>
      ua =>
        regime =>
          _ =>
            _ =>
              ua.getByKey(WhatDoYouNeedToDeclarePage, regime) match {
                case Some(rateBands)
                    if rateBands
                      .map(_.rateType)
                      .intersect(Set(Core, DraughtRelief))
                      .nonEmpty =>
                  controllers.declareDuty.routes.HowMuchDoYouNeedToDeclareController.onPageLoad(NormalMode, regime)
                case Some(_) =>
                  controllers.declareDuty.routes.DoYouHaveMultipleSPRDutyRatesController.onPageLoad(NormalMode, regime)
                case _       =>
                  routes.TaskListController.onPageLoad
              }

    case HowMuchDoYouNeedToDeclarePage =>
      ua =>
        regime =>
          _ =>
            _ =>
              ua.getByKey(WhatDoYouNeedToDeclarePage, regime) match {
                case Some(rateBands)
                    if rateBands
                      .map(_.rateType)
                      .intersect(Set(SmallProducerRelief, DraughtAndSmallProducerRelief))
                      .nonEmpty =>
                  controllers.declareDuty.routes.DoYouHaveMultipleSPRDutyRatesController.onPageLoad(NormalMode, regime)
                case Some(_) =>
                  controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)
                case _       =>
                  routes.TaskListController.onPageLoad
              }

    case DoYouHaveMultipleSPRDutyRatesPage =>
      ua =>
        regime =>
          _ =>
            _ =>
              (ua.getByKey(DoYouHaveMultipleSPRDutyRatesPage, regime), ua.getByKey(MultipleSPRListPage, regime)) match {
                case (Some(true), Some(list)) if list.nonEmpty =>
                  controllers.declareDuty.routes.MultipleSPRListController.onPageLoad(regime)
                case (Some(true), _)                           =>
                  controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController.onPageLoad(NormalMode, regime)
                case (Some(false), _)                          =>
                  controllers.declareDuty.routes.TellUsAboutSingleSPRRateController.onPageLoad(NormalMode, regime)
                case _                                         =>
                  routes.TaskListController.onPageLoad
              }

    case TellUsAboutMultipleSPRRatePage =>
      _ =>
        regime =>
          hasValueChanged =>
            index =>
              if (hasValueChanged || index.isEmpty) {
                controllers.declareDuty.routes.CheckYourAnswersSPRController.onPageLoad(regime, index)
              } else {
                controllers.declareDuty.routes.MultipleSPRListController.onPageLoad(regime)
              }

    case TellUsAboutSingleSPRRatePage =>
      _ => regime => _ => _ => controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)

    case DoYouWantToAddMultipleSPRToListPage =>
      ua =>
        regime =>
          _ =>
            _ =>
              ua.getByKey(DoYouWantToAddMultipleSPRToListPage, regime) match {
                case Some(true)  =>
                  controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController.onPageLoad(CheckMode, regime)
                case Some(false) =>
                  missingSPRRateBandHelper.findMissingSPRRateBands(regime, ua) match {
                    case Some(missingRateBands) if missingRateBands.nonEmpty =>
                      controllers.declareDuty.routes.MultipleSPRMissingDetailsController.onPageLoad(regime)
                    case Some(_)                                             =>
                      controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)
                    case _                                                   =>
                      routes.TaskListController.onPageLoad
                  }
                case _           => routes.TaskListController.onPageLoad
              }

    case MultipleSPRMissingDetailsPage =>
      ua =>
        regime =>
          _ =>
            _ =>
              ua.getByKey(MultipleSPRMissingDetailsPage, regime) match {
                case Some(true)  =>
                  controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController.onPageLoad(CheckMode, regime)
                case Some(false) =>
                  controllers.declareDuty.routes.MultipleSPRMissingDetailsConfirmationController.onPageLoad(regime)
                case _           => routes.TaskListController.onPageLoad
              }

    case MultipleSPRMissingDetailsConfirmationPage =>
      ua =>
        regime =>
          _ =>
            _ =>
              ua.getByKey(MultipleSPRMissingDetailsConfirmationPage, regime) match {
                case Some(true)  =>
                  controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)
                case Some(false) =>
                  controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController.onPageLoad(CheckMode, regime)
                case _           => routes.TaskListController.onPageLoad
              }

    case DeleteMultipleSPREntryPage =>
      ua =>
        regime =>
          _ =>
            _ =>
              ua.getByKey(MultipleSPRListPage, regime) match {
                case Some(list) if list.nonEmpty =>
                  controllers.declareDuty.routes.MultipleSPRListController.onPageLoad(regime)
                case _                           =>
                  controllers.declareDuty.routes.DoYouHaveMultipleSPRDutyRatesController.onPageLoad(NormalMode, regime)
              }

    case _ => _ => _ => _ => _ => routes.TaskListController.onPageLoad
  }

  private val checkRouteMapReturnJourney: Page => UserAnswers => AlcoholRegime => Boolean => Option[Int] => Call = {
    case WhatDoYouNeedToDeclarePage =>
      ua =>
        regime =>
          hasChanged =>
            index =>
              if (hasChanged) {
                normalRoutesReturnJourney(WhatDoYouNeedToDeclarePage)(ua)(regime)(hasChanged)(index)
              } else {
                controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)
              }

    case HowMuchDoYouNeedToDeclarePage =>
      _ => regime => _ => _ => controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)

    case DoYouHaveMultipleSPRDutyRatesPage =>
      ua =>
        regime =>
          hasChanged =>
            index =>
              if (hasChanged) {
                normalRoutesReturnJourney(DoYouHaveMultipleSPRDutyRatesPage)(ua)(regime)(hasChanged)(index)
              } else {
                controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)
              }

    case TellUsAboutSingleSPRRatePage =>
      _ => regime => _ => _ => controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime)

    case TellUsAboutMultipleSPRRatePage =>
      _ =>
        regime =>
          hasChanged =>
            index =>
              if (hasChanged) {
                controllers.declareDuty.routes.CheckYourAnswersSPRController.onPageLoad(regime, index)
              } else {
                controllers.declareDuty.routes.MultipleSPRListController.onPageLoad(regime)
              }
    case _                              =>
      _ => _ => _ => _ => routes.TaskListController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Boolean => Call = {
    case pages.declareDuty.DeclareAlcoholDutyQuestionPage =>
      userAnswers => _ => declareAlcoholQuestionRoute(userAnswers, CheckMode)
    case _                                                => _ => _ => routes.TaskListController.onPageLoad
  }

  def nextPageWithRegime(
    page: Page,
    mode: Mode,
    userAnswers: UserAnswers,
    regime: AlcoholRegime,
    hasAnswerChanged: Boolean = false,
    index: Option[Int] = None
  ): Call = mode match {
    case NormalMode =>
      normalRoutesReturnJourney(page)(userAnswers)(regime)(hasAnswerChanged)(index)
    case CheckMode  =>
      checkRouteMapReturnJourney(page)(userAnswers)(regime)(hasAnswerChanged)(index)
  }

  def nextPage(
    page: Page,
    mode: Mode,
    userAnswers: UserAnswers,
    hasAnswerChanged: Option[Boolean]
  ): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)(hasAnswerChanged.getOrElse(false))
  }

  private def declareAlcoholQuestionRoute(userAnswers: UserAnswers, mode: Mode): Call =
    userAnswers.get(DeclareAlcoholDutyQuestionPage) match {
      case Some(true) if userAnswers.regimes.regimes.size > 1 =>
        controllers.declareDuty.routes.AlcoholTypeController.onPageLoad(mode)
      case Some(_)                                            => routes.TaskListController.onPageLoad
      case _                                                  => routes.JourneyRecoveryController.onPageLoad()
    }
}
