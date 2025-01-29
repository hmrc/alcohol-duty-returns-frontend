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
import config.FrontendAppConfig
import connectors.UserAnswersConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifyWithEnrolmentAction}
import models.audit.{AuditObligationData, AuditReturnStarted}
import models.{ObligationData, ReturnId, UserAnswers}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.AuditService
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnAndUserDetails
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.tasklist.TaskListViewModel
import views.html.ServiceUpdatedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ServiceUpdatedController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  userAnswersConnector: UserAnswersConnector,
  auditService: AuditService,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  view: ServiceUpdatedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData) { implicit request =>
    Ok(view())
  }

  // same as BeforeStartReturnController
  def onSubmit(): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    request.session.get(periodKeySessionKey) match {
      case None            =>
        logger.warn("Period key not present in session")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case Some(periodKey) =>
        val returnAndUserDetails =
          ReturnAndUserDetails(ReturnId(request.appaId, periodKey), request.groupId, request.userId)
        userAnswersConnector.createUserAnswers(returnAndUserDetails).map {
          case Right(userAnswer) =>
            logger.info(s"Return ${request.appaId}/$periodKey created")
            auditReturnStarted(userAnswer)
            Redirect(controllers.routes.TaskListController.onPageLoad)
          case Left(error)       =>
            logger.warn(s"Unable to create userAnswers: $error")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }
  }

  private def auditReturnStarted(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier): Unit =
    userAnswers.get(ObligationData) match {
      case Some(obligationData) =>
        val auditReturnStarted = AuditReturnStarted(
          appaId = userAnswers.returnId.appaId,
          periodKey = userAnswers.returnId.periodKey,
          credentialId = userAnswers.internalId,
          groupId = userAnswers.groupId,
          obligationData = AuditObligationData(obligationData),
          returnStartedTime = userAnswers.startedTime,
          returnValidUntilTime = userAnswers.validUntil
        )
        auditService.audit(auditReturnStarted)
      case None                 => logger.warn("Impossible to create Return Started Audit Event, unable to retrieve obligation data")
    }
}
