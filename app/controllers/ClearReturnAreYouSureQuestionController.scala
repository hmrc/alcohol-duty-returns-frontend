/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.UserAnswersConnector
import controllers.actions._
import forms.ClearReturnAreYouSureQuestionFormProvider
import models.ReturnId
import models.requests.DataRequest
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnAndUserDetails
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ClearReturnAreYouSureQuestionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ClearReturnAreYouSureQuestionController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ClearReturnAreYouSureQuestionFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ClearReturnAreYouSureQuestionView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value => clearUserAnswers(value, request.appaId, request.returnPeriod.toPeriodKey)
      )
  }

  private def clearUserAnswers(shouldClear: Boolean, appaId: String, periodKey: String)(implicit
    request: DataRequest[AnyContent]
  ): Future[Result] =
    if (shouldClear) {
      userAnswersConnector.delete(appaId, periodKey).transformWith {
        case Success(httpResponse) if httpResponse.status == OK =>
          recreateUserAnswers(appaId, periodKey)
        case Success(httpResponse)                              =>
          logger.warn(s"Unable to clear user answers: $appaId/$periodKey: ${httpResponse.status} ${httpResponse.body}")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        case Failure(e)                                         =>
          logger.warn(s"Unable to clear user answers: $appaId/$periodKey", e)
          Future.failed(e)
      }
    } else {
      Future.successful(Redirect(routes.TaskListController.onPageLoad.url))
    }

  private def recreateUserAnswers(appaId: String, periodKey: String)(implicit
    request: DataRequest[AnyContent]
  ): Future[Result] = {
    val returnAndUserDetails =
      ReturnAndUserDetails(ReturnId(appaId, periodKey), request.groupId, request.userId)
    userAnswersConnector.createUserAnswers(returnAndUserDetails).map {
      case Right(_)    =>
        logger.info(s"Return $appaId/$periodKey recreated")
        Redirect(routes.TaskListController.onPageLoad.url)
      case Left(error) =>
        logger.warn(s"Unable to recreate userAnswers: $error")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
