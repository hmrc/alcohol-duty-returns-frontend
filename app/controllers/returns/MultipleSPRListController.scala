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
import forms.returns.MultipleSPRListFormProvider

import javax.inject.Inject
import models.{AlcoholRegime, Mode}
import navigation.ReturnsNavigator
import pages.returns.DoYouWantToAddMultipleSPRToListPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returns.MultipleSPRListHelper
import views.html.returns.MultipleSPRListView

import scala.concurrent.{ExecutionContext, Future}

class MultipleSPRListController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ReturnsNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: MultipleSPRListFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: MultipleSPRListView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val preparedForm = request.userAnswers.get(DoYouWantToAddMultipleSPRToListPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      MultipleSPRListHelper
        .sprTableViewModel(request.userAnswers, regime)
        .fold(
          error => {
            logger.warn(s"Failed to create SPR table: $error")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          },
          sprTable => Ok(view(preparedForm, mode, regime, sprTable))
        )
    }

  def onSubmit(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            MultipleSPRListHelper
              .sprTableViewModel(request.userAnswers, regime)
              .fold(
                error => {
                  logger.warn(s"Failed to create SPR table: $error")
                  Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                },
                sprTable => Future.successful(BadRequest(view(formWithErrors, mode, regime, sprTable)))
              ),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(DoYouWantToAddMultipleSPRToListPage, value))
              _              <- cacheConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(DoYouWantToAddMultipleSPRToListPage, mode, updatedAnswers))
        )
    }
}
