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

import config.Constants.periodKeySessionKey
import connectors.UserAnswersConnector
import controllers.actions.{DataRetrievalAction, IdentifyWithEnrolmentAction}
import models.ReturnId
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnAndUserDetails
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswersAuditHelper
import views.html.ServiceUpdatedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ServiceUpdatedController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  userAnswersConnector: UserAnswersConnector,
  userAnswersAuditHelper: UserAnswersAuditHelper,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  view: ServiceUpdatedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData) { implicit request =>
    Ok(view()).withSession(request.session)
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    request.session.get(periodKeySessionKey) match {
      case None            =>
        logger.warn("[ServiceUpdatedController] [onSubmit] Period key not present in session")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case Some(periodKey) =>
        val returnId             = ReturnId(request.appaId, periodKey)
        val returnAndUserDetails = ReturnAndUserDetails(returnId, request.groupId, request.userId)
        val newUserAnswers       = for {
          _          <- userAnswersConnector.delete(request.appaId, periodKey)
          _          <- userAnswersConnector.releaseLock(returnId)
          newAnswers <- userAnswersConnector.createUserAnswers(returnAndUserDetails)
        } yield newAnswers
        newUserAnswers.map {
          case Right(userAnswer) =>
            logger.info(s"[ServiceUpdatedController] [onSubmit] Return ${request.appaId}/$periodKey created")
            userAnswersAuditHelper.auditReturnStarted(userAnswer)
            Redirect(controllers.routes.TaskListController.onPageLoad)
          case Left(error)       =>
            logger.warn(s"[ServiceUpdatedController] [onSubmit] Unable to create new userAnswers: $error")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }
  }

}
