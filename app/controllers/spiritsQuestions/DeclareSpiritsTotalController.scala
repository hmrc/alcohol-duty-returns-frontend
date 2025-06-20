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

package controllers.spiritsQuestions

import config.FrontendAppConfig
import connectors.UserAnswersConnector
import controllers.actions._
import forms.spiritsQuestions.DeclareSpiritsTotalFormProvider

import javax.inject.Inject
import models.Mode
import navigation.QuarterlySpiritsQuestionsNavigator
import pages.spiritsQuestions.DeclareSpiritsTotalPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.spiritsQuestions.DeclareSpiritsTotalView

import scala.concurrent.{ExecutionContext, Future}

class DeclareSpiritsTotalController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: QuarterlySpiritsQuestionsNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DeclareSpiritsTotalFormProvider,
  checkSpiritsRegime: CheckSpiritsRegimeAction,
  val controllerComponents: MessagesControllerComponents,
  view: DeclareSpiritsTotalView,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkSpiritsRegime) { implicit request =>
      val preparedForm = request.userAnswers.get(DeclareSpiritsTotalPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      val declareSpiritsGuidanceUrl = appConfig.declareSpiritsGuidanceUrl

      Ok(view(preparedForm, mode, declareSpiritsGuidanceUrl))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkSpiritsRegime).async { implicit request =>
      val declareSpiritsGuidanceUrl = appConfig.declareSpiritsGuidanceUrl

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, declareSpiritsGuidanceUrl))),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(DeclareSpiritsTotalPage, value))
              _              <- userAnswersConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(DeclareSpiritsTotalPage, mode, updatedAnswers, Some(true)))
        )
    }
}
