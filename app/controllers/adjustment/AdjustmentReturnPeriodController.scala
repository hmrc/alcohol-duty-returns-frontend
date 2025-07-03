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
import forms.adjustment.AdjustmentReturnPeriodFormProvider
import models.Mode
import models.adjustment.AdjustmentEntry
import navigation.AdjustmentNavigator
import pages.adjustment.{AdjustmentReturnPeriodPage, CurrentAdjustmentEntryPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.adjustment.AdjustmentReturnPeriodHelper
import views.html.adjustment.AdjustmentReturnPeriodView

import java.time.YearMonth
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdjustmentReturnPeriodController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AdjustmentReturnPeriodFormProvider,
  helper: AdjustmentReturnPeriodHelper,
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentReturnPeriodView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val returnPeriod = request.returnPeriod.period
    val form         = formProvider(returnPeriod)
    request.userAnswers.get(CurrentAdjustmentEntryPage) match {
      case Some(AdjustmentEntry(_, Some(adjustmentType), Some(period), _, _, _, _, _, _, _, _, _, _)) =>
        Ok(view(form.fill(period), mode, adjustmentType))
      case Some(AdjustmentEntry(_, Some(adjustmentType), _, _, _, _, _, _, _, _, _, _, _))            =>
        Ok(view(form, mode, adjustmentType))
      case _                                                                                          =>
        logger.warn("Couldn't fetch the adjustmentType and period in AdjustmentEntry from user answers")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val returnPeriod = request.returnPeriod.period
      val form         = formProvider(returnPeriod)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            request.userAnswers.get(CurrentAdjustmentEntryPage) match {
              case Some(AdjustmentEntry(_, Some(adjustmentType), _, _, _, _, _, _, _, _, _, _, _)) =>
                Future.successful(
                  BadRequest(view(formWithErrors, mode, adjustmentType))
                )
              case _                                                                               =>
                logger.warn("Couldn't fetch the adjustmentType in AdjustmentEntry from user answers")
                Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            },
          value => {
            val adjustment                      = request.userAnswers.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
            val (updatedAdjustment, hasChanged) = updatePeriod(adjustment, value)
            for {
              updatedAnswers <-
                Future.fromTry(
                  request.userAnswers.set(CurrentAdjustmentEntryPage, updatedAdjustment.copy(period = Some(value)))
                )
              _              <- userAnswersConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AdjustmentReturnPeriodPage, mode, updatedAnswers, Some(hasChanged)))
          }
        )
  }

  def updatePeriod(adjustmentEntry: AdjustmentEntry, currentValue: YearMonth): (AdjustmentEntry, Boolean) =
    adjustmentEntry.period match {
      case Some(existingValue) if currentValue == existingValue => (adjustmentEntry, false)
      case _                                                    =>
        (
          adjustmentEntry.copy(
            totalLitresVolume = None,
            pureAlcoholVolume = None,
            sprDutyRate = None,
            rateBand = None,
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
