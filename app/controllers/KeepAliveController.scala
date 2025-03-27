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

package controllers

import config.Constants.periodKeySessionKey
import connectors.UserAnswersConnector
import controllers.actions.IdentifyWithEnrolmentAction
import models.ReturnId
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class KeepAliveController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifyWithEnrolmentAction,
  userAnswersConnector: UserAnswersConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController {

  def keepAlive: Action[AnyContent] = identify.async { implicit request =>
    val maybePeriodKey: Option[String] = request.session.get(periodKeySessionKey)
    maybePeriodKey match {
      case Some(periodKey) => userAnswersConnector.keepAlive(ReturnId(request.appaId, periodKey)).map(_ => Ok)
      case None     => Future.successful(Ok)
    }
  }
}
