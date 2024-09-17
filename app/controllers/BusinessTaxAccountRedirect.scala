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

package controllers

import config.Constants.periodKeySessionKey
import config.FrontendAppConfig
import connectors.CacheConnector
import controllers.actions.IdentifyWithEnrolmentAction
import models.ReturnId
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessTaxAccountRedirect @Inject() (
  config: FrontendAppConfig,
  identify: IdentifyWithEnrolmentAction,
  cacheConnector: CacheConnector,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with Logging {

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    request.session.get(periodKeySessionKey) match {
      case Some(periodKey) =>
        cacheConnector
          .releaseLock(ReturnId(request.appaId, periodKey))
          .map(_ => Redirect(config.businessTaxAccountUrl))
      case None            =>
        logger.info("Period key not found during redirection to BTA")
        Future.successful(Redirect(config.businessTaxAccountUrl))
    }
  }

}
