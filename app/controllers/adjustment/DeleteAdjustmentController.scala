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

import connectors.CacheConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.adjustment.DeleteAdjustmentFormProvider
import navigation.AdjustmentNavigator
import pages.adjustment.AdjustmentEntryListPage
import pages.productEntry.DeleteProductPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustment.DeleteAdjustmentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeleteAdjustmentController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DeleteAdjustmentFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DeleteAdjustmentView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(DeleteProductPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

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
                updatedAnswers <- Future.fromTry(request.userAnswers.removeBySeqIndex(AdjustmentEntryListPage, index))
                _              <- cacheConnector.set(updatedAnswers)
              } yield Redirect(controllers.adjustment.routes.AdjustmentListController.onPageLoad())
            } else {
              Future.successful(Redirect(controllers.adjustment.routes.AdjustmentListController.onPageLoad()))
            }
        )
  }
}
