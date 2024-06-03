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

package controllers.returns

import controllers.actions._
import forms.returns.DeleteMultipleSPREntryFormProvider

import javax.inject.Inject
import models.{AlcoholRegime, NormalMode}
import pages.returns.{DeleteMultipleSPREntryPage, MultipleSPRListPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.DeleteMultipleSPREntryView

import scala.concurrent.{ExecutionContext, Future}

class DeleteMultipleSPREntryController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DeleteMultipleSPREntryFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DeleteMultipleSPREntryView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(regime: AlcoholRegime, index: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val preparedForm = request.userAnswers.get(DeleteMultipleSPREntryPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, regime, index))
    }

  def onSubmit(regime: AlcoholRegime, index: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, regime, index))),
          value =>
            if (value) {
              for {
                updatedAnswers <-
                  Future.fromTry(request.userAnswers.removeByKeyAndIndex(MultipleSPRListPage, regime, index))
                _              <- cacheConnector.set(updatedAnswers)
              } yield updatedAnswers.getByKey(MultipleSPRListPage, regime) match {
                case Some(list) if list.nonEmpty =>
                  Redirect(controllers.returns.routes.MultipleSPRListController.onPageLoad(regime))
                case _                           =>
                  Redirect(
                    controllers.returns.routes.DoYouHaveMultipleSPRDutyRatesController.onPageLoad(NormalMode, regime)
                  )
              }
            } else {
              Future.successful(Redirect(controllers.returns.routes.MultipleSPRListController.onPageLoad(regime)))
            }
        )
    }
}
