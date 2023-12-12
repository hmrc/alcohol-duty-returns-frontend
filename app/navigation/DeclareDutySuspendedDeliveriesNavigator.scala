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

  // Defines the regular flow of this subjourney
  private val normalRoutes: Page => UserAnswers => Call = {
    case DeclareDutySuspendedDeliveriesQuestionPage  =>
      _ => routes.DeclareDutySuspendedDeliveriesOutsideUkController.onPageLoad(NormalMode)
    case DeclareDutySuspendedDeliveriesOutsideUkPage =>
      _ => routes.DutySuspendedDeliveriesController.onPageLoad(NormalMode)
    case DutySuspendedDeliveriesPage                 =>
      _ => routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad
    case _                                           =>
      _ => routes.IndexController.onPageLoad
  }

  // For when the user has navigated from the final check ALL your answers page.
  // It will navigate them back to the final check page, unless otherwise stated as a case.
  // For example, a user might navigate to a question that requires the next to also be changed.
  // Or, as well, there may need to be some logic - e.g. to delete other answers if a yes no question is changed.
  // Keeping this simple is important, the original navigator relies on modes, and so should we.
  // The modes are picked by visiting slightly different urls defined in the routes file.
  // So we should do the same.
  private val checkRouteMap: Page => UserAnswers => Call = { case _ =>
    _ => routes.CheckYourAnswersController.onPageLoad
  }

  // For when the user has navigated from this subjourney's check answers page.
  // It will navigate them back to that subjourney check page unless otherwise stated, similar to before.
  private val checkDutySuspendedDeliveriesRouteMap: Page => UserAnswers => Call = { case _ =>
    _ => routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad
  }

  // The method that is called by the controller has been modified to accept a new mode
  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode                       =>
      normalRoutes(page)(userAnswers)
    case CheckMode                        =>
      checkRouteMap(page)(userAnswers)
    case CheckDutySuspendedDeliveriesMode =>
      checkDutySuspendedDeliveriesRouteMap(page)(userAnswers)
  }
}
