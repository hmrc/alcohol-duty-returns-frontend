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

package controllers.auth

import config.Constants.periodKeySessionKey
import config.FrontendAppConfig
import connectors.UserAnswersConnector
import controllers.actions.SignOutAction
import models.ReturnId
import models.requests.RequestWithOptAppaId
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SignOutController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  config: FrontendAppConfig,
  userAnswersConnector: UserAnswersConnector,
  signOutAction: SignOutAction
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with Logging
    with I18nSupport {

  def signOut(): Action[AnyContent] = signOutAction.async { implicit request =>
    handleSignOut(request, Seq(config.exitSurveyUrl))
  }

  def signOutDuringEnrolment(): Action[AnyContent] = signOutAction.async { implicit request =>
    handleSignOut(
      request,
      Seq(
        config.loginUrl + s"?continue=${config.loginContinueUrlRequestAccess}"
      )
    )
  }

  private def handleSignOut(request: RequestWithOptAppaId[AnyContent], continueUrl: Seq[String])(implicit
    hc: HeaderCarrier
  ) =
    request.appaId match {
      case Some(appaId) =>
        request.session.get(periodKeySessionKey) match {
          case Some(periodKey) =>
            userAnswersConnector
              .releaseLock(ReturnId(appaId, periodKey))
              .map(_ => Redirect(config.signOutUrl, Map("continue" -> continueUrl)))
          case None            =>
            logger.info("Period key not found during sign out")
            Future.successful(Redirect(config.signOutUrl, Map("continue" -> continueUrl)))
        }
      case None         =>
        logger.info(
          "User not authenticated. No locks to release."
        )
        Future.successful(Redirect(config.signOutUrl, Map("continue" -> continueUrl)))
    }
}
