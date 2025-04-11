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

package controllers.dutySuspendedNew

import connectors.UserAnswersConnector
import controllers.actions._
import forms.dutySuspendedNew.DeclareDutySuspenseQuestionFormProvider
import models.{Mode, UserAnswers}
import navigation.DutySuspendedNavigator
import pages.dutySuspendedNew.{DeclareDutySuspenseQuestionPage, DutySuspendedAlcoholTypePage}
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
              updatedAnswers                <-
                Future.fromTry(
                  request.userAnswers.set(DeclareDutySuspenseQuestionPage, value)
                )
              singleRegimeUpdatedUserAnswer <- Future.fromTry(checkIfOneRegimeAndUpdateUserAnswer(updatedAnswers))
              filterUserAnswer              <- Future.fromTry(filterDSDQuestionAnswer(singleRegimeUpdatedUserAnswer, value))
              _                             <- userAnswersConnector.set(filterUserAnswer)
            } yield Redirect(
              navigator.nextPage(DeclareDutySuspenseQuestionPage, mode, filterUserAnswer, Some(false))
            )
        )
    }

  private def checkIfOneRegimeAndUpdateUserAnswer(userAnswer: UserAnswers): Try[UserAnswers] =
    if (userAnswer.regimes.regimes.size == 1) {
      userAnswer.set(DutySuspendedAlcoholTypePage, userAnswer.regimes.regimes)
    } else {
      Try(userAnswer)
    }

  private def filterDSDQuestionAnswer(userAnswer: UserAnswers, value: Boolean): Try[UserAnswers] =
    if (value) {
      Try(userAnswer)
    } else {
      // TODO: add other pages from new journey to be removed if user submits 'No'
      userAnswer.remove(
        List(
          DutySuspendedAlcoholTypePage
        )
      )
    }
}
