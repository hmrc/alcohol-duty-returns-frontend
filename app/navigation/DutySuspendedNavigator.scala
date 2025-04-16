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
import play.api.Logging
import play.api.mvc.Call
import viewmodels.AlcoholRegimesViewOrder

import javax.inject.{Inject, Singleton}

@Singleton
class DutySuspendedNavigator @Inject() () extends Logging {

  private val normalRoutes: Page => UserAnswers => Call = {
    case DeclareDutySuspenseQuestionPage =>
      userAnswers => declareDutySuspenseQuestionPageRoute(userAnswers, NormalMode)
    case DutySuspendedAlcoholTypePage    =>
      userAnswers =>
        val firstRegime = userAnswers.get(DutySuspendedAlcoholTypePage).flatMap { dutySuspendedRegimes =>
          AlcoholRegimesViewOrder.nextViewRegime(regimes = AlcoholRegimes(dutySuspendedRegimes), current = None)
        }
        firstRegime match {
          case Some(regime) =>
            controllers.dutySuspendedNew.routes.DutySuspendedQuantitiesController.onPageLoad(NormalMode, regime)
          case None         => routes.JourneyRecoveryController.onPageLoad()
        }
    case _                               =>
      _ => routes.TaskListController.onPageLoad
  }

  private val normalRoutesWithRegime: Page => UserAnswers => AlcoholRegime => Call = {
    case DutySuspendedQuantitiesPage =>
      userAnswers =>
        regime =>
          userAnswers.getByKey(DutySuspendedQuantitiesPage, regime) match {
            case Some(_) =>
              // TODO: Go to display calculation page for current regime
              logger.info("Go to display calculation page")
              routes.JourneyRecoveryController.onPageLoad()
            case None    => routes.JourneyRecoveryController.onPageLoad()
          }
      // TODO: Implement for new pages that depend on the regime
    case _                           => _ => _ => routes.TaskListController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Boolean => Call = {
    case DeclareDutySuspenseQuestionPage =>
      userAnswers => _ => declareDutySuspenseQuestionPageRoute(userAnswers, CheckMode)
    case DutySuspendedAlcoholTypePage    =>
      userAnswers =>
        hasRegimesToAdd =>
          if (hasRegimesToAdd) {
            normalRoutes(DutySuspendedAlcoholTypePage)(userAnswers)
          } else {
            // TODO: Nothing new to declare, go to check your answers
            routes.JourneyRecoveryController.onPageLoad()
          }
    case _                               =>
      _ => _ => routes.TaskListController.onPageLoad
  }

  private val checkRouteMapWithRegime: Page => UserAnswers => AlcoholRegime => Call = {
    case DutySuspendedQuantitiesPage =>
      userAnswers =>
        regime =>
          userAnswers.getByKey(DutySuspendedQuantitiesPage, regime) match {
            case Some(_) =>
              // TODO: Go to check your answers page
              routes.JourneyRecoveryController.onPageLoad()
            case None    => routes.JourneyRecoveryController.onPageLoad()
          }
      // TODO: Implement for new pages that depend on the regime (always go to Check Your Answers)
    case _                           => _ => _ => routes.TaskListController.onPageLoad
  }

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
    hasRegimesToAdd: Option[Boolean]
  ): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)(hasRegimesToAdd.getOrElse(false))
  }

  private def declareDutySuspenseQuestionPageRoute(userAnswers: UserAnswers, mode: Mode): Call =
    userAnswers.get(DeclareDutySuspenseQuestionPage) match {
      case Some(true) if userAnswers.regimes.regimes.size > 1 =>
        controllers.dutySuspendedNew.routes.DutySuspendedAlcoholTypeController.onPageLoad(mode)
      case Some(true)                                         =>
        controllers.dutySuspendedNew.routes.DutySuspendedQuantitiesController
          .onPageLoad(mode, userAnswers.regimes.regimes.head)
        routes.JourneyRecoveryController.onPageLoad()
      case Some(false)                                        => routes.TaskListController.onPageLoad
      case _                                                  => routes.JourneyRecoveryController.onPageLoad()
    }
}
