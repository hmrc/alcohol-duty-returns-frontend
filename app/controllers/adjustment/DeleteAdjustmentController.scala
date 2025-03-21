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

import connectors.UserAnswersConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifyWithEnrolmentAction}
import forms.adjustment.DeleteAdjustmentFormProvider
import models.{NormalMode, UserAnswers}
import navigation.AdjustmentNavigator
import pages.adjustment.{AdjustmentEntryListPage, DeclareAdjustmentQuestionPage, DeleteAdjustmentPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustment.AdjustmentOverUnderDeclarationCalculationHelper
import views.html.adjustment.DeleteAdjustmentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DeleteAdjustmentController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DeleteAdjustmentFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DeleteAdjustmentView,
  adjustmentOverUnderDeclarationCalculationHelper: AdjustmentOverUnderDeclarationCalculationHelper
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(DeleteAdjustmentPage).fold(form)(value => form.fill(value))
    Ok(view(preparedForm, index))
  }

  def onSubmit(index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, index))),
          value =>
            if (value) {
              for {
                updatedAnswers                        <- Future.fromTry(request.userAnswers.removeBySeqIndex(AdjustmentEntryListPage, index))
                userAnswersWithUpdatedOverUnderReason <-
                  adjustmentOverUnderDeclarationCalculationHelper.fetchOverUnderDeclarationTotals(updatedAnswers)
                userAnswersDeclarationUpdate          <- Future.fromTry(emptyListCheck(userAnswersWithUpdatedOverUnderReason))
                _                                     <- userAnswersConnector.set(userAnswersDeclarationUpdate)
              } yield Redirect(
                navigator.nextPage(DeleteAdjustmentPage, NormalMode, userAnswersDeclarationUpdate, Some(true))
              )
            } else {
              Future.successful(
                Redirect(navigator.nextPage(DeleteAdjustmentPage, NormalMode, request.userAnswers, Some(true)))
              )
            }
        )
  }

  private def emptyListCheck(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.get(pages.adjustment.AdjustmentEntryListPage) match {
      case Some(list) if list.nonEmpty => Try(userAnswers)
      case _                           => userAnswers.remove(DeclareAdjustmentQuestionPage)
    }

}
