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
import forms.adjustment.AdjustmentRepackagedTaxTypeWithSPRFormProvider
import javax.inject.Inject
import models.Mode
import navigation.AdjustmentNavigator
import pages.adjustment.AdjustmentRepackagedTaxTypeWithSPRPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustment.AdjustmentRepackagedTaxTypeWithSPRView

import scala.concurrent.{ExecutionContext, Future}

class AdjustmentRepackagedTaxTypeWithSPRController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cacheConnector: CacheConnector,
                                       navigator: AdjustmentNavigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: AdjustmentRepackagedTaxTypeWithSPRFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: AdjustmentRepackagedTaxTypeWithSPRView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(AdjustmentRepackagedTaxTypeWithSPRPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AdjustmentRepackagedTaxTypeWithSPRPage, value))
            _              <- cacheConnector.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AdjustmentRepackagedTaxTypeWithSPRPage, mode, updatedAnswers))
      )
  }
}
