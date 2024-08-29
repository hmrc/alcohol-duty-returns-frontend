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

package controllers.adjustment

import controllers.actions._
import forms.adjustment.DeclareAdjustmentQuestionFormProvider

import javax.inject.Inject
import models.{Mode, UserAnswers}
import navigation.AdjustmentNavigator
import pages.adjustment.{AdjustmentEntryListPage, AdjustmentListPage, AdjustmentTotalPage, CurrentAdjustmentEntryPage, DeclareAdjustmentQuestionPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustment.DeclareAdjustmentQuestionView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DeclareAdjustmentQuestionController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DeclareAdjustmentQuestionFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DeclareAdjustmentQuestionView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(DeclareAdjustmentQuestionPage) match {
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
              updatedAnswers   <- Future.fromTry(request.userAnswers.set(DeclareAdjustmentQuestionPage, value))
              filterUserAnswer <- Future.fromTry(filterAdjustmentQuestionAnswer(updatedAnswers, value))
              _                <- cacheConnector.set(filterUserAnswer)
            } yield Redirect(navigator.nextPage(DeclareAdjustmentQuestionPage, mode, filterUserAnswer))
        )
  }

  private def filterAdjustmentQuestionAnswer(userAnswer: UserAnswers, value: Boolean): Try[UserAnswers] =
    if (value) {
      Try(userAnswer)
    } else {
      userAnswer.remove(
        List(AdjustmentEntryListPage, AdjustmentListPage, CurrentAdjustmentEntryPage, AdjustmentTotalPage)
      )
    }
}
