/*
 * Copyright 2025 HM Revenue & Customs
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

import controllers._
import models._
import pages._
import pages.dutySuspendedNew._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class DeclareDutySuspendedDeliveriesNewNavigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case DeclareDutySuspendedDeliveriesQuestionNewPage =>
      userAnswers => declareDutySuspendedDeliveriesQuestionPageRoute(userAnswers, NormalMode)
    case DutySuspendedAlcoholTypePage                  =>
      // TODO: Implement for alcohol type selection page
      _ => routes.TaskListController.onPageLoad
    case _                                             =>
      _ => routes.TaskListController.onPageLoad
  }

  private val normalRoutesWithRegime: Page => UserAnswers => AlcoholRegime => Call =
    // TODO: Implement for new pages that depend on the regime
    _ => _ => _ => routes.TaskListController.onPageLoad

  private val checkRouteMap: Page => UserAnswers => Boolean => Call = {
    case DeclareDutySuspendedDeliveriesQuestionNewPage =>
      userAnswers => _ => declareDutySuspendedDeliveriesQuestionPageRoute(userAnswers, CheckMode)
    case DutySuspendedAlcoholTypePage                  =>
      // TODO: Implement for alcohol type selection page
      _ => _ => routes.TaskListController.onPageLoad
    case _                                             =>
      _ => _ => routes.TaskListController.onPageLoad
  }

  private val checkRouteMapWithRegime: Page => UserAnswers => AlcoholRegime => Call =
    // TODO: Implement for new pages that depend on the regime (always go to Check Your Answers)
    _ => _ => _ => routes.TaskListController.onPageLoad

  def nextPageWithRegime(
    page: Page,
    mode: Mode,
    userAnswers: UserAnswers,
    regime: AlcoholRegime
  ): Call = mode match {
    case NormalMode =>
      normalRoutesWithRegime(page)(userAnswers)(regime)
    case CheckMode  =>
      checkRouteMapWithRegime(page)(userAnswers)(regime)
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

  private def declareDutySuspendedDeliveriesQuestionPageRoute(userAnswers: UserAnswers, mode: Mode): Call =
    userAnswers.get(DeclareDutySuspendedDeliveriesQuestionNewPage) match {
      case Some(true) if userAnswers.regimes.regimes.size > 1 =>
        // TODO: Go to new declare quantity page for the single regime (insert mode on page load)
        routes.TaskListController.onPageLoad
      case Some(true)                                         =>
        // TODO: Go to new alcohol type selection page (insert mode on page load)
        routes.TaskListController.onPageLoad
      case Some(false)                                        => routes.TaskListController.onPageLoad
      case _                                                  => routes.JourneyRecoveryController.onPageLoad()
    }
}
