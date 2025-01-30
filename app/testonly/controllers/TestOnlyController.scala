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

import controllers.actions.{DataRetrievalAction, IdentifyWithEnrolmentAction}
import models.{ReturnId, ReturnPeriod}
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import testonly.connectors.TestOnlyUserAnswersConnector
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnAndUserDetails
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestOnlyController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifyWithEnrolmentAction,
  testOnlyConnector: TestOnlyUserAnswersConnector
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
        val returnAndUserDetails =
          ReturnAndUserDetails(ReturnId(request.appaId, periodKey), request.groupId, request.userId)
        testOnlyConnector
          .createUserAnswers(returnAndUserDetails, beer, cider, wine, spirits, OFP)
          .map {
            case Right(userAnswers) =>
              logger.info(s"Test userAnswers for ${request.appaId}/$periodKey created")
              // TODO: Another page that confirms user answers were created and has a button to Before you start
//              Redirect(controllers.routes.BeforeStartReturnController.onPageLoad(periodKey))
              Ok("Created user answers")
            case Left(error)        =>
              logger.warn(s"Unable to create test userAnswers: $error")
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
    }
  }
}
