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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.requests.IdentifierWithoutEnrolmentRequest
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.CredentialStrength.strong
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifyWithoutEnrolmentAction
    extends ActionBuilder[IdentifierWithoutEnrolmentRequest, AnyContent]
    with ActionFunction[Request, IdentifierWithoutEnrolmentRequest]

class IdentifyWithoutEnrolmentActionImpl @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifyWithoutEnrolmentAction
    with AuthorisedFunctions
    with Logging {

  private def predicate: Predicate =
    AuthProviders(GovernmentGateway) and
      CredentialStrength(strong) and
      Organisation and
      ConfidenceLevel.L50

  override def invokeBlock[A](
    request: Request[A],
    block: IdentifierWithoutEnrolmentRequest[A] => Future[Result]
  ): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(predicate).retrieve(internalId and groupIdentifier) { case optInternalId ~ optGroupId =>
      val internalId: String = getOrElseFailWithUnauthorised(optInternalId, "Unable to retrieve internalId")
      val groupId: String    = getOrElseFailWithUnauthorised(optGroupId, "Unable to retrieve groupIdentifier")
      block(IdentifierWithoutEnrolmentRequest(request, groupId, internalId))
    } recover {
      case e: AuthorisationException =>
        logger.debug("Got AuthorisationException:", e)
        handleAuthException(e)
      case e: UnauthorizedException  =>
        logger.debug("Got UnauthorizedException:", e)
        Redirect(routes.UnauthorisedController.onPageLoad)
    }
  }

  private def handleAuthException: PartialFunction[Throwable, Result] = {
    case _: UnsupportedAffinityGroup    => Redirect(routes.NotOrganisationController.onPageLoad)
    case _: InsufficientConfidenceLevel => Redirect(routes.UnauthorisedController.onPageLoad)
    case _: UnsupportedAuthProvider     => Redirect(routes.UnauthorisedController.onPageLoad)
    case _: UnsupportedCredentialRole   => Redirect(routes.UnauthorisedController.onPageLoad)
    case _: IncorrectCredentialStrength => Redirect(routes.UnauthorisedController.onPageLoad)
    case _                              => Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
  }

  def getOrElseFailWithUnauthorised[T](o: Option[T], failureMessage: String): T =
    o.getOrElse {
      logger.warn(s"Identifier Action failed with error: $failureMessage")
      throw new IllegalStateException(failureMessage)
    }
}
