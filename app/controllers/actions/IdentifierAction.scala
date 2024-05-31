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
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction
    extends ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with Logging {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(internalId and groupIdentifier and allEnrolments) {
      case optInternalId ~ optGroupId ~ enrolments =>
        val internalId: String = getOrElseFailWithUnauthorised(optInternalId, "Unable to retrieve internalId")
        val groupId: String    = getOrElseFailWithUnauthorised(optGroupId, "Unable to retrieve groupIdentifier")
        val appaId             = getAppaId(enrolments)
        block(IdentifierRequest(request, appaId, groupId, internalId))
    } recover {
      case _: NoActiveSession        =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case _: AuthorisationException =>
        Redirect(routes.UnauthorisedController.onPageLoad)
    }
  }

  private def getAppaId(enrolments: Enrolments): String = {
    val adrEnrolments: Enrolment  = getOrElseFailWithUnauthorised(
      enrolments.enrolments.find(_.key == config.enrolmentServiceName),
      s"Unable to retrieve enrolment: ${config.enrolmentServiceName}"
    )
    val appaIdOpt: Option[String] =
      adrEnrolments.getIdentifier(config.enrolmentIdentifierKey).map(_.value)
    getOrElseFailWithUnauthorised(appaIdOpt, "Unable to retrieve APPAID from enrolments")
  }

  def getOrElseFailWithUnauthorised[T](o: Option[T], failureMessage: String): T =
    o.getOrElse {
      logger.warn(s"Identifier Action failed with error: $failureMessage")
      throw new IllegalStateException(failureMessage)
    }

}
