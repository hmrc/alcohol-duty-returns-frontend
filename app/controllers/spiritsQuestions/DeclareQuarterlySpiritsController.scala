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

import connectors.UserAnswersConnector
import controllers.actions._
import forms.spiritsQuestions.DeclareQuarterlySpiritsFormProvider
import models.{Mode, UserAnswers}
import navigation.QuarterlySpiritsQuestionsNavigator
import pages.spiritsQuestions._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.spiritsQuestions.DeclareQuarterlySpiritsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class DeclareQuarterlySpiritsController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: QuarterlySpiritsQuestionsNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkSpiritsRegime: CheckSpiritsRegimeAction,
  checkSpiritsAndIngredientsToggle: CheckSpiritsAndIngredientsToggleAction,
  formProvider: DeclareQuarterlySpiritsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DeclareQuarterlySpiritsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkSpiritsRegime andThen checkSpiritsAndIngredientsToggle) {
      implicit request =>
        val preparedForm = request.userAnswers.get(DeclareQuarterlySpiritsPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkSpiritsRegime andThen checkSpiritsAndIngredientsToggle)
      .async { implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
            value =>
              for {
                updatedAnswers      <- Future.fromTry(request.userAnswers.set(DeclareQuarterlySpiritsPage, value))
                maybeClearedAnswers <- Future.fromTry(clearUserAnswersWhenNoSelected(updatedAnswers, value))
                _                   <- userAnswersConnector.set(maybeClearedAnswers)
              } yield Redirect(navigator.nextPage(DeclareQuarterlySpiritsPage, mode, updatedAnswers))
          )
      }

  private def clearUserAnswersWhenNoSelected(userAnswers: UserAnswers, value: Boolean): Try[UserAnswers] =
    if (value) {
      Success(userAnswers)
    } else {
      userAnswers.remove(
        List(
          DeclareSpiritsTotalPage,
          SpiritTypePage,
          WhiskyPage
        )
      )
    }
}
