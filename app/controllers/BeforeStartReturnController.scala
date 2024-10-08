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
import connectors.CacheConnector
import controllers.actions._
import models.audit.AuditContinueReturn
import models.{ReturnId, ReturnPeriod, UserAnswers}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.AuditService
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnAndUserDetails
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.returns.ReturnPeriodViewModel
import views.html.BeforeStartReturnView

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BeforeStartReturnController @Inject() (
  cacheConnector: CacheConnector,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  auditService: AuditService,
  clock: Clock,
  val controllerComponents: MessagesControllerComponents,
  view: BeforeStartReturnView
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
        cacheConnector.get(request.appaId, periodKey).map {
          case Right(ua)                                    =>
            logger.info(s"Return $appaId/$periodKey retrieved from cache by the user $credentialId")
            auditContinueReturn(ua, periodKey, appaId, credentialId, groupId)
            Redirect(controllers.routes.TaskListController.onPageLoad).withSession(session)
          case Left(error) if error.statusCode == NOT_FOUND =>
            logger.info(s"Return $appaId/$periodKey not found in cache")
            Ok(view(ReturnPeriodViewModel(returnPeriod))).withSession(session)
          case Left(error) if error.statusCode == LOCKED    =>
            logger.warn(s"Return ${request.appaId}/$periodKey locked for the user $credentialId")
            Redirect(controllers.routes.ReturnLockedController.onPageLoad())
          case _                                            =>
            logger.warn(s"Error retrieving the return $appaId/$periodKey from the cache for the user $credentialId")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    request.session.get(periodKeySessionKey) match {
      case None            =>
        logger.warn("Period key not present in session")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case Some(periodKey) =>
        val returnAndUserDetails =
          ReturnAndUserDetails(ReturnId(request.appaId, periodKey), request.groupId, request.userId)
        cacheConnector.createUserAnswers(returnAndUserDetails).map { response =>
          if (response.status != CREATED) {
            logger.warn(s"Unable to create userAnswers, response status: ${response.status}")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          } else {
            logger.info(s"Return ${request.appaId}/$periodKey created")
            Redirect(controllers.routes.TaskListController.onPageLoad)
          }
        }
    }
  }
  // TODO : change returnStartedTime to start time in future

  private def auditContinueReturn(
    userAnswers: UserAnswers,
    periodKey: String,
    appaId: String,
    credentialId: String,
    groupId: String
  )(implicit
    hc: HeaderCarrier
  ): Unit = {
    val returnContinueTime = Instant.now(clock)
    val eventDetail        = AuditContinueReturn(
      appaId = appaId,
      periodKey = periodKey,
      credentialId = credentialId,
      groupId = groupId,
      returnContinueTime = returnContinueTime,
      returnStartedTime = userAnswers.lastUpdated,
      returnValidUntilTime = userAnswers.validUntil
    )
    auditService.audit(eventDetail)
  }

}
