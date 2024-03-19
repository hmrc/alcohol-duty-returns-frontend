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
import pages._
import models._

@Singleton
class ProductEntryNavigator @Inject() () extends BaseNavigator {

  override val normalRoutes: Page => UserAnswers => Call = {
    case pages.productEntry.ProductNamePage                        =>
      _ => controllers.productEntry.routes.AlcoholByVolumeQuestionController.onPageLoad(NormalMode)
    case pages.productEntry.AlcoholByVolumeQuestionPage            =>
      _ => controllers.productEntry.routes.DraughtReliefQuestionController.onPageLoad(NormalMode)
    case pages.productEntry.DraughtReliefQuestionPage              =>
      _ => controllers.productEntry.routes.SmallProducerReliefQuestionController.onPageLoad(NormalMode)
    case pages.productEntry.SmallProducerReliefQuestionPage        =>
      _ => controllers.productEntry.routes.TaxTypeController.onPageLoad(NormalMode)
    case pages.productEntry.TaxTypePage                            => taxTypePageRoute
    case pages.productEntry.DeclareSmallProducerReliefDutyRatePage =>
      _ => controllers.productEntry.routes.ProductVolumeController.onPageLoad(NormalMode)
    case pages.productEntry.DeclareAlcoholDutyQuestionPage         => declareAlcoholDutyQuestionPageRoute
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
      case Some(true)  =>
        controllers.productEntry.routes.DeclareSmallProducerReliefDutyRateController.onPageLoad(NormalMode)
      case Some(false) => controllers.productEntry.routes.ProductVolumeController.onPageLoad(NormalMode)
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }
  }

  override val checkRouteMap: Page => UserAnswers => Call                     = { case _ =>
    _ => controllers.productEntry.routes.CheckYourAnswersController.onPageLoad()
  }
  private def declareAlcoholDutyQuestionPageRoute(answers: UserAnswers): Call =
    answers.get(pages.productEntry.DeclareAlcoholDutyQuestionPage) match {
      case Some(true)  => controllers.productEntry.routes.ProductEntryGuidanceController.onPageLoad()
      case Some(false) => routes.IndexController.onPageLoad
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }

  private def productListPageRoute(answers: UserAnswers): Call =
    answers.get(pages.productEntry.ProductListPage) match {
      case Some(true)  => controllers.productEntry.routes.ProductNameController.onPageLoad(NormalMode)
      case Some(false) => routes.IndexController.onPageLoad
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }
}
