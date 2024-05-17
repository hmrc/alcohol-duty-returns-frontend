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
import pages._
import models._
import pages.returns.{DeclareAlcoholDutyQuestionPage, WhatDoYouNeedToDeclarePage}

@Singleton
class ReturnsNavigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case DeclareAlcoholDutyQuestionPage => _ => routes.TaskListController.onPageLoad
    case _                              => _ => routes.IndexController.onPageLoad
  }

  private val normalRoutesReturnJourney: Page => UserAnswers => AlcoholRegime => Call = {
    case WhatDoYouNeedToDeclarePage =>
      _ => regime => controllers.returns.routes.HowMuchDoYouNeedToDeclareController.onPageLoad(NormalMode, regime)
    case _                          => _ => _ => routes.IndexController.onPageLoad
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
    hasAnswerChanged: Boolean = true
  ): Call = mode match {
    case NormalMode =>
      normalRoutesReturnJourney(page)(userAnswers)(regime)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)(hasAnswerChanged)
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
