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

import controllers.actions._
import forms.adjustment.AdjustmentSmallProducerReliefDutyRateFormProvider

import javax.inject.Inject
import models.Mode
import navigation.AdjustmentNavigator
import pages.adjustment.{AdjustmentSmallProducerReliefDutyRatePage, CurrentAdjustmentEntryPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import models.adjustment.AdjustmentEntry
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustment.AdjustmentSmallProducerReliefDutyRateView

import scala.concurrent.{ExecutionContext, Future}

class AdjustmentSmallProducerReliefDutyRateController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AdjustmentSmallProducerReliefDutyRateFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentSmallProducerReliefDutyRateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(CurrentAdjustmentEntryPage) match {
      case Some(AdjustmentEntry(_, Some(adjustmentType), _, _, _, _, _, _, Some(repackagedSprDutyRate), _, _, _)) =>
        Ok(
          view(
            form.fill(repackagedSprDutyRate),
            mode,
            adjustmentType
          )
        )
      case Some(AdjustmentEntry(_, Some(adjustmentType), _, _, _, _, _, _, _, _, _, _))                           =>
        Ok(
          view(
            form,
            mode,
            adjustmentType
          )
        )
      case _                                                                                                      =>
        logger.warn("Couldn't fetch the adjustmentType and repackagedSprDutyRate in AdjustmentEntry from user answers")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            request.userAnswers.get(CurrentAdjustmentEntryPage) match {
              case Some(AdjustmentEntry(_, Some(adjustmentType), _, _, _, _, _, _, _, _, _, _)) =>
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      mode,
                      adjustmentType
                    )
                  )
                )
              case _                                                                            =>
                logger.warn("Couldn't fetch the adjustmentType in AdjustmentEntry from user answers")
                Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            },
          value => {
            val adjustment                      = request.userAnswers.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
            val (updatedAdjustment, hasChanged) = updateSPRRate(adjustment, value)
            for {
              updatedAnswers <-
                Future.fromTry(
                  request.userAnswers
                    .set(CurrentAdjustmentEntryPage, updatedAdjustment.copy(repackagedSprDutyRate = Some(value)))
                )
              _              <- cacheConnector.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(AdjustmentSmallProducerReliefDutyRatePage, mode, updatedAnswers, hasChanged)
            )
          }
        )
  }

  def updateSPRRate(adjustmentEntry: AdjustmentEntry, currentValue: BigDecimal): (AdjustmentEntry, Boolean) =
    adjustmentEntry.repackagedSprDutyRate match {
      case Some(existingValue) if currentValue == existingValue => (adjustmentEntry, false)
      case _                                                    =>
        (
          adjustmentEntry.copy(
            duty = None,
            newDuty = None,
            repackagedDuty = None
          ),
          true
        )
    }
}
