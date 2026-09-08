/*
 * Copyright 2026 HM Revenue & Customs
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
import connectors.{AlcoholDutyReturnsConnector, UserAnswersConnector}
import controllers.actions._
import models.requests.OptionalDataRequest
import models.{ErrorModel, ReturnId, ReturnPeriod}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.BeforeStartReturnService
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnAndUserDetails
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswersAuditHelper
import viewmodels.{BeforeStartReturnViewModelFactory, ReturnPeriodViewModelFactory}
import views.html.BeforeStartReturnView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BeforeStartReturnController @Inject() (
  userAnswersConnector: UserAnswersConnector,
  alcoholDutyReturnsConnector: AlcoholDutyReturnsConnector,
  beforeStartReturnService: BeforeStartReturnService,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  userAnswersAuditHelper: UserAnswersAuditHelper,
  clock: Clock,
  config: FrontendAppConfig,
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
        logger.warn("[BeforeStartReturnController] [onPageLoad] Period key is not valid")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case Some(returnPeriod) =>
        val session = request.session + (periodKeySessionKey, periodKey)
        userAnswersConnector.get(request.appaId, periodKey).flatMap {
          case Right(ua)                                    =>
            logger.info(s"[BeforeStartReturnController] [onPageLoad] Return $appaId/$periodKey retrieved by the user")
            beforeStartReturnService.handleExistingUserAnswers(ua).map {
              case Right(_)                            =>
                userAnswersAuditHelper.auditContinueReturn(ua, periodKey, appaId, credentialId, groupId)
                Redirect(controllers.routes.TaskListController.onPageLoad).withSession(session)
              case Left(ErrorModel(CONFLICT, message)) =>
                logger.warn(s"[BeforeStartReturnController] [onPageLoad] Conflict: $message")
                Redirect(controllers.routes.ServiceUpdatedController.onPageLoad).withSession(session)
              case Left(ErrorModel(_, message))        =>
                logger.warn(s"[BeforeStartReturnController] [onPageLoad] Unexpected error: $message")
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
          case Left(error) if error.statusCode == NOT_FOUND =>
            logger.info(s"[BeforeStartReturnController] [onPageLoad] Return $appaId/$periodKey not found")
            alcoholDutyReturnsConnector.shouldAskContactPreference(appaId).value.map {
              case Right(true)  =>
                logger.info(
                  s"[BeforeStartReturnController] [onPageLoad] Redirecting $appaId/$periodKey to set a contact preference"
                )
                Redirect(config.contactPreferencesFrontendPreReturnUrl(periodKey)).withSession(session)
              case Right(false) =>
                beforeYouStartView(returnPeriod, session)
              case Left(err)    =>
                logger.warn(
                  s"[BeforeStartReturnController] [onPageLoad] Unable to check contact preference for $appaId/$periodKey, continuing: $err"
                )
                beforeYouStartView(returnPeriod, session)
            }
          case Left(error) if error.statusCode == LOCKED    =>
            logger.info(s"[BeforeStartReturnController] [onPageLoad] Return $appaId/$periodKey locked for the user")
            Future.successful(Redirect(controllers.routes.ReturnLockedController.onPageLoad()))
          case Left(error)                                  =>
            logger.warn(
              s"[BeforeStartReturnController] [onPageLoad] Error retrieving the return $appaId/$periodKey for the user: $error"
            )
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }
  }

  private def beforeYouStartView(returnPeriod: ReturnPeriod, session: Session)(implicit
    request: Request[_]
  ): Result = {
    val currentDate = LocalDate.now(clock)
    val viewModel   = beforeStartReturnViewModelFactory(returnPeriod, currentDate)
    Ok(view(returnPeriodViewModelFactory(returnPeriod), viewModel)).withSession(session)
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    request.session.get(periodKeySessionKey) match {
      case None            =>
        logger.warn("[BeforeStartReturnController] [onSubmit] Period key not present in session")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case Some(periodKey) =>
        createUserAnswersAndRedirect(periodKey)
    }
  }

  def onContactPreferenceComplete(periodKey: String): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      ReturnPeriod.fromPeriodKey(periodKey) match {
        case None               =>
          logger.warn("[BeforeStartReturnController] [onContactPreferenceComplete] Period key is not valid")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        case Some(returnPeriod) =>
          val session = request.session + (periodKeySessionKey, periodKey)
          Future.successful(beforeYouStartView(returnPeriod, session))
      }
  }

  private def createUserAnswersAndRedirect(
    periodKey: String
  )(implicit request: OptionalDataRequest[_]): Future[Result] = {
    val returnAndUserDetails =
      ReturnAndUserDetails(ReturnId(request.appaId, periodKey), request.groupId, request.userId)
    userAnswersConnector.createUserAnswers(returnAndUserDetails).map {
      case Right(userAnswer) =>
        logger.info(
          s"[BeforeStartReturnController] [createUserAnswersAndRedirect] Return ${request.appaId}/$periodKey created"
        )
        userAnswersAuditHelper.auditReturnStarted(userAnswer)
        Redirect(controllers.routes.TaskListController.onPageLoad)
      case Left(error)       =>
        logger.warn(
          s"[BeforeStartReturnController] [createUserAnswersAndRedirect] Unable to create userAnswers: $error"
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

}
