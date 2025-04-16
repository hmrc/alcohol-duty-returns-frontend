/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.dutySuspendedNew

import connectors.UserAnswersConnector
import controllers.actions._
import forms.dutySuspendedNew.DutySuspendedQuantitiesFormProvider
import models.{AlcoholRegime, Mode}
import navigation.DutySuspendedNavigator
import pages.dutySuspendedNew.DutySuspendedQuantitiesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.dutySuspendedNew.DutySuspendedQuantitiesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DutySuspendedQuantitiesController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: DutySuspendedNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkDSDNewJourneyToggle: CheckDSDNewJourneyToggleAction,
  formProvider: DutySuspendedQuantitiesFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DutySuspendedQuantitiesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkDSDNewJourneyToggle) { implicit request =>
      val form         = formProvider(regime)
      val preparedForm = request.userAnswers.getByKey(DutySuspendedQuantitiesPage, regime) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, regime, mode))
    }

  def onSubmit(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkDSDNewJourneyToggle).async { implicit request =>
      val form = formProvider(regime)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, regime, mode))),
          value => Future.successful(Ok("submitted"))
//            for {
//              updatedAnswers <- Future.fromTry(request.userAnswers.set(DutySuspendedBeerPage, value))
//              _              <- userAnswersConnector.set(updatedAnswers)
//            } yield Redirect(navigator.nextPage(DutySuspendedBeerPage, mode, updatedAnswers))
        )
    }
}
