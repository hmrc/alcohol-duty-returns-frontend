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
import play.api.mvc.Results.Redirect
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.CredentialStrength.strong
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import scala.concurrent.{ExecutionContext, Future}

trait AuthorisedAction extends ActionBuilder[Request, AnyContent] with ActionFunction[Request, Request]

class BaseAuthorisedAction @Inject() (
  appConfig: FrontendAppConfig,
  override val authConnector: AuthConnector,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends AuthorisedAction
    with FrontendHeaderCarrierProvider
    with AuthorisedFunctions {

  private val alcoholDutyEnrolment = "HMRC-AD-ORG"

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    implicit val headerCarrier: HeaderCarrier = hc(request)

    authorised(
      AuthProviders(GovernmentGateway)
        and Enrolment(alcoholDutyEnrolment)
        and CredentialStrength(strong)
        and Organisation
        and ConfidenceLevel.L50
    ) {
      block(request)
    } recover {
      handleAuthException
    }
  }

  private def handleAuthException: PartialFunction[Throwable, Result] = {
    case _: InsufficientEnrolments      => Redirect(routes.UnauthorisedController.onPageLoad)
    case _: InsufficientConfidenceLevel => Redirect(routes.UnauthorisedController.onPageLoad)
    case _: UnsupportedAuthProvider     => Redirect(routes.UnauthorisedController.onPageLoad)
    case _: UnsupportedAffinityGroup    => Redirect(routes.UnauthorisedController.onPageLoad)
    case _: UnsupportedCredentialRole   => Redirect(routes.UnauthorisedController.onPageLoad)
    case _: IncorrectCredentialStrength => Redirect(routes.UnauthorisedController.onPageLoad)
    case _: UnauthorizedException       => Redirect(routes.UnauthorisedController.onPageLoad)
    case _: AuthorisationException      => Redirect(appConfig.loginUrl, Map("continue" -> Seq(appConfig.loginContinueUrl)))
  }
}
