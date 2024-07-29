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
import forms.returns.DoYouHaveMultipleSPRDutyRatesFormProvider

import javax.inject.Inject
import models.{AlcoholRegime, Mode}
import navigation.ReturnsNavigator
import pages.returns.{DoYouHaveMultipleSPRDutyRatesPage, nextPages}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.DoYouHaveMultipleSPRDutyRatesView

import scala.concurrent.{ExecutionContext, Future}

class DoYouHaveMultipleSPRDutyRatesController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ReturnsNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DoYouHaveMultipleSPRDutyRatesFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DoYouHaveMultipleSPRDutyRatesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ReturnController[Boolean, DoYouHaveMultipleSPRDutyRatesPage.type] {

  val currentPage = DoYouHaveMultipleSPRDutyRatesPage
  val form        = formProvider()

  def onPageLoad(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val preparedForm = request.userAnswers.getByKey(currentPage, regime) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, regime, mode))
    }

  def onSubmit(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, regime, mode))),
          value => {
            val hasChanged   = hasValueChanged(value, regime)
            val pagesToClear = if (hasChanged) nextPages(currentPage) else Seq.empty
            for {
              updatedAnswers     <-
                Future.fromTry(request.userAnswers.setByKey(currentPage, regime, value))
              clearedUserAnswers <- Future.fromTry(updatedAnswers.removePagesByKey(pagesToClear, regime))
              _                  <- cacheConnector.set(clearedUserAnswers)
            } yield Redirect(
              navigator.nextPageWithRegime(currentPage, mode, updatedAnswers, regime, pagesToClear.nonEmpty)
            )
          }
        )
    }
}
