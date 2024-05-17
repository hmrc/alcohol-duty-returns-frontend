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

import connectors.CacheConnector
import controllers.actions._
import forms.returns.DeclareAlcoholDutyQuestionFormProvider
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.{AlcoholRegime, Mode, UserAnswers}
import navigation.ReturnsNavigator
import pages.returns.{DeclareAlcoholDutyQuestionPage, HowMuchDoYouNeedToDeclarePage, WhatDoYouNeedToDeclarePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.DeclareAlcoholDutyQuestionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DeclareAlcoholDutyQuestionController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ReturnsNavigator,
  identify: IdentifierAction,
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
    val regimes: Seq[AlcoholRegime] = Seq(Beer, Cider, Spirits, Wine, OtherFermentedProduct)

    val preparedForm = request.userAnswers.get(DeclareAlcoholDutyQuestionPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    val showCider = regimes.contains(Cider)
    Ok(view(preparedForm, showCider, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val regimes: Seq[AlcoholRegime] = Seq(Beer, Cider, Spirits, Wine, OtherFermentedProduct)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, regimes.contains(Cider), mode))),
          value =>
            for {
              updatedAnswers   <- Future.fromTry(request.userAnswers.set(DeclareAlcoholDutyQuestionPage, value))
              filterUserAnswer <- Future.fromTry(filterAlcoholDutyQuestionAnswer(updatedAnswers, value))
              _                <- cacheConnector.set(filterUserAnswer)
            } yield Redirect(navigator.nextPage(DeclareAlcoholDutyQuestionPage, mode, filterUserAnswer))
        )
  }

  def filterAlcoholDutyQuestionAnswer(userAnswer: UserAnswers, value: Boolean): Try[UserAnswers] =
    if (value) {
      Try(userAnswer)
    } else {
      userAnswer.remove(
        List(
          WhatDoYouNeedToDeclarePage,
          HowMuchDoYouNeedToDeclarePage
        )
      )
    }
}
