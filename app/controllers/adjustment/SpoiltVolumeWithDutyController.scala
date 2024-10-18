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
import forms.adjustment.SpoiltVolumeWithDutyFormProvider
import models.adjustment.{AdjustmentEntry, AdjustmentVolumeWithSPR}
import models.requests.DataRequest
import models.{AlcoholRegime, Mode}
import navigation.AdjustmentNavigator
import pages.adjustment.{SpoiltVolumeWithDutyPage, CurrentAdjustmentEntryPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustment.SpoiltVolumeWithDutyView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SpoiltVolumeWithDutyController @Inject()(
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SpoiltVolumeWithDutyFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SpoiltVolumeWithDutyView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def getRegime(implicit request: DataRequest[_]): Option[AlcoholRegime] =
    request.userAnswers.get(CurrentAdjustmentEntryPage) match {
      case Some(AdjustmentEntry(_, _, _, _, Some(rateBand), _, _, _, _, _, _, _, _)) =>
        rateBand.rangeDetails.map(_.alcoholRegime).headOption
      case _                                                                      => None
    }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    getRegime match {
      case Some(regime) =>
        val form = formProvider(regime)
        request.userAnswers.get(CurrentAdjustmentEntryPage) match {

          case Some(
                AdjustmentEntry(
                  _,
                  Some(adjustmentType),
                  _,
                  _,
                  _,
                  Some(totalLitres),
                  Some(pureAlcohol),
                  _,
                  _,
                  _,
                  Some(duty),
                  _,
                  _
                )
              ) =>
            Ok(
              view(
                form.fill(AdjustmentVolumeWithSPR(totalLitres, pureAlcohol, duty)),
                mode,
                regime
              )
            )
          case Some(AdjustmentEntry(_, _, _, _, _, _, _, _, _, _, _, _, _)) =>
            Ok(
              view(
                form,
                mode,
                regime
              )
            )
          case _                                                                                         =>
            logger.warn(
              "Couldn't fetch the adjustmentType, rateBand, totalLitres, pureAlcohol and sprDutyRate in AdjustmentEntry from user answers"
            )
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      case _            =>
        logger.warn("Couldn't fetch regime value in AdjustmentEntry from user answers")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getRegime match {
        case Some(regime) =>
          val form = formProvider(regime)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => handleFormErrors(mode, formWithErrors, regime),
              value => {
                val adjustment                      = request.userAnswers.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
                val (updatedAdjustment, hasChanged) = updateSpoiltVolumeAndDuty(adjustment, value)
                for {
                  updatedAnswers <- Future.fromTry(
                                      request.userAnswers.set(
                                        CurrentAdjustmentEntryPage,
                                        updatedAdjustment.copy(
                                          totalLitresVolume = Some(value.totalLitresVolume),
                                          pureAlcoholVolume = Some(value.pureAlcoholVolume),
                                          duty = Some(value.sprDutyRate)
                                        )
                                      )
                                    )
                  _              <- cacheConnector.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(SpoiltVolumeWithDutyPage, mode, updatedAnswers, hasChanged))
              }
            )
        case _            =>
          logger.warn("Couldn't fetch regime value in AdjustmentEntry from user answers")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }

  private def handleFormErrors(mode: Mode, formWithErrors: Form[AdjustmentVolumeWithSPR], regime: AlcoholRegime)(
    implicit request: DataRequest[_]
  ): Future[Result] =
    request.userAnswers.get(CurrentAdjustmentEntryPage) match {
      case Some(AdjustmentEntry(_, _, _, _, _, _, _, _, _, _, _, _, _)) =>
        Future.successful(
          BadRequest(
            view(
              formWithErrors,
              mode,
              regime
            )
          )
        )
      case _                                                                                         =>
        logger.warn("Couldn't fetch the adjustmentType and rateBand in AdjustmentEntry from user answers")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

  def updateSpoiltVolumeAndDuty(
    adjustmentEntry: AdjustmentEntry,
    currentValue: AdjustmentVolumeWithSPR
  ): (AdjustmentEntry, Boolean) =
    (adjustmentEntry.totalLitresVolume, adjustmentEntry.pureAlcoholVolume, adjustmentEntry.duty) match {
      case (Some(existingTotalLitres), Some(existingPureAlcohol), Some(existingDuty))
          if currentValue.totalLitresVolume == existingTotalLitres && currentValue.pureAlcoholVolume == existingPureAlcohol && currentValue.sprDutyRate == existingDuty =>
        (adjustmentEntry, false)
      case _ =>
        (
          adjustmentEntry.copy(
            duty = None,
            repackagedRateBand = None,
            repackagedDuty = None,
            repackagedSprDutyRate = None,
            newDuty = None
          ),
          true
        )
    }

}
