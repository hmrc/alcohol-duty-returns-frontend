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
import forms.adjustment.AlcoholicProductTypeFormProvider

import javax.inject.Inject
import models.{AlcoholRegime, Mode}
import navigation.AdjustmentNavigator
import pages.adjustment.{AlcoholicProductTypePage, CurrentAdjustmentEntryPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import models.adjustment.AdjustmentEntry
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustment.AlcoholicProductTypeHelper
import views.html.adjustment.AlcoholicProductTypeView

import java.time.YearMonth
import scala.concurrent.{ExecutionContext, Future}

class AlcoholicProductTypeController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AlcoholicProductTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AlcoholicProductTypeView,
  helper: AlcoholicProductTypeHelper
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val spoiltRegimeOpt = request.userAnswers.get(CurrentAdjustmentEntryPage).flatMap(_.spoiltRegime)
    val preparedForm    = spoiltRegimeOpt match {
      case None        => form
      case Some(value) => form.fill(value.toString)
    }
    Ok(view(preparedForm, mode, request.userAnswers.regimes))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userAnswers.regimes))),
          value =>
            AlcoholRegime.fromString(value) match {
              case Some(regime) =>
                val rateBand                        = helper.createRateBandFromRegime(regime)
                val adjustment                      = request.userAnswers.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
                val (updatedAdjustment, hasChanged) = updateAlcoholicProductType(adjustment, regime)
                println(hasChanged)
                for {
                  updatedAnswers <- Future.fromTry(
                                      request.userAnswers.set(
                                        CurrentAdjustmentEntryPage,
                                        updatedAdjustment.copy(
                                          spoiltRegime = Some(regime),
                                          rateBand = Some(rateBand),
                                          period = Some(YearMonth.of(2024, 1))
                                        )
                                      )
                                    ) //change to the correct period
                  _              <- cacheConnector.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(AlcoholicProductTypePage, mode, updatedAnswers, hasChanged))
              case _            =>
                logger.warn("Couldn't retrieve regime")
                Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            }
        )
  }

  def updateAlcoholicProductType(
    adjustmentEntry: AdjustmentEntry,
    currentValue: AlcoholRegime
  ): (AdjustmentEntry, Boolean) =
    adjustmentEntry.spoiltRegime match {
      case Some(existingValue) if currentValue == existingValue => (adjustmentEntry, false)
      case _                                                    =>
        (
          adjustmentEntry.copy(
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
}
