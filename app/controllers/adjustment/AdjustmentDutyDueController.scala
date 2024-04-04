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

import connectors.CacheConnector
import controllers.actions._
import models.adjustment.AdjustmentEntry
import pages.adjustment.CurrentAdjustmentEntryPage

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import services.adjustmentEntry.AdjustmentEntryService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustment.AdjustmentTypeHelper
import views.html.adjustment.AdjustmentDutyDueView

import scala.concurrent.{ExecutionContext, Future}

class AdjustmentDutyDueController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  adjustmentEntryService: AdjustmentEntryService,
  view: AdjustmentDutyDueView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    for {
      adjustment  <- adjustmentEntryService.createAdjustment(request.userAnswers)
      userAnswers <- Future.fromTry(request.userAnswers.set(CurrentAdjustmentEntryPage, adjustment))
      _           <- cacheConnector.set(userAnswers)
    } yield getView(adjustment)

  }

  private def getView(adjustmentEntry: AdjustmentEntry)(implicit request: Request[_]): Result = {
    val result = for {
      abv               <- adjustmentEntry.abv
      volume            <- adjustmentEntry.volume
      pureAlcoholVolume <- adjustmentEntry.pureAlcoholVolume
      taxCode           <- adjustmentEntry.taxCode
      duty              <- adjustmentEntry.duty
      rate              <- adjustmentEntry.rate
      adjustmentType    <- Some(AdjustmentTypeHelper.getAdjustmentTypeValue(adjustmentEntry))

    } yield Ok(view(adjustmentType, abv.value, volume, duty, pureAlcoholVolume, taxCode, rate))

    result.getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }
}
