/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import connectors.CacheConnector
import controllers.actions._
import forms.DeclareDutySuspendedDeliveriesQuestionFormProvider

import javax.inject.Inject
import models.Mode
import navigation.{DeclareDutySuspendedDeliveriesNavigator, Navigator}
import pages.DeclareDutySuspendedDeliveriesQuestionPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DeclareDutySuspendedDeliveriesQuestionView

import scala.concurrent.{ExecutionContext, Future}

class DeclareDutySuspendedDeliveriesQuestionController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: DeclareDutySuspendedDeliveriesNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DeclareDutySuspendedDeliveriesQuestionFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DeclareDutySuspendedDeliveriesQuestionView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(DeclareDutySuspendedDeliveriesQuestionPage) match {
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
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(
                  request.userAnswers.set(DeclareDutySuspendedDeliveriesQuestionPage, value)
                )
              _              <- cacheConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(DeclareDutySuspendedDeliveriesQuestionPage, mode, updatedAnswers))
        )
  }
}
