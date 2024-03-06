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
import forms.adjustment.AlcoholByVolumeFormProvider

import javax.inject.Inject
import models.{AlcoholByVolume, Mode}
import navigation.AdjustmentNavigator
import pages.adjustment.{AlcoholByVolumePage, CurrentAdjustmentEntryPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import models.adjustment.AdjustmentEntry
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustment.AlcoholByVolumeView

import scala.concurrent.{ExecutionContext, Future}

class AlcoholByVolumeController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        cacheConnector: CacheConnector,
                                        navigator: AdjustmentNavigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: AlcoholByVolumeFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: AlcoholByVolumeView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val abv = request.userAnswers.get(CurrentAdjustmentEntryPage).flatMap(_.abv)
      val preparedForm = abv match {
        case None => form
        case Some(alcoholByVolume) => form.fill(alcoholByVolume.value)
      }
      val adjustmentType= request.userAnswers.get(CurrentAdjustmentEntryPage).flatMap(_.adjustmentType)
      Ok(view(preparedForm, mode, adjustmentType.getOrElse("").toString))/*
    implicit request =>
      val result = for {
        adjustmentEntry <- request.userAnswers.get[AdjustmentEntry](CurrentAdjustmentEntryPage)
        abv <- adjustmentEntry.abv
        preparedForm <- form.fill(abv.value)
        adjustmentType <- adjustmentEntry.adjustmentType
      }yield Ok(view(preparedForm, mode, adjustmentType))
      result.getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))*/
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val adjustmentType= ""
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, adjustmentType))),

        value => {
          val adjustment = request.userAnswers.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CurrentAdjustmentEntryPage, adjustment.copy(abv = Some(AlcoholByVolume(value)))))
            _ <- cacheConnector.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AlcoholByVolumePage, mode, updatedAnswers))
        }
      )
  }
}
