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
import forms.adjustment.WhenDidYouPayDutyFormProvider

import javax.inject.Inject
import models.Mode
import navigation.AdjustmentNavigator
import pages.adjustment.{CurrentAdjustmentEntryPage, WhenDidYouPayDutyPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.UserAnswersConnector
import handlers.ADRServerException
import models.adjustment.AdjustmentEntry
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustment.WhenDidYouPayDutyView

import java.time.YearMonth
import scala.concurrent.{ExecutionContext, Future}

class WhenDidYouPayDutyController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: WhenDidYouPayDutyFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhenDidYouPayDutyView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(CurrentAdjustmentEntryPage) match {
      case Some(AdjustmentEntry(_, Some(adjustmentType), Some(period), _, _, _, _, _, _, _, _, _, _)) =>
        Ok(
          view(
            form.fill(
              period
            ),
            mode,
            adjustmentType
          )
        )
      case Some(AdjustmentEntry(_, Some(adjustmentType), _, _, _, _, _, _, _, _, _, _, _))            =>
        Ok(
          view(
            form,
            mode,
            adjustmentType
          )
        )
      case _                                                                                          =>
        throw ADRServerException(s"Couldn't fetch adjustmentType and period in AdjustmentEntry from UserAnswers for page load $request")
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
              case Some(AdjustmentEntry(_, Some(adjustmentType), _, _, _, _, _, _, _, _, _, _, _)) =>
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      mode,
                      adjustmentType
                    )
                  )
                )
              case _                                                                               =>
                throw ADRServerException(s"Couldn't fetch adjustmentType in AdjustmentEntry from UserAnswers for page submit $request")
            },
          value => {
            val adjustment                      = request.userAnswers.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
            val (updatedAdjustment, hasChanged) = updatePeriod(adjustment, value)
            for {
              updatedAnswers <-
                Future.fromTry(
                  request.userAnswers
                    .set(CurrentAdjustmentEntryPage, updatedAdjustment.copy(period = Some(value)))
                )
              _              <- userAnswersConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(WhenDidYouPayDutyPage, mode, updatedAnswers, hasChanged))
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
