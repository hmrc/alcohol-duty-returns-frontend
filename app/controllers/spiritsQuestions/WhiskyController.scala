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

package controllers.spiritsQuestions

import controllers.actions._
import forms.spiritsQuestions.WhiskyFormProvider
import javax.inject.Inject
import models.Mode
import navigation.QuarterlySpiritsQuestionsNavigator
import pages.spiritsQuestions.WhiskyPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.spiritsQuestions.WhiskyView

import scala.concurrent.{ExecutionContext, Future}

class WhiskyController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: QuarterlySpiritsQuestionsNavigator,
  authorise: AuthorisedAction,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: WhiskyFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhiskyView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(WhiskyPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen identify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(WhiskyPage, value))
              _              <- cacheConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(WhiskyPage, mode, updatedAnswers))
        )
    }
}
