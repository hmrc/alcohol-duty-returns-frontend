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
import controllers._
import models.RateType.{Core, DraughtAndSmallProducerRelief, DraughtRelief, SmallProducerRelief}
import models._
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.RepackagedDraughtProducts
import pages._
import pages.adjustment.CurrentAdjustmentEntryPage
import play.api.mvc.Call
import viewmodels.checkAnswers.adjustment.AdjustmentTypeHelper

import javax.inject.{Inject, Singleton}

@Singleton
class AdjustmentNavigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case pages.adjustment.DeclareAdjustmentQuestionPage             =>
      _ => controllers.adjustment.routes.AdjustmentTypeController.onPageLoad(NormalMode)
    case pages.adjustment.AdjustmentTypePage                        =>
      _ => controllers.adjustment.routes.WhenDidYouPayDutyController.onPageLoad(NormalMode)
    case pages.adjustment.WhenDidYouPayDutyPage                     =>
      _ => controllers.adjustment.routes.AdjustmentTaxTypeController.onPageLoad(NormalMode)
    case pages.adjustment.AdjustmentTaxTypePage                     => adjustmentTaxTypePageRoute
    case pages.adjustment.AdjustmentVolumePage                      => adjustmentVolumePageRoute
    case pages.adjustment.AdjustmentVolumeWithSPRPage               => adjustmentVolumePageRoute
    case pages.adjustment.AdjustmentSmallProducerReliefDutyRatePage =>
      _ => controllers.adjustment.routes.AdjustmentDutyDueController.onPageLoad()
    case pages.adjustment.AdjustmentRepackagedTaxTypePage           => repackagedTaxTypeRoute
    case _                                                          =>
      _ => routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Boolean => Call = {
    case pages.adjustment.AdjustmentTypePage              =>
      _ =>
        hasChanged =>
          if (hasChanged) controllers.adjustment.routes.WhenDidYouPayDutyController.onPageLoad(CheckMode)
          else controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
    case pages.adjustment.WhenDidYouPayDutyPage           =>
      _ =>
        hasChanged =>
          if (hasChanged) controllers.adjustment.routes.AdjustmentTaxTypeController.onPageLoad(CheckMode)
          else controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
    case pages.adjustment.AdjustmentTaxTypePage           =>
      userAnswers =>
        hasChanged =>
          if (hasChanged) adjustmentTaxTypePageRoute(userAnswers)
          else controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
    case pages.adjustment.AdjustmentVolumePage            =>
      userAnswers =>
        hasChanged =>
          if (hasChanged) adjustmentVolumePageRoute(userAnswers)
          else controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
    case pages.adjustment.AdjustmentVolumeWithSPRPage     =>
      userAnswers =>
        hasChanged =>
          if (hasChanged) adjustmentVolumePageRoute(userAnswers)
          else controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
    case pages.adjustment.AdjustmentRepackagedTaxTypePage =>
      userAnswers =>
        hasChanged =>
          if (hasChanged) repackagedTaxTypeRoute(userAnswers)
          else controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
    case _                                                => _ => _ => controllers.adjustment.routes.CheckYourAnswersController.onPageLoad()
  }

  private def adjustmentTaxTypePageRoute(userAnswers: UserAnswers): Call = {
    val rateType = for {
      adjustment <- userAnswers.get(pages.adjustment.CurrentAdjustmentEntryPage)
      rateType   <- adjustment.rateBand.map(_.rateType)
    } yield rateType
    rateType match {
      case Some(Core)                          => controllers.adjustment.routes.AdjustmentVolumeController.onPageLoad(NormalMode)
      case Some(DraughtRelief)                 =>
        controllers.adjustment.routes.AdjustmentVolumeController.onPageLoad(NormalMode)
      case Some(DraughtAndSmallProducerRelief) =>
        controllers.adjustment.routes.AdjustmentVolumeWithSPRController.onPageLoad(NormalMode)
      case Some(SmallProducerRelief)           =>
        controllers.adjustment.routes.AdjustmentVolumeWithSPRController.onPageLoad(NormalMode)
      case _                                   => routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private def adjustmentVolumePageRoute(userAnswers: UserAnswers): Call = {
    val adjustmentType = AdjustmentTypeHelper.getAdjustmentTypeValue(
      userAnswers.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
    )
    adjustmentType match {
      case RepackagedDraughtProducts.toString =>
        controllers.adjustment.routes.AdjustmentRepackagedTaxTypeController.onPageLoad(NormalMode)
      case _                                  => controllers.adjustment.routes.AdjustmentDutyDueController.onPageLoad()
    }
  }

  private def repackagedTaxTypeRoute(userAnswers: UserAnswers): Call = {
    val rateType = for {
      adjustment <- userAnswers.get(pages.adjustment.CurrentAdjustmentEntryPage)
      rateType   <- adjustment.repackagedRateBand.map(_.rateType)
    } yield rateType
    rateType match {
      case Some(DraughtAndSmallProducerRelief) =>
        controllers.adjustment.routes.AdjustmentSmallProducerReliefDutyRateController.onPageLoad(NormalMode)
      case Some(SmallProducerRelief)           =>
        controllers.adjustment.routes.AdjustmentSmallProducerReliefDutyRateController.onPageLoad(NormalMode)
      case _                                   => controllers.adjustment.routes.AdjustmentDutyDueController.onPageLoad()
    }
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, hasAnswerChanged: Boolean = true): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)(hasAnswerChanged)
  }
}
