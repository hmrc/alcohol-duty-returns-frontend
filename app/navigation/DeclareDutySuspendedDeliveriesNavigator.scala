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
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class DeclareDutySuspendedDeliveriesNavigator @Inject() () extends BaseNavigator {

  override val normalRoutes: Page => UserAnswers => Call = {
    case pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage  =>
      declareDutySuspendedDeliveriesQuestionPageRoute
    case pages.dutySuspended.DutySuspendedGuidancePage                   =>
      userAnswers => nextRegimePage(userAnswers, None, NormalMode)
    case pages.dutySuspended.DutySuspendedBeerPage                       =>
      userAnswers => nextRegimePage(userAnswers, Some(Beer), NormalMode)
    case pages.dutySuspended.DutySuspendedCiderPage                      =>
      userAnswers => nextRegimePage(userAnswers, Some(Cider), NormalMode)
    case pages.dutySuspended.DutySuspendedWinePage                       =>
      userAnswers => nextRegimePage(userAnswers, Some(Wine), NormalMode)
    case pages.dutySuspended.DutySuspendedSpiritsPage                    =>
      userAnswers => nextRegimePage(userAnswers, Some(Spirits), NormalMode)
    case pages.dutySuspended.DeclareDutySuspendedDeliveriesOutsideUkPage =>
      _ => controllers.dutySuspended.routes.DutySuspendedDeliveriesController.onPageLoad(NormalMode)
    case pages.dutySuspended.DutySuspendedDeliveriesPage                 =>
      _ => controllers.dutySuspended.routes.DeclareDutySuspendedReceivedController.onPageLoad(NormalMode)
    case pages.dutySuspended.DutySuspendedOtherFermentedPage             =>
      _ => controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad()
    case _                                                               =>
      _ => routes.IndexController.onPageLoad
  }

  override val checkRouteMap: Page => UserAnswers => Call = {
    case pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage =>
      declareDutySuspendedDeliveriesQuestionPageRoute
    case _                                                              =>
      _ => controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad()
  }

  private def declareDutySuspendedDeliveriesQuestionPageRoute(answers: UserAnswers): Call =
    answers.get(pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage) match {
      case Some(true)  => controllers.dutySuspended.routes.DutySuspendedDeliveriesGuidanceController.onPageLoad()
      case Some(false) => routes.TaskListController.onPageLoad
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }

  private def nextRegime(lastPageRegime: Option[AlcoholRegime], alcoholRegimes: AlcoholRegimes): Option[AlcoholRegime] =
    lastPageRegime match {
      case None                                                       => Seq(Beer, Cider, Wine, Spirits, OtherFermentedProduct).find(alcoholRegimes.hasRegime)
      case Some(Beer)                                                 => Seq(Cider, Wine, Spirits, OtherFermentedProduct).find(alcoholRegimes.hasRegime)
      case Some(Cider)                                                => Seq(Wine, Spirits, OtherFermentedProduct).find(alcoholRegimes.hasRegime)
      case Some(Wine)                                                 => Seq(Spirits, OtherFermentedProduct).find(alcoholRegimes.hasRegime)
      case Some(Spirits) if alcoholRegimes.hasOtherFermentedProduct() => Some(OtherFermentedProduct)
      case _                                                          => None
    }

  private val pageMapping: Map[AlcoholRegime, Mode => Call] = Map(
    Beer                  -> controllers.dutySuspended.routes.DutySuspendedBeerController.onPageLoad,
    Cider                 -> controllers.dutySuspended.routes.DutySuspendedCiderController.onPageLoad,
    Wine                  -> controllers.dutySuspended.routes.DutySuspendedWineController.onPageLoad,
    Spirits               -> controllers.dutySuspended.routes.DutySuspendedSpiritsController.onPageLoad,
    OtherFermentedProduct -> controllers.dutySuspended.routes.DutySuspendedOtherFermentedController.onPageLoad
  )

  private def nextRegimePage(userAnswers: UserAnswers, lastPageRegime: Option[AlcoholRegime], mode: Mode): Call =
    nextRegime(lastPageRegime, userAnswers.regimes)
      .flatMap(pageMapping.get(_).map(_(mode)))
      .getOrElse(controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad())
}
