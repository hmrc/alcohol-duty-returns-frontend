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
import controllers.actions._
import models.{ErrorModel, ReturnId, ReturnPeriod}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.BeforeStartReturnService
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnAndUserDetails
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswersAuditHelper
import viewmodels.{BeforeStartReturnViewModelFactory, ReturnPeriodViewModelFactory}
import views.html.BeforeStartReturnView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BeforeStartReturnController @Inject() (
  userAnswersConnector: UserAnswersConnector,
  beforeStartReturnService: BeforeStartReturnService,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  userAnswersAuditHelper: UserAnswersAuditHelper,
  clock: Clock,
  val controllerComponents: MessagesControllerComponents,
  view: BeforeStartReturnView,
  beforeStartReturnViewModelFactory: BeforeStartReturnViewModelFactory,
  returnPeriodViewModelFactory: ReturnPeriodViewModelFactory
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(periodKey: String): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    val appaId       = request.appaId
    val credentialId = request.userId
    val groupId      = request.groupId

    ReturnPeriod.fromPeriodKey(periodKey) match {
      case None               =>
        logger.warn("Period key is not valid")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case Some(returnPeriod) =>
        val session = request.session + (periodKeySessionKey, periodKey)
        userAnswersConnector.get(request.appaId, periodKey).flatMap {
          case Right(ua)   =>
            logger.info(s"Return $appaId/$periodKey retrieved by the user")
            beforeStartReturnService.handleExistingUserAnswers(ua).map {
              case Right(_)                            =>
                userAnswersAuditHelper.auditContinueReturn(ua, periodKey, appaId, credentialId, groupId)
                Redirect(controllers.routes.TaskListController.onPageLoad).withSession(session)
              case Left(ErrorModel(CONFLICT, message)) =>
                logger.warn(s"Conflict: $message")
                Redirect(controllers.routes.ServiceUpdatedController.onPageLoad).withSession(session)
              case Left(ErrorModel(_, message))        =>
                logger.warn(s"Unexpected error: $message")
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
          case Left(error) =>
            Future.successful(handleGetUserAnswersError(appaId, periodKey, returnPeriod, session, error))
        }
    }
  }

  private def handleGetUserAnswersError(
    appaId: String,
    periodKey: String,
    returnPeriod: ReturnPeriod,
    session: Session,
    error: UpstreamErrorResponse
  )(implicit request: Request[_]): Result = error match {
    case err if err.statusCode == NOT_FOUND =>
      logger.info(s"Return $appaId/$periodKey not found")
      val currentDate = LocalDate.now(clock)
      val viewModel   = beforeStartReturnViewModelFactory(returnPeriod, currentDate)
      Ok(view(returnPeriodViewModelFactory(returnPeriod), viewModel)).withSession(session)
    case err if err.statusCode == LOCKED    =>
      logger.warn(s"Return $appaId/$periodKey locked for the user")
      Redirect(controllers.routes.ReturnLockedController.onPageLoad())
    case _                                  =>
      logger.warn(s"Error retrieving the return $appaId/$periodKey for the user")
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
  }

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
            userAnswersAuditHelper.auditReturnStarted(userAnswer)
            Redirect(controllers.routes.TaskListController.onPageLoad)
          case Left(error)       =>
            logger.warn(s"Unable to create userAnswers: $error")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }
  }

}
