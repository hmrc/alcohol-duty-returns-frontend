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

package testonly.controllers

import controllers.actions.IdentifyWithEnrolmentAction
import models.{ReturnId, ReturnPeriod}
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import testonly.connectors.TestOnlyUserAnswersConnector
import testonly.views.html.CreatedUserAnswersTestView
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnAndUserDetails
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestOnlyController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifyWithEnrolmentAction,
  testOnlyConnector: TestOnlyUserAnswersConnector,
  createdUserAnswersView: CreatedUserAnswersTestView
)(implicit val ec: ExecutionContext)
    extends FrontendBaseController
    with Logging {

  def clearAllData(): Action[AnyContent] = Action.async { implicit request =>
    testOnlyConnector.clearAllData().map(httpResponse => Ok(httpResponse.body))
  }

  def createUserAnswers(
    periodKey: String,
    beer: Boolean,
    cider: Boolean,
    wine: Boolean,
    spirits: Boolean,
    OFP: Boolean
  ): Action[AnyContent] = identify.async { implicit request =>
    ReturnPeriod.fromPeriodKey(periodKey) match {
      case None    => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case Some(_) =>
        if (!(beer | cider | wine | spirits | OFP)) {
          Future.successful(BadRequest("At least one alcohol regime approval is required."))
        } else {
          val returnAndUserDetails =
            ReturnAndUserDetails(ReturnId(request.appaId, periodKey), request.groupId, request.userId)
          testOnlyConnector
            .createUserAnswers(returnAndUserDetails, beer, cider, wine, spirits, OFP)
            .map {
              case Right(_)                                  =>
                logger.info(s"Test userAnswers for ${request.appaId}/$periodKey created")
                Ok(createdUserAnswersView(request.appaId, periodKey, beer, cider, wine, spirits, OFP))
              case Left(error) if error.statusCode == LOCKED =>
                logger.warn(s"Return ${request.appaId}/$periodKey locked for the user")
                Redirect(controllers.routes.ReturnLockedController.onPageLoad())
              case Left(error)                               =>
                logger.warn(s"Unable to create test userAnswers: $error")
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
        }
    }
  }
}
