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

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers._
import models.RateType.{Core, DraughtAndSmallProducerRelief, DraughtRelief, SmallProducerRelief}
import pages._
import models._
import pages.returns.DeclareAlcoholDutyQuestionPage

@Singleton
class ProductEntryNavigator @Inject() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case pages.productEntry.ProductNamePage                        =>
      _ => controllers.productEntry.routes.AlcoholByVolumeQuestionController.onPageLoad(NormalMode)
    case pages.productEntry.AlcoholByVolumeQuestionPage            => abvPageRoute
    case pages.productEntry.DraughtReliefQuestionPage              => draughtReliefRoute
    case pages.productEntry.SmallProducerReliefQuestionPage        =>
      _ => controllers.productEntry.routes.TaxTypeController.onPageLoad(NormalMode)
    case pages.productEntry.TaxTypePage                            => taxTypePageRoute
    case pages.productEntry.DeclareSmallProducerReliefDutyRatePage =>
      _ => controllers.productEntry.routes.ProductVolumeController.onPageLoad(NormalMode)
    case pages.productEntry.ProductVolumePage                      =>
      _ => controllers.productEntry.routes.DutyDueController.onPageLoad()
    case pages.productEntry.ProductListPage                        => productListPageRoute
    case _                                                         =>
      _ => routes.IndexController.onPageLoad

  }

  private def taxTypePageRoute(answers: UserAnswers): Call = {
    val eligibleForSPR = for {
      product             <- answers.get(pages.productEntry.CurrentProductEntryPage)
      smallProducerRelief <- product.smallProducerRelief
    } yield smallProducerRelief

    eligibleForSPR match {
      case Some(true) =>
        controllers.productEntry.routes.DeclareSmallProducerReliefDutyRateController.onPageLoad(NormalMode)
      case _          => controllers.productEntry.routes.ProductVolumeController.onPageLoad(NormalMode)
    }
  }
  private def abvPageRoute(answers: UserAnswers): Call = {
    val rateType = for {
      product  <- answers.get(pages.productEntry.CurrentProductEntryPage)
      rateType <- product.rateType
    } yield rateType
    rateType match {
      case Some(Core)                          => controllers.productEntry.routes.TaxTypeController.onPageLoad(NormalMode)
      case Some(DraughtAndSmallProducerRelief) =>
        controllers.productEntry.routes.DraughtReliefQuestionController.onPageLoad(NormalMode)
      case Some(SmallProducerRelief)           =>
        controllers.productEntry.routes.SmallProducerReliefQuestionController.onPageLoad(NormalMode)
      case Some(DraughtRelief)                 =>
        controllers.productEntry.routes.DraughtReliefQuestionController.onPageLoad(NormalMode)
      case _                                   => routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private def draughtReliefRoute(answers: UserAnswers): Call = {
    val rateType = for {
      product  <- answers.get(pages.productEntry.CurrentProductEntryPage)
      rateType <- product.rateType
    } yield rateType
    rateType match {
      case Some(DraughtAndSmallProducerRelief) =>
        controllers.productEntry.routes.SmallProducerReliefQuestionController.onPageLoad(NormalMode)
      case Some(DraughtRelief)                 =>
        controllers.productEntry.routes.TaxTypeController.onPageLoad(NormalMode)
      case _                                   => routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private val checkRouteMap: Page => UserAnswers => Boolean => Call           = {
    case pages.productEntry.AlcoholByVolumeQuestionPage            =>
      userAnswers =>
        hasChanged =>
          if (hasChanged) abvPageRoute(userAnswers)
          else controllers.productEntry.routes.CheckYourAnswersController.onPageLoad()
    case pages.productEntry.DraughtReliefQuestionPage              =>
      userAnswers =>
        hasChanged =>
          if (hasChanged) draughtReliefRoute(userAnswers)
          else controllers.productEntry.routes.CheckYourAnswersController.onPageLoad()
    case pages.productEntry.SmallProducerReliefQuestionPage        =>
      _ =>
        hasChanged =>
          if (hasChanged) controllers.productEntry.routes.TaxTypeController.onPageLoad(NormalMode)
          else controllers.productEntry.routes.CheckYourAnswersController.onPageLoad()
    case pages.productEntry.TaxTypePage                            =>
      userAnswers =>
        hasChanged =>
          if (hasChanged) taxTypePageRoute(userAnswers)
          else controllers.productEntry.routes.CheckYourAnswersController.onPageLoad()
    case pages.productEntry.ProductVolumePage                      =>
      _ =>
        hasChanged =>
          if (hasChanged) controllers.productEntry.routes.DutyDueController.onPageLoad()
          else controllers.productEntry.routes.CheckYourAnswersController.onPageLoad()
    case pages.productEntry.DeclareSmallProducerReliefDutyRatePage =>
      _ =>
        hasChanged =>
          if (hasChanged) controllers.productEntry.routes.ProductVolumeController.onPageLoad(NormalMode)
          else controllers.productEntry.routes.CheckYourAnswersController.onPageLoad()

    case pages.returns.DeclareAlcoholDutyQuestionPage =>
      userAnswers =>
        hasChanged =>
          if (hasChanged) declareAlcoholDutyQuestionPageRoute(userAnswers)
          else routes.TaskListController.onPageLoad
    case _                                            => _ => _ => controllers.productEntry.routes.CheckYourAnswersController.onPageLoad()
  }
  private def declareAlcoholDutyQuestionPageRoute(answers: UserAnswers): Call =
    answers.get(pages.returns.DeclareAlcoholDutyQuestionPage) match {
      case Some(true)  => controllers.productEntry.routes.ProductEntryGuidanceController.onPageLoad()
      case Some(false) => routes.TaskListController.onPageLoad
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }

  private def productListPageRoute(answers: UserAnswers): Call =
    answers.get(pages.productEntry.ProductListPage) match {
      case Some(true)  => controllers.productEntry.routes.ProductNameController.onPageLoad(NormalMode)
      case Some(false) => routes.IndexController.onPageLoad
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, hasAnswerChanged: Boolean = true): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)(hasAnswerChanged)
  }
}
