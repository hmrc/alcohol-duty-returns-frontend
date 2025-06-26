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

package controllers.declareDuty

import connectors.UserAnswersConnector
import controllers.actions._
import forms.declareDuty.DeclareAlcoholDutyQuestionFormProvider
import models.AlcoholRegime.{Cider, OtherFermentedProduct}
import models.{Mode, UserAnswers}
import navigation.ReturnsNavigator
import pages.declareDuty.{AlcoholTypePage, DeclareAlcoholDutyQuestionPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.declareDuty.DeclareAlcoholDutyQuestionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DeclareAlcoholDutyQuestionController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: ReturnsNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DeclareAlcoholDutyQuestionFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DeclareAlcoholDutyQuestionView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val approvedRegimes = request.userAnswers.regimes.regimes

    val preparedForm = request.userAnswers.get(DeclareAlcoholDutyQuestionPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    val showSparklingCider = approvedRegimes.contains(Cider) || approvedRegimes.contains(OtherFermentedProduct)
    Ok(view(preparedForm, showSparklingCider, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            val approvedRegimes    = request.userAnswers.regimes.regimes
            val showSparklingCider = approvedRegimes.contains(Cider) || approvedRegimes.contains(OtherFermentedProduct)
            Future.successful(BadRequest(view(formWithErrors, showSparklingCider, mode)))
          },
          value =>
            for {
              singleRegimeUpdatedUserAnswer <- Future.fromTry(checkIfOneRegimeAndUpdateUserAnswer(request.userAnswers))
              updatedAnswers                <- Future.fromTry(singleRegimeUpdatedUserAnswer.set(DeclareAlcoholDutyQuestionPage, value))
              _                             <- userAnswersConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(DeclareAlcoholDutyQuestionPage, mode, updatedAnswers, Some(false)))
        )
  }

  private def checkIfOneRegimeAndUpdateUserAnswer(userAnswer: UserAnswers): Try[UserAnswers] =
    if (userAnswer.regimes.regimes.size == 1) {
      userAnswer.set(AlcoholTypePage, userAnswer.regimes.regimes)
    } else {
      Try(userAnswer)
    }
}
