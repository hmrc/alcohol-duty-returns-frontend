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

package controllers

import controllers.actions._
import forms.DeclareAlcoholDutyQuestionFormProvider
import javax.inject.Inject
import models.Mode
import navigation.ProductEntryNavigator
import pages.{AlcoholByVolumeQuestionPage, DeclareAlcoholDutyQuestionPage, DraughtReliefQuestionPage, ProductNamePage, SmallProducerReliefQuestionPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DeclareAlcoholDutyQuestionView
import scala.util.Try
import models.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class DeclareAlcoholDutyQuestionController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ProductEntryNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  formProvider: DeclareAlcoholDutyQuestionFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DeclareAlcoholDutyQuestionView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val preparedForm = request.userAnswers.flatMap(_.get(DeclareAlcoholDutyQuestionPage)) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers   <-
              Future.fromTry(
                request.userAnswers.getOrElse(UserAnswers(request.userId)).set(DeclareAlcoholDutyQuestionPage, value)
              )
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
          ProductNamePage,
          AlcoholByVolumeQuestionPage,
          DraughtReliefQuestionPage,
          SmallProducerReliefQuestionPage
        )
      )
    }
}
