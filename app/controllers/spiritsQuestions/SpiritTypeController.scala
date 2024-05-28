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
import forms.spiritsQuestions.SpiritTypeFormProvider

import javax.inject.Inject
import models.{Mode, SpiritType, UserAnswers}
import navigation.QuarterlySpiritsQuestionsNavigator
import pages.spiritsQuestions.{OtherSpiritsProducedPage, SpiritTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.spiritsQuestions.SpiritTypeView

import scala.concurrent.{ExecutionContext, Future}

class SpiritTypeController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: QuarterlySpiritsQuestionsNavigator,
  authorise: AuthorisedAction,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SpiritTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SpiritTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(SpiritTypePage) match {
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
          value => {
            val (intermediateAnswers, otherSpiritsNowSelected) =
              handleOtherSpiritsChange(request.userAnswers, SpiritTypePage.hasMadeOtherSpirits(value))
            for {
              updatedAnswers <- Future.fromTry(intermediateAnswers.set(SpiritTypePage, value))
              _              <- cacheConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(SpiritTypePage, mode, updatedAnswers, otherSpiritsNowSelected))
          }
        )
    }

  def isNowSelected(oldValue: Boolean, newValue: Boolean) = !oldValue && newValue

  def isNowUnselected(oldValue: Boolean, newValue: Boolean) = oldValue && !newValue

  def handleOtherSpiritsChange(
    answers: UserAnswers,
    newValue: Boolean
  ): (UserAnswers, Boolean) = {
    val oldValue = SpiritTypePage.hasMadeOtherSpirits(answers.get(SpiritTypePage).getOrElse(Set.empty[SpiritType]))
    if (isNowSelected(oldValue, newValue)) {
      (answers, true)
    } else if (isNowUnselected(oldValue, newValue)) {
      (answers.remove(OtherSpiritsProducedPage).get, false)
    } else {
      (answers, false)
    }
  }

}
