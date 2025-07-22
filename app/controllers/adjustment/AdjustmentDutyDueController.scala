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

package controllers.adjustment

import connectors.UserAnswersConnector
import controllers.actions._
import models.adjustment.AdjustmentEntry
import pages.adjustment.CurrentAdjustmentEntryPage
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import services.adjustment.AdjustmentEntryService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustment.AdjustmentDutyDueView

import viewmodels.checkAnswers.adjustment.AdjustmentDutyDueViewModelCreator

import scala.concurrent.{ExecutionContext, Future}

class AdjustmentDutyDueController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  adjustmentEntryService: AdjustmentEntryService,
  view: AdjustmentDutyDueView,
  viewModelFactory: AdjustmentDutyDueViewModelCreator
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    for {
      adjustment  <- adjustmentEntryService.createAdjustment(request.userAnswers)
      userAnswers <- Future.fromTry(request.userAnswers.set(CurrentAdjustmentEntryPage, adjustment))
      _           <- userAnswersConnector.set(userAnswers)
    } yield getView(adjustment)

  }

  private def getView(adjustmentEntry: AdjustmentEntry)(implicit request: Request[_]): Result = {
    val result = for {
      pureAlcoholVolume <- adjustmentEntry.pureAlcoholVolume
      duty              <- adjustmentEntry.duty
      rate              <- adjustmentEntry.rate
      adjustmentType    <- adjustmentEntry.adjustmentType
      repackagedRate     = adjustmentEntry.repackagedRate.getOrElse(BigDecimal(0))
      repackagedDuty     = adjustmentEntry.repackagedDuty.getOrElse(BigDecimal(0))
      newDuty            = adjustmentEntry.newDuty.getOrElse(BigDecimal(0))
    } yield Ok(
      view(
        viewModelFactory(
          adjustmentType,
          duty,
          newDuty,
          pureAlcoholVolume,
          rate,
          repackagedRate,
          repackagedDuty,
          adjustmentEntry.rateBand.flatMap(x => x.repackagedTaxTypeCode)
        ),
        adjustmentType
      )
    )
    result.getOrElse {
      logger.warn("Couldn't fetch correct AdjustmentEntry from user answers")
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

}
