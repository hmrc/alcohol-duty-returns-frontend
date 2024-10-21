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
import forms.adjustment.AdjustmentTypeFormProvider

import javax.inject.Inject
import models.{Mode, UserAnswers}
import navigation.AdjustmentNavigator
import pages.adjustment.{AdjustmentTypePage, CurrentAdjustmentEntryPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustment.AlcoholicProductTypeHelper
import views.html.adjustment.AdjustmentTypeView

import java.time.YearMonth
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AdjustmentTypeController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AdjustmentTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentTypeView,
  helper: AlcoholicProductTypeHelper
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val adjustmentType = request.userAnswers.get(CurrentAdjustmentEntryPage).flatMap(_.adjustmentType)
    val preparedForm   = adjustmentType match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value => {
            val adjustment                      = request.userAnswers.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
            val (updatedAdjustment, hasChanged) = updateAdjustmentType(adjustment, value)
            for {
              updatedAnswers                <-
                Future.fromTry(
                  request.userAnswers
                    .set(CurrentAdjustmentEntryPage, updatedAdjustment.copy(adjustmentType = Some(value)))
                )
              singleRegimeUpdatedUserAnswer <- Future.fromTry(checkIfOneRegimeAndUpdateUserAnswer(updatedAnswers))
              _                             <- cacheConnector.set(singleRegimeUpdatedUserAnswer)
            } yield Redirect(navigator.nextPage(AdjustmentTypePage, mode, singleRegimeUpdatedUserAnswer, hasChanged))
          }
        )
  }

  def updateAdjustmentType(adjustmentEntry: AdjustmentEntry, currentValue: AdjustmentType): (AdjustmentEntry, Boolean) =
    adjustmentEntry.adjustmentType match {
      case Some(existingValue) if currentValue == existingValue => (adjustmentEntry, false)
      case _                                                    =>
        (
          adjustmentEntry.copy(
            period = None,
            rateBand = None,
            spoiltRegime = None,
            totalLitresVolume = None,
            pureAlcoholVolume = None,
            sprDutyRate = None,
            duty = None,
            repackagedRateBand = None,
            repackagedDuty = None,
            repackagedSprDutyRate = None,
            newDuty = None
          ),
          true
        )
    }

  private def checkIfOneRegimeAndUpdateUserAnswer(
    userAnswer: UserAnswers
  )(implicit messages: Messages): Try[UserAnswers] =
    if (userAnswer.regimes.regimes.size == 1) {
      val adjustment = userAnswer.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
      val rateBand   = helper.createRateBandFromRegime(userAnswer.regimes.regimes.head)
      userAnswer.set(
        CurrentAdjustmentEntryPage,
        adjustment.copy(
          spoiltRegime = userAnswer.regimes.regimes.headOption,
          rateBand = Some(rateBand),
          period = Some(YearMonth.of(2024, 1))
        )
      )
    } else {
      Try(userAnswer)
    }

}
