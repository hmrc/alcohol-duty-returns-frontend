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
import forms.returns.AlcoholTypeFormProvider

import javax.inject.Inject
import models.{AlcoholRegime, Mode, UserAnswers}
import navigation.ReturnsNavigator
import pages.returns.{AlcoholTypePage, nextPages}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.AlcoholTypeView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AlcoholTypeController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ReturnsNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AlcoholTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AlcoholTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(AlcoholTypePage) match {
      case None        => form
      case Some(value) => form.fill(value.map(_.entryName))
    }

    Ok(view(preparedForm, mode, request.userAnswers.regimes))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userAnswers.regimes))),
          value => {
            val alcoholRegimes: Set[AlcoholRegime]  = value.flatMap(AlcoholRegime.fromString)
            val regimesToRemove: Set[AlcoholRegime] = request.userAnswers.regimes.regimes.diff(alcoholRegimes)
            for {
              updatedAnswers                <- Future.fromTry(request.userAnswers.set(AlcoholTypePage, alcoholRegimes))
              userAnswersWithUpdatedRegimes <- Future.fromTry(clearUserAnswers(updatedAnswers, regimesToRemove))
              _                             <- cacheConnector.set(userAnswersWithUpdatedRegimes)
            } yield Redirect(navigator.nextPage(AlcoholTypePage, mode, userAnswersWithUpdatedRegimes))
          }
        )
  }

  private def clearUserAnswers(userAnswers: UserAnswers, regimes: Set[AlcoholRegime]): Try[UserAnswers] = {
    val pagesToClear = nextPages(AlcoholTypePage)
    regimes.foldLeft(Try(userAnswers)) { (currentUserAnswers, regime) =>
      currentUserAnswers.flatMap(currentAnswers => currentAnswers.removePagesByKey(pagesToClear, regime))
    }
  }
}
