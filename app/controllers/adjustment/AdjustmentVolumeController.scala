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
import models.Mode
import navigation.AdjustmentNavigator
import pages.adjustment.{AdjustmentVolumePage, CurrentAdjustmentEntryPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import models.adjustment.{AdjustmentEntry, AdjustmentVolume}
import models.requests.DataRequest
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustment.AdjustmentTypeHelper
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

  private def getRegime(implicit request: DataRequest[_]): String = {
    val rateBand = request.userAnswers.get(CurrentAdjustmentEntryPage).flatMap(_.rateBand)
    rateBand
      .map(_.alcoholRegime.head)
      .getOrElse(throw new RuntimeException("Couldn't fetch regime value from cache"))
      .toString
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
        AdjustmentTypeHelper.getAdjustmentTypeValue(adjustmentEntry),
        getRegime, // change this
        adjustmentEntry.rateBand
          .map(_.minABV.value)
          .getOrElse(throw new RuntimeException("Couldn't fetch minABV value from cache")),
        adjustmentEntry.rateBand
          .map(_.maxABV.value)
          .getOrElse(throw new RuntimeException("Couldn't fetch maxABV value from cache")),
        adjustmentEntry.rateBand
          .map(_.taxType)
          .getOrElse(throw new RuntimeException("Couldn't fetch taxType value from cache"))
      )
    )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider(getRegime)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            request.userAnswers.get(CurrentAdjustmentEntryPage) match {
              case None        => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
              case Some(value) =>
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      mode,
                      AdjustmentTypeHelper.getAdjustmentTypeValue(value),
                      getRegime, // change this
                      value.rateBand
                        .map(_.minABV.value)
                        .getOrElse(throw new RuntimeException("Couldn't fetch minABV value from cache")),
                      value.rateBand
                        .map(_.maxABV.value)
                        .getOrElse(throw new RuntimeException("Couldn't fetch maxABV value from cache")),
                      value.rateBand
                        .map(_.taxType)
                        .getOrElse(throw new RuntimeException("Couldn't fetch taxType value from cache"))
                    )
                  )
                )
            },
          value => {
            val adjustment                      = request.userAnswers.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
            val (updatedAdjustment, hasChanged) = updateVolume(adjustment, value)
            for {
              updatedAnswers <-
                Future.fromTry(
                  request.userAnswers.set(
                    CurrentAdjustmentEntryPage,
                    updatedAdjustment.copy(
                      totalLitresVolume = Some(value.totalLitersVolume),
                      pureAlcoholVolume = Some(value.pureAlcoholVolume)
                    )
                  )
                )
              _              <- cacheConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AdjustmentVolumePage, mode, updatedAnswers, hasChanged))
          }
        )
  }

  def updateVolume(adjustmentEntry: AdjustmentEntry, currentValue: AdjustmentVolume): (AdjustmentEntry, Boolean) =
    (adjustmentEntry.totalLitresVolume, adjustmentEntry.pureAlcoholVolume) match {
      case (Some(existingTotalLitres), Some(existingPureAlcohol))
          if currentValue.totalLitersVolume == existingTotalLitres && currentValue.pureAlcoholVolume == existingPureAlcohol =>
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
