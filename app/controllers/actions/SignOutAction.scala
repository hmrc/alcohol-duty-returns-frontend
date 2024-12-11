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
import models.requests.RequestWithOptAppaId
import play.api.Logging
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.CredentialStrength.strong
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait SignOutAction
    extends ActionBuilder[RequestWithOptAppaId, AnyContent]
    with ActionFunction[Request, RequestWithOptAppaId]

class SignOutActionImpl @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends SignOutAction
    with AuthorisedFunctions
    with Logging {

  private def predicate: Predicate =
    AuthProviders(GovernmentGateway) and
      Enrolment(config.enrolmentServiceName) and
      CredentialStrength(strong) and
      Organisation and
      ConfidenceLevel.L50

  override def invokeBlock[A](request: Request[A], block: RequestWithOptAppaId[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(predicate).retrieve(allEnrolments) { enrolments =>
      val appaId = getAppaId(enrolments)
      block(RequestWithOptAppaId(request, Some(appaId)))
    } recoverWith {
      case e: AuthorisationException =>
        logger.debug(s"Returning a request with AppaId set to None since there was an AuthorisationException:", e)
        block(RequestWithOptAppaId(request, None))
      case e: UnauthorizedException  =>
        logger.debug(s"Returning a request with AppaId set to None since there was an UnauthorizedException:", e)
        block(RequestWithOptAppaId(request, None))
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
