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

import controllers.actions._

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.AlcoholDutyReturnsConnector
import models.ObligationStatus.{Fulfilled, Open}
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ViewPastReturnsView
import viewmodels.ViewPastReturnsHelper

import scala.concurrent.ExecutionContext

class ViewPastReturnsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view: ViewPastReturnsView,
  alcoholDutyReturnsConnector: AlcoholDutyReturnsConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    (for { obligations <- alcoholDutyReturnsConnector.obligationDetails(request.appaId) } yield {
      val fulfilledObligations    = obligations.filter(_.status == Fulfilled)
      val openObligations         = obligations.filter(_.status == Open)
      val outstandingReturnsTable = ViewPastReturnsHelper.outstandingReturnsTable(openObligations)
      val completedReturnsTable   = ViewPastReturnsHelper.completedReturnsTable(fulfilledObligations)
      Ok(view(outstandingReturnsTable, completedReturnsTable))
    }).recover { case _ =>
      logger.warn("Unable to fetch obligation data")
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
