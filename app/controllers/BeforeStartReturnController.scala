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
import connectors.CacheConnector
import controllers.actions._
import models.{ReturnId, ReturnPeriod}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnAndUserDetails
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.returns.ReturnPeriodViewModel
import views.html.BeforeStartReturnView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BeforeStartReturnController @Inject() (
  cacheConnector: CacheConnector,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  val controllerComponents: MessagesControllerComponents,
  view: BeforeStartReturnView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(periodKey: String): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    ReturnPeriod.fromPeriodKey(periodKey) match {
      case None               =>
        logger.warn("Period key is not valid")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case Some(returnPeriod) =>
        val session = request.session + (periodKeySessionKey, periodKey)
        cacheConnector.get(request.appaId, periodKey).map {
          case Some(_) => Redirect(controllers.routes.TaskListController.onPageLoad).withSession(session)
          case None    =>
            Ok(view(ReturnPeriodViewModel(returnPeriod))).withSession(session)
        }
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    request.session.get(periodKeySessionKey) match {
      case None            =>
        logger.warn("Period key not present in session")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case Some(periodKey) =>
        val returnAndUserDetails =
          ReturnAndUserDetails(ReturnId(request.appaId, periodKey), request.groupId, request.userId)
        cacheConnector.createUserAnswers(returnAndUserDetails).map { response =>
          if (response.status != CREATED) {
            logger.warn(s"Unable to create userAnswers: ${response.status} ${response.body}")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          } else {
            Redirect(controllers.routes.TaskListController.onPageLoad)
          }
        }
    }
  }
}
