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

import controllers.routes
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class DeclareDutySuspendedDeliveriesNavigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case DeclareDutySuspendedDeliveriesQuestionPage  =>
      _ => routes.DutySuspendedDeliveriesGuidanceController.onPageLoad
    case DeclareDutySuspendedDeliveriesOutsideUkPage =>
      _ => routes.DutySuspendedDeliveriesController.onPageLoad(NormalMode)
    case DutySuspendedDeliveriesPage                 =>
      _ => routes.DeclareDutySuspendedReceivedController.onPageLoad(NormalMode)
    case DeclareDutySuspendedReceivedPage            =>
      _ => routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad
    case _                                           =>
      _ => routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Call = { case _ =>
    _ => routes.CheckYourAnswersController.onPageLoad
  }

  private val checkDutySuspendedDeliveriesRouteMap: Page => UserAnswers => Call = { case _ =>
    _ => routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode                       =>
      normalRoutes(page)(userAnswers)
    case CheckMode                        =>
      checkRouteMap(page)(userAnswers)
    case CheckDutySuspendedDeliveriesMode =>
      checkDutySuspendedDeliveriesRouteMap(page)(userAnswers)
  }
}
