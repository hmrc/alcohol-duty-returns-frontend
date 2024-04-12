/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class DeclareDutySuspendedDeliveriesNavigator @Inject() () extends BaseNavigator {

  override val normalRoutes: Page => UserAnswers => Call = {
    case pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage  =>
      declareDutySuspendedDeliveriesQuestionPageRoute
    case pages.dutySuspended.DutySuspendedBeerPage                       =>
      _ => controllers.dutySuspended.routes.DutySuspendedCiderController.onPageLoad(NormalMode)
    case pages.dutySuspended.DutySuspendedCiderPage                      =>
      _ => controllers.dutySuspended.routes.DutySuspendedWineController.onPageLoad(NormalMode)
    case pages.dutySuspended.DutySuspendedWinePage =>
      _ => controllers.dutySuspended.routes.DutySuspendedOtherFermentedController.onPageLoad(NormalMode)
    case pages.dutySuspended.DeclareDutySuspendedDeliveriesOutsideUkPage =>
      _ => controllers.dutySuspended.routes.DutySuspendedDeliveriesController.onPageLoad(NormalMode)
    case pages.dutySuspended.DutySuspendedDeliveriesPage                 =>
      _ => controllers.dutySuspended.routes.DeclareDutySuspendedReceivedController.onPageLoad(NormalMode)
    case pages.dutySuspended.DutySuspendedOtherFermentedPage            =>
      _ => controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad
    case _                                                               =>
      _ => routes.IndexController.onPageLoad
  }

  override val checkRouteMap: Page => UserAnswers => Call = { case _ =>
    _ => controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad
  }

  private def declareDutySuspendedDeliveriesQuestionPageRoute(answers: UserAnswers): Call =
    answers.get(pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage) match {
      case Some(true)  => controllers.dutySuspended.routes.DutySuspendedDeliveriesGuidanceController.onPageLoad()
      case Some(false) => routes.IndexController.onPageLoad
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }

}
