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
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import connectors.CacheConnector
import models.adjustment.{AdjustmentEntry, AdjustmentVolumeWithSPR}
import models.requests.DataRequest
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustment.AdjustmentTypeHelper
import views.html.adjustment.AdjustmentVolumeWithSPRView

import scala.concurrent.{ExecutionContext, Future}

class AdjustmentVolumeWithSPRController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AdjustmentVolumeWithSPRFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentVolumeWithSPRView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def getRegime(implicit request: DataRequest[_]): AlcoholRegime = {
    val rateBand = request.userAnswers.get(CurrentAdjustmentEntryPage).flatMap(_.rateBand)
    rateBand
      .map(_.alcoholRegime.head)
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
          sprDutyRate       <- adjustmentEntry.sprDutyRate
        } yield form.fill(AdjustmentVolumeWithSPR(totalLitresVolume, pureAlcoholVolume, sprDutyRate)))
          .getOrElse(form)
        (filledForm, adjustmentEntry)
    }

    Ok(
      view(
        preparedForm,
        mode,
        AdjustmentTypeHelper.getAdjustmentTypeValue(adjustmentEntry),
        getRegime,
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
          formWithErrors => handleFormErrors(mode, formWithErrors),
          value => {
            val adjustment = request.userAnswers.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
            if (value.totalLitersVolume < value.pureAlcoholVolume) {
              checkAdjustmentVolumes(
                value,
                mode,
                AdjustmentTypeHelper.getAdjustmentTypeValue(adjustment),
                getRegime,
                adjustment.rateBand
                  .map(_.minABV.value)
                  .getOrElse(throw new RuntimeException("Couldn't fetch minABV value from cache")),
                adjustment.rateBand
                  .map(_.maxABV.value)
                  .getOrElse(throw new RuntimeException("Couldn't fetch maxABV value from cache")),
                adjustment.rateBand
                  .map(_.taxType)
                  .getOrElse(throw new RuntimeException("Couldn't fetch taxType value from cache"))
              )
            } else {
              val (updatedAdjustment, hasChanged) = updateVolume(adjustment, value)
              for {
                updatedAnswers <- Future.fromTry(
                                    request.userAnswers.set(
                                      CurrentAdjustmentEntryPage,
                                      updatedAdjustment.copy(
                                        totalLitresVolume = Some(value.totalLitersVolume),
                                        pureAlcoholVolume = Some(value.pureAlcoholVolume),
                                        sprDutyRate = Some(value.sprDutyRate)
                                      )
                                    )
                                  )
                _              <- cacheConnector.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(AdjustmentVolumeWithSPRPage, mode, updatedAnswers, hasChanged))
            }
          }
        )
  }

  private def handleFormErrors(mode: Mode, formWithErrors: Form[AdjustmentVolumeWithSPR])(implicit
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
              AdjustmentTypeHelper.getAdjustmentTypeValue(AdjustmentEntry()),
              getRegime,
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
    }

  def updateVolume(
    adjustmentEntry: AdjustmentEntry,
    currentValue: AdjustmentVolumeWithSPR
  ): (AdjustmentEntry, Boolean) =
    (adjustmentEntry.totalLitresVolume, adjustmentEntry.pureAlcoholVolume, adjustmentEntry.sprDutyRate) match {
      case (Some(existingTotalLitres), Some(existingPureAlcohol), Some(existingSprDutyRate))
          if currentValue.totalLitersVolume == existingTotalLitres && currentValue.pureAlcoholVolume == existingPureAlcohol && currentValue.sprDutyRate == existingSprDutyRate =>
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

  private def checkAdjustmentVolumes(
    adjustmentVolume: AdjustmentVolumeWithSPR,
    mode: Mode,
    adjustmentType: String,
    regime: AlcoholRegime,
    minABV: BigDecimal,
    maxABV: BigDecimal,
    taxType: String
  )(implicit
    request: Request[_],
    messages: Messages
  ): Future[Result] =
    Future.successful(
      BadRequest(
        view(
          formProvider(regime)(messages)
            .withError(
              "adjustment-pure-alcohol-input",
              "adjustmentVolume.error.pureAlcoholVolume.lessThanExpected",
              messages(s"regime.$regime")
            )
            .fill(adjustmentVolume),
          mode,
          adjustmentType,
          regime,
          minABV,
          maxABV,
          taxType
        )(request, messages)
      )
    )
}
