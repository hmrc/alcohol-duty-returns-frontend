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

package controllers.actions

import config.FrontendAppConfig
import models.requests.{IdentifierRequest, IdentifierWithoutEnrolmentRequest}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFunction, Result}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait ServiceEntryCheckAction extends ActionFunction[IdentifierWithoutEnrolmentRequest, IdentifierRequest]

class ServiceEntryCheckActionImpl @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig
)(implicit val executionContext: ExecutionContext)
    extends ServiceEntryCheckAction
    with AuthorisedFunctions
    with Logging {

  val predicate: Predicate = AuthProviders(GovernmentGateway) and
    Enrolment(config.enrolmentServiceName)

  override def invokeBlock[A](
    request: IdentifierWithoutEnrolmentRequest[A],
    block: IdentifierRequest[A] => Future[Result]
  ): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(predicate).retrieve(allEnrolments) { enrolments: Enrolments =>
      getAppaId(enrolments) match {
        case Some(appaId) if appaId.nonEmpty =>
          block(
            IdentifierRequest(
              request = request.request,
              appaId = appaId,
              groupId = request.groupId,
              userId = request.userId
            )
          )
        case _                               =>
          logger.warn(s"ADR Enrolment Identifier not found for user")
          Future.successful(Redirect(controllers.auth.routes.DoYouHaveAnAppaIdController.onPageLoad()))
      }
    } recover {
      case _: InsufficientEnrolments =>
        logger.info(s"Enrolment not found for user")
        Redirect(controllers.auth.routes.DoYouHaveAnAppaIdController.onPageLoad())
      case e                         =>
        logger.warn("Enrolment check error: ", e)
        Redirect(controllers.auth.routes.UnauthorisedController.onPageLoad)
    }
  }

  private def getAppaId(enrolments: Enrolments): Option[String] =
    for {
      adrEnrolments <- enrolments.enrolments.find(_.key == config.enrolmentServiceName)
      appaId        <- adrEnrolments.getIdentifier(config.enrolmentIdentifierKey).map(_.value)
    } yield appaId

}
