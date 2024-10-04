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

import config.Constants.adrReturnCreatedDetails
import config.FrontendAppConfig
import connectors.AlcoholDutyReturnsConnector
import controllers.actions._
import models.{ReturnPeriod, UserAnswers}
import models.audit.AuditReturnSubmitted
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

import scala.concurrent.{ExecutionContext, Future}

class DutyDueForThisReturnController @Inject() (
  override val messagesApi: MessagesApi,
  appConfig: FrontendAppConfig,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  alcoholDutyReturnsConnector: AlcoholDutyReturnsConnector,
  auditService: AuditService,
  adrReturnSubmissionService: AdrReturnSubmissionService,
  val controllerComponents: MessagesControllerComponents,
  dutyDueForThisReturnHelper: DutyDueForThisReturnHelper,
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
    } yield adrSubmissionCreatedDetails

    result.foldF(
      error => {
        logger.warn(error)
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      },
      adrSubmissionCreatedDetails => {
        logger.warn(s"Successfully submitted return: $adrSubmissionCreatedDetails")
        val session =
          request.session + (adrReturnCreatedDetails -> Json.toJson(adrSubmissionCreatedDetails).toString)
        auditEvent(request.userAnswers, request.returnPeriod)
        Future.successful(
          Redirect(controllers.checkAndSubmit.routes.ReturnSubmittedController.onPageLoad()).withSession(session)
        )
      }
    )
  }

  def auditEvent(userAnswers: UserAnswers, returnPeriod: ReturnPeriod)(implicit hc: HeaderCarrier): Future[Unit] =
    AuditReturnSubmitted(
      userAnswers = userAnswers,
      returnPeriod = returnPeriod,
      spiritsAndIngredientsEnabled = appConfig.spiritsAndIngredientsEnabled,
      submissionService = adrReturnSubmissionService
    ).foldF(
      error => {
        logger.warn(s"Unable to generate return submitted audit event. Error: $error")
        Future.successful(())
      },
      event => Future.successful(auditService.audit(event))
    )

}
