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
import forms.dutySuspended.DutySuspendedFormProvider

import javax.inject.Inject
import models.Mode
import navigation.DeclareDutySuspendedDeliveriesNavigator
import pages.dutySuspended.DutySuspendedWinePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.UserAnswersConnector
import models.AlcoholRegime.Wine
import models.dutySuspended.DutySuspendedVolume
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.dutySuspended.DutySuspendedWineView

import scala.concurrent.{ExecutionContext, Future}

class DutySuspendedWineController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: DeclareDutySuspendedDeliveriesNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkRegime: CheckWineRegimeAction,
  formProvider: DutySuspendedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DutySuspendedWineView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkRegime) {
    implicit request =>
      val form         = formProvider(Wine)
      val preparedForm = request.userAnswers.get(DutySuspendedWinePage)(DutySuspendedVolume.format(Wine)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkRegime).async { implicit request =>
      val form = formProvider(Wine)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(DutySuspendedWinePage, value)(DutySuspendedVolume.format(Wine)))
              _              <- userAnswersConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(DutySuspendedWinePage, mode, updatedAnswers))
        )
    }
}
