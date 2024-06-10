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
import forms.adjustment.AdjustmentVolumeFormProvider

import javax.inject.Inject
import models.Mode
import navigation.AdjustmentNavigator
import pages.adjustment.{AdjustmentVolumePage, CurrentAdjustmentEntryPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import models.adjustment.AdjustmentEntry
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

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(CurrentAdjustmentEntryPage) match {
      case None                                             => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      case Some(value) if value.totalLitresVolume.isDefined =>
        Ok(view(form.fill(value.totalLitresVolume.get), mode, AdjustmentTypeHelper.getAdjustmentTypeValue(value)))
      case Some(value)                                      => Ok(view(form, mode, AdjustmentTypeHelper.getAdjustmentTypeValue(value)))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            request.userAnswers.get(CurrentAdjustmentEntryPage) match {
              case None        => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
              case Some(value) =>
                Future.successful(
                  BadRequest(view(formWithErrors, mode, AdjustmentTypeHelper.getAdjustmentTypeValue(value)))
                )
            },
          value => {
            val adjustment = request.userAnswers.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
            for {
              updatedAnswers <-
                Future.fromTry(
                  request.userAnswers.set(CurrentAdjustmentEntryPage, adjustment.copy(totalLitresVolume = Some(value)))
                )
              _              <- cacheConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AdjustmentVolumePage, mode, updatedAnswers))
          }
        )
  }
}
