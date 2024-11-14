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

package controllers.dutySuspended

import controllers.actions._
import forms.dutySuspended.DutySuspendedOtherFermentedFormProvider

import javax.inject.Inject
import models.Mode
import navigation.DeclareDutySuspendedDeliveriesNavigator
import pages.dutySuspended.DutySuspendedOtherFermentedPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.UserAnswersConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.dutySuspended.DutySuspendedOtherFermentedView

import scala.concurrent.{ExecutionContext, Future}

class DutySuspendedOtherFermentedController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: DeclareDutySuspendedDeliveriesNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkRegime: CheckOtherFermentedRegimeAction,
  formProvider: DutySuspendedOtherFermentedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DutySuspendedOtherFermentedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkRegime) {
    implicit request =>
      val preparedForm = request.userAnswers.get(DutySuspendedOtherFermentedPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkRegime).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(DutySuspendedOtherFermentedPage, value))
              _              <- userAnswersConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(DutySuspendedOtherFermentedPage, mode, updatedAnswers))
        )
    }
}
