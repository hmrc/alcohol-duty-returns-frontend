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
import pages.returns.{DeclareAlcoholDutyQuestionPage, DoYouHaveMultipleSPRDutyRatesPage, DoYouWantToAddMultipleSPRToListPage, HowMuchDoYouNeedToDeclarePage, TellUsAboutMultipleSPRRatePage, WhatDoYouNeedToDeclarePage}

@Singleton
class ReturnsNavigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case DeclareAlcoholDutyQuestionPage => _ => routes.TaskListController.onPageLoad
    case _                              => _ => routes.IndexController.onPageLoad
  }

  private val normalRoutesReturnJourney: Page => UserAnswers => AlcoholRegime => Call = {
    case WhatDoYouNeedToDeclarePage        =>
      ua =>
        regime =>
          ua.getByKey(WhatDoYouNeedToDeclarePage, regime) match {
            case Some(rateBands)
                if rateBands
                  .map(_.rateType)
                  .intersect(Set(Core, DraughtRelief))
                  .nonEmpty =>
              controllers.returns.routes.HowMuchDoYouNeedToDeclareController.onPageLoad(NormalMode, regime)
            case Some(_) =>
              controllers.returns.routes.DoYouHaveMultipleSPRDutyRatesController.onPageLoad(NormalMode, regime)
            case _       =>
              routes.IndexController.onPageLoad
          }
    case HowMuchDoYouNeedToDeclarePage     =>
      ua =>
        regime =>
          ua.getByKey(WhatDoYouNeedToDeclarePage, regime) match {
            case Some(rateBands)
                if rateBands
                  .map(_.rateType)
                  .intersect(Set(SmallProducerRelief, DraughtAndSmallProducerRelief))
                  .nonEmpty =>
              controllers.returns.routes.DoYouHaveMultipleSPRDutyRatesController.onPageLoad(NormalMode, regime)
            case Some(_) =>
              controllers.returns.routes.CheckYourAnswersController.onPageLoad(regime)
            case _       =>
              routes.IndexController.onPageLoad
          }
    case DoYouHaveMultipleSPRDutyRatesPage =>
      ua =>
        regime =>
          ua.getByKey(DoYouHaveMultipleSPRDutyRatesPage, regime) match {
            case Some(true)  =>
              controllers.returns.routes.TellUsAboutMultipleSPRRateController.onPageLoad(NormalMode, regime)
            case Some(false) =>
              controllers.returns.routes.TellUsAboutSingleSPRRateController.onPageLoad(NormalMode, regime)
            case _           =>
              routes.IndexController.onPageLoad
          }

    case TellUsAboutMultipleSPRRatePage =>
      _ => regime => controllers.returns.routes.CheckYourAnswersSPRController.onPageLoad(regime)

    case DoYouWantToAddMultipleSPRToListPage =>
      ua =>
        regime =>
          ua.getByKey(DoYouWantToAddMultipleSPRToListPage, regime) match {
            case Some(true)  =>
              controllers.returns.routes.TellUsAboutMultipleSPRRateController.onPageLoad(CheckMode, regime)
            case Some(false) => routes.TaskListController.onPageLoad
            case _           => routes.IndexController.onPageLoad
          }

    case _ => _ => _ => routes.IndexController.onPageLoad
  }

  private val checkRouteMapReturnJourney: Page => UserAnswers => AlcoholRegime => Boolean => Option[Int] => Call = {
    case TellUsAboutMultipleSPRRatePage =>
      _ =>
        regime =>
          hasChanged =>
            index =>
              if (hasChanged)
                controllers.returns.routes.CheckYourAnswersSPRController.onPageLoad(regime, index)
              else
                controllers.returns.routes.MultipleSPRListController.onPageLoad(regime)
    case _                              =>
      _ => _ => _ => _ => routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Boolean => Call = {
    case pages.returns.DeclareAlcoholDutyQuestionPage => _ => _ => routes.TaskListController.onPageLoad
    case _                                            => _ => _ => routes.IndexController.onPageLoad
  }

  def nextPageWithRegime(
    page: Page,
    mode: Mode,
    userAnswers: UserAnswers,
    regime: AlcoholRegime,
    hasAnswerChanged: Boolean = true,
    index: Option[Int] = None
  ): Call = mode match {
    case NormalMode =>
      normalRoutesReturnJourney(page)(userAnswers)(regime)
    case CheckMode  =>
      checkRouteMapReturnJourney(page)(userAnswers)(regime)(hasAnswerChanged)(index)
  }

  def nextPage(
    page: Page,
    mode: Mode,
    userAnswers: UserAnswers,
    hasAnswerChanged: Boolean = true
  ): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)(hasAnswerChanged)
  }
}
