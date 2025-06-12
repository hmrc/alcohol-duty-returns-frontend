/*
 * Copyright 2025 HM Revenue & Customs
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

import connectors.UserAnswersConnector
import controllers.actions._
import forms.dutySuspended.DeclareDutySuspenseQuestionFormProvider
import models.{Mode, UserAnswers}
import navigation.DutySuspendedNavigator
import pages.dutySuspended.{DeclareDutySuspenseQuestionPage, DutySuspendedAlcoholTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.dutySuspendedNew.DeclareDutySuspenseQuestionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DeclareDutySuspenseQuestionController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: DutySuspendedNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkDSDNewJourneyToggle: CheckDSDNewJourneyToggleAction,
  formProvider: DeclareDutySuspenseQuestionFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DeclareDutySuspenseQuestionView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkDSDNewJourneyToggle) { implicit request =>
      val preparedForm = request.userAnswers.get(DeclareDutySuspenseQuestionPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkDSDNewJourneyToggle).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              singleRegimeUpdatedUserAnswer <- Future.fromTry(checkIfOneRegimeAndUpdateUserAnswer(request.userAnswers))
              updatedAnswers                <-
                Future.fromTry(singleRegimeUpdatedUserAnswer.set(DeclareDutySuspenseQuestionPage, value))
              _                             <- userAnswersConnector.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(DeclareDutySuspenseQuestionPage, mode, updatedAnswers, Some(false))
            )
        )
    }

  private def checkIfOneRegimeAndUpdateUserAnswer(userAnswer: UserAnswers): Try[UserAnswers] =
    if (userAnswer.regimes.regimes.size == 1) {
      userAnswer.set(DutySuspendedAlcoholTypePage, userAnswer.regimes.regimes)
    } else {
      Try(userAnswer)
    }
}
