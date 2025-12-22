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

package controllers.checkAndSubmit

import config.Constants.returnCreatedDetailsKey
import controllers.actions._
import models.checkAndSubmit.AdrReturnCreatedDetails
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.returns.{ReturnSubmittedHelper, ReturnSubmittedViewModel}
import views.html.checkAndSubmit.ReturnSubmittedView

import javax.inject.Inject

class ReturnSubmittedController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifyWithEnrolmentAction,
  val controllerComponents: MessagesControllerComponents,
  view: ReturnSubmittedView,
  returnSubmittedHelper: ReturnSubmittedHelper
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = identify { implicit request =>
    request.session.get(returnCreatedDetailsKey) match {
      case None                       =>
        logger.warn("[ReturnSubmittedController] [onPageLoad] Return details not present in session")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      case Some(returnCreatedDetails) =>
        Json.fromJson[AdrReturnCreatedDetails](Json.parse(returnCreatedDetails)).asOpt match {
          case Some(returnDetails: AdrReturnCreatedDetails) =>
            val viewModel: ReturnSubmittedViewModel = returnSubmittedHelper.getReturnSubmittedViewModel(returnDetails)
            Ok(view(viewModel))
          case None                                         =>
            logger.warn("[ReturnSubmittedController] [onPageLoad] Return details not valid")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  }
}
