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
import forms.spiritsQuestions.GrainsUsedFormProvider

import javax.inject.Inject
import models.{Mode, UserAnswers}
import navigation.QuarterlySpiritsQuestionsNavigator
import pages.spiritsQuestions.{GrainsUsedPage, OtherMaltedGrainsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.spiritsQuestions.GrainsUsedView

import scala.concurrent.{ExecutionContext, Future}

class GrainsUsedController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: QuarterlySpiritsQuestionsNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkSpiritsRegime: CheckSpiritsRegimeAction,
  checkSpiritsAndIngredientsToggle: CheckSpiritsAndIngredientsToggleAction,
  formProvider: GrainsUsedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: GrainsUsedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkSpiritsRegime andThen checkSpiritsAndIngredientsToggle) {
      implicit request =>
        val preparedForm = request.userAnswers.get(GrainsUsedPage) match {
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
            value => {
              val (intermediateAnswers, maltedGrainNotBarleyNowSelected) =
                handleOtherMaltedGrainsChange(request.userAnswers, value.usedMaltedGrainNotBarley)
              for {
                updatedAnswers <- Future.fromTry(intermediateAnswers.set(GrainsUsedPage, value))
                _              <- cacheConnector.set(updatedAnswers)
              } yield Redirect(
                navigator.nextPage(GrainsUsedPage, mode, updatedAnswers, maltedGrainNotBarleyNowSelected)
              )
            }
          )
      }

  def isNowSelected(oldValue: Boolean, newValue: Boolean) = !oldValue && newValue

  def isNowUnselected(oldValue: Boolean, newValue: Boolean) = oldValue && !newValue

  def handleOtherMaltedGrainsChange(
    answers: UserAnswers,
    newValue: Boolean
  ): (UserAnswers, Boolean) = {
    val oldValue = answers.get(GrainsUsedPage).exists(_.usedMaltedGrainNotBarley)
    if (isNowSelected(oldValue, newValue)) {
      (answers, true)
    } else if (isNowUnselected(oldValue, newValue)) {
      (answers.remove(OtherMaltedGrainsPage).get, false)
    } else {
      (answers, false)
    }
  }
}
