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
import forms.adjustment.AdjustmentVolumeWithSPRFormProvider

import javax.inject.Inject
import models.{AlcoholRegime, Mode}
import navigation.AdjustmentNavigator
import pages.adjustment.{AdjustmentVolumeWithSPRPage, CurrentAdjustmentEntryPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import connectors.UserAnswersConnector
import models.adjustment.{AdjustmentEntry, AdjustmentVolumeWithSPR}
import models.requests.DataRequest
import play.api.Logging
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.declareDuty.RateBandHelper.rateBandContent
import views.html.adjustment.AdjustmentVolumeWithSPRView

import scala.concurrent.{ExecutionContext, Future}

class AdjustmentVolumeWithSPRController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AdjustmentVolumeWithSPRFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentVolumeWithSPRView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def getRegime(implicit request: DataRequest[_]): Option[AlcoholRegime] =
    request.userAnswers.get(CurrentAdjustmentEntryPage) match {
      case Some(AdjustmentEntry(_, _, _, _, Some(rateBand), _, _, _, _, _, _, _, _)) =>
        rateBand.rangeDetails.map(_.alcoholRegime).headOption
      case _                                                                         => None
    }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    getRegime match {
      case Some(regime) =>
        val form = formProvider()
        request.userAnswers.get(CurrentAdjustmentEntryPage) match {

          case Some(
                AdjustmentEntry(
                  _,
                  Some(adjustmentType),
                  _,
                  _,
                  Some(rateBand),
                  Some(totalLitres),
                  Some(pureAlcohol),
                  Some(sprDutyRate),
                  _,
                  _,
                  _,
                  _,
                  _
                )
              ) =>
            Ok(
              view(
                form.fill(AdjustmentVolumeWithSPR(totalLitres, pureAlcohol, sprDutyRate)),
                mode,
                adjustmentType,
                regime,
                rateBandContent(rateBand, None).capitalize
              )
            )
          case Some(AdjustmentEntry(_, Some(adjustmentType), _, _, Some(rateBand), _, _, _, _, _, _, _, _)) =>
            Ok(
              view(
                form,
                mode,
                adjustmentType,
                regime,
                rateBandContent(rateBand, None).capitalize
              )
            )
          case _                                                                                            =>
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
          val form = formProvider()
          form
            .bindFromRequest()
            .fold(
              formWithErrors => handleFormErrors(mode, formWithErrors, regime),
              value => {
                val adjustment                      = request.userAnswers.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
                val (updatedAdjustment, hasChanged) = updateVolume(adjustment, value)
                for {
                  updatedAnswers <- Future.fromTry(
                                      request.userAnswers.set(
                                        CurrentAdjustmentEntryPage,
                                        updatedAdjustment.copy(
                                          totalLitresVolume = Some(value.totalLitresVolume),
                                          pureAlcoholVolume = Some(value.pureAlcoholVolume),
                                          sprDutyRate = Some(value.sprDutyRate)
                                        )
                                      )
                                    )
                  _              <- userAnswersConnector.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(AdjustmentVolumeWithSPRPage, mode, updatedAnswers, hasChanged))
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
      case Some(AdjustmentEntry(_, Some(adjustmentType), _, _, Some(rateBand), _, _, _, _, _, _, _, _)) =>
        Future.successful(
          BadRequest(
            view(
              formWithErrors,
              mode,
              adjustmentType,
              regime,
              rateBandContent(rateBand, None).capitalize
            )
          )
        )
      case _                                                                                            =>
        logger.warn("Couldn't fetch the adjustmentType and rateBand in AdjustmentEntry from user answers")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

  def updateVolume(
    adjustmentEntry: AdjustmentEntry,
    currentValue: AdjustmentVolumeWithSPR
  ): (AdjustmentEntry, Boolean) =
    (adjustmentEntry.totalLitresVolume, adjustmentEntry.pureAlcoholVolume, adjustmentEntry.sprDutyRate) match {
      case (Some(existingTotalLitres), Some(existingPureAlcohol), Some(existingSprDutyRate))
          if currentValue.totalLitresVolume == existingTotalLitres && currentValue.pureAlcoholVolume == existingPureAlcohol && currentValue.sprDutyRate == existingSprDutyRate =>
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
