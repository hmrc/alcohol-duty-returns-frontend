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
import pages.spiritsQuestions.{DeclareQuarterlySpiritsPage, GrainsUsedPage, SpiritTypePage}
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class QuarterlySpiritsQuestionsNavigator @Inject() () extends BaseNavigator {

  override val normalRoutes: Page => UserAnswers => Call = {
    case pages.spiritsQuestions.DeclareQuarterlySpiritsPage   => declareQuarterlySpiritsRoute
    case pages.spiritsQuestions.DeclareSpiritsTotalPage       =>
      _ => controllers.spiritsQuestions.routes.WhiskyController.onPageLoad(NormalMode)
    case pages.spiritsQuestions.WhiskyPage                    =>
      _ => controllers.spiritsQuestions.routes.SpiritTypeController.onPageLoad(NormalMode)
    case pages.spiritsQuestions.SpiritTypePage                => spiritTypesRoute
    case pages.spiritsQuestions.OtherSpiritsProducedPage      =>
      _ => controllers.spiritsQuestions.routes.GrainsUsedController.onPageLoad(NormalMode)
    case pages.spiritsQuestions.GrainsUsedPage                => grainsUsedRoute
    case pages.spiritsQuestions.OtherMaltedGrainsPage         =>
      _ => controllers.spiritsQuestions.routes.AlcoholUsedController.onPageLoad(NormalMode)
    case pages.spiritsQuestions.AlcoholUsedPage               =>
      _ => controllers.spiritsQuestions.routes.EthyleneGasOrMolassesUsedController.onPageLoad(NormalMode)
    case pages.spiritsQuestions.EthyleneGasOrMolassesUsedPage =>
      _ => controllers.spiritsQuestions.routes.OtherIngredientsUsedController.onPageLoad(NormalMode)
    case _                                                    => _ => routes.IndexController.onPageLoad

  }

  override val checkRouteMap: Page => UserAnswers => Call = { case _ =>
    _ => routes.CheckYourAnswersController.onPageLoad()
  }

  private def declareQuarterlySpiritsRoute(userAnswers: UserAnswers): Call =
    userAnswers.get(DeclareQuarterlySpiritsPage) match {
      case Some(true)  => controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode)
      case Some(false) => routes.TaskListController.onPageLoad
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }

  private def spiritTypesRoute(userAnswers: UserAnswers): Call =
    userAnswers.get(SpiritTypePage) match {
      case Some(spiritsType) if SpiritTypePage.hasMadeOtherSpirits(spiritsType) =>
        controllers.spiritsQuestions.routes.OtherSpiritsProducedController.onPageLoad(NormalMode)
      case Some(_)                                                              =>
        controllers.spiritsQuestions.routes.GrainsUsedController.onPageLoad(NormalMode)
      case _                                                                    => routes.JourneyRecoveryController.onPageLoad()
    }

  private def grainsUsedRoute(userAnswers: UserAnswers): Call =
    userAnswers.get(GrainsUsedPage) match {
      case Some(grainsUsed) if GrainsUsedPage.hasUsedOtherMaltedGrains(grainsUsed) =>
        controllers.spiritsQuestions.routes.OtherMaltedGrainsController.onPageLoad(NormalMode)
      case Some(_)                                                                 =>
        controllers.spiritsQuestions.routes.AlcoholUsedController.onPageLoad(NormalMode)
      case _                                                                       => routes.JourneyRecoveryController.onPageLoad()
    }
}
