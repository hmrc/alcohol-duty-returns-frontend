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
import forms.adjustment.AdjustmentVolumeFormProvider

import javax.inject.Inject
import models.{AlcoholRegime, Mode}
import navigation.AdjustmentNavigator
import pages.adjustment.{AdjustmentVolumePage, CurrentAdjustmentEntryPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import models.adjustment.{AdjustmentEntry, AdjustmentVolume}
import models.requests.DataRequest
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returns.RateBandHelper.rateBandContent
import views.html.adjustment.AdjustmentVolumeView

import scala.concurrent.{ExecutionContext, Future}

class AdjustmentVolumeController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AdjustmentVolumeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentVolumeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def getRegime(implicit request: DataRequest[_]): AlcoholRegime = {
    val rateBand = request.userAnswers.get(CurrentAdjustmentEntryPage).flatMap(_.rateBand)
    rateBand
      .map(_.rangeDetails.map(_.alcoholRegime).head)
      .getOrElse(throw new RuntimeException("Couldn't fetch regime value from cache"))
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val form                            = formProvider(getRegime)
    val (preparedForm, adjustmentEntry) = request.userAnswers.get(CurrentAdjustmentEntryPage) match {
      case None                  => (form, AdjustmentEntry())
      case Some(adjustmentEntry) =>
        val filledForm = (for {
          totalLitresVolume <- adjustmentEntry.totalLitresVolume
          pureAlcoholVolume <- adjustmentEntry.pureAlcoholVolume
        } yield form.fill(AdjustmentVolume(totalLitresVolume, pureAlcoholVolume)))
          .getOrElse(form)
        (filledForm, adjustmentEntry)
    }

    Ok(
      view(
        preparedForm,
        mode,
        adjustmentEntry.adjustmentType.getOrElse(
          throw new RuntimeException("Couldn't fetch adjustment type value from cache")
        ),
        getRegime,
        rateBandContent(
          adjustmentEntry.rateBand.getOrElse(throw new RuntimeException("Couldn't fetch rateBand from cache"))
        )
      )
    )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider(getRegime)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => handleFormErrors(mode, formWithErrors),
          value => {
            val adjustment                      = request.userAnswers.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
            val (updatedAdjustment, hasChanged) = updateVolume(adjustment, value)
            for {
              updatedAnswers <-
                Future.fromTry(
                  request.userAnswers.set(
                    CurrentAdjustmentEntryPage,
                    updatedAdjustment.copy(
                      totalLitresVolume = Some(value.totalLitresVolume),
                      pureAlcoholVolume = Some(value.pureAlcoholVolume)
                    )
                  )
                )
              _              <- cacheConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AdjustmentVolumePage, mode, updatedAnswers, hasChanged))
          }
        )
  }

  private def handleFormErrors(mode: Mode, formWithErrors: Form[AdjustmentVolume])(implicit
    request: DataRequest[_]
  ): Future[Result] =
    request.userAnswers.get(CurrentAdjustmentEntryPage) match {
      case None        => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case Some(value) =>
        Future.successful(
          BadRequest(
            view(
              formWithErrors,
              mode,
              value.adjustmentType.getOrElse(
                throw new RuntimeException("Couldn't fetch adjustment type value from cache")
              ),
              getRegime,
              rateBandContent(
                value.rateBand.getOrElse(throw new RuntimeException("Couldn't fetch rateBand from cache"))
              )
            )
          )
        )
    }

  def updateVolume(adjustmentEntry: AdjustmentEntry, currentValue: AdjustmentVolume): (AdjustmentEntry, Boolean) =
    (adjustmentEntry.totalLitresVolume, adjustmentEntry.pureAlcoholVolume) match {
      case (Some(existingTotalLitres), Some(existingPureAlcohol))
          if currentValue.totalLitresVolume == existingTotalLitres && currentValue.pureAlcoholVolume == existingPureAlcohol =>
        (adjustmentEntry, false)
      case _ =>
        (
          adjustmentEntry.copy(
            duty = None,
            repackagedRateBand = None,
            repackagedDuty = None,
            repackagedSprDutyRate = None,
            newDuty = None,
            sprDutyRate = None
          ),
          true
        )
    }

}
