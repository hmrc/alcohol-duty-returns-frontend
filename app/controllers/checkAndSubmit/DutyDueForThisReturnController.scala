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

package controllers.checkAndSubmit

import cats.data.EitherT
import config.Constants.adrReturnCreatedDetails
import connectors.AlcoholDutyReturnsConnector
import controllers.actions._
import models.UserAnswers
import models.audit.AuditReturnSubmitted
import models.checkAndSubmit.AdrReturnSubmission
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.AuditService
import services.checkAndSubmit.AdrReturnSubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.checkAndSubmit.DutyDueForThisReturnHelper
import views.html.checkAndSubmit.DutyDueForThisReturnView

import java.time.{Clock, Instant}
import scala.concurrent.{ExecutionContext, Future}

class DutyDueForThisReturnController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  alcoholDutyReturnsConnector: AlcoholDutyReturnsConnector,
  auditService: AuditService,
  adrReturnSubmissionService: AdrReturnSubmissionService,
  val controllerComponents: MessagesControllerComponents,
  dutyDueForThisReturnHelper: DutyDueForThisReturnHelper,
  clock: Clock,
  view: DutyDueForThisReturnView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    dutyDueForThisReturnHelper
      .getDutyDueViewModel(request.userAnswers)
      .foldF(
        error => {
          logger.warn(error)
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        },
        result => Future.successful(Ok(view(result)))
      )
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val result = for {
      adrReturnSubmission         <-
        adrReturnSubmissionService.getAdrReturnSubmission(request.userAnswers, request.returnPeriod)
      adrSubmissionCreatedDetails <-
        alcoholDutyReturnsConnector.submitReturn(request.appaId, request.returnPeriod.toPeriodKey, adrReturnSubmission)
      _                           <- EitherT.rightT[Future, String](auditReturnSubmitted(request.userAnswers, adrReturnSubmission))
    } yield adrSubmissionCreatedDetails
    result.foldF(
      error => {
        logger.warn(error)
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      },
      adrSubmissionCreatedDetails => {
        logger.warn(s"Successfully submitted return")
        val session =
          request.session + (adrReturnCreatedDetails -> Json.toJson(adrSubmissionCreatedDetails).toString)
        Future.successful(
          Redirect(controllers.checkAndSubmit.routes.ReturnSubmittedController.onPageLoad()).withSession(session)
        )
      }
    )
  }

  private def auditReturnSubmitted(userAnswers: UserAnswers, adrReturnSubmission: AdrReturnSubmission)(implicit
    hc: HeaderCarrier
  ): Unit = {
    val event = AuditReturnSubmitted(userAnswers, adrReturnSubmission, Instant.now(clock))
    auditService.audit(event)
  }
}
