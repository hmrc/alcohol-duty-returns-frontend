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

package controllers.returns

import connectors.AlcoholDutyReturnsConnector
import controllers.actions.IdentifyWithEnrolmentAction
import models.ObligationData
import models.ObligationStatus.Fulfilled
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.returns.ViewPastReturnsHelper
import views.html.returns.CompletedReturnsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CompletedReturnsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifyWithEnrolmentAction,
  val controllerComponents: MessagesControllerComponents,
  viewModelHelper: ViewPastReturnsHelper,
  view: CompletedReturnsView,
  alcoholDutyReturnsConnector: AlcoholDutyReturnsConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(year: Int): Action[AnyContent] = identify.async { implicit request =>
    alcoholDutyReturnsConnector
      .obligationDetails(request.appaId)
      .map { obligations: Seq[ObligationData] =>
        val completedReturns = obligations
          .filter(_.status == Fulfilled)
          .filter(_.toDate.getYear == year)
          .sortBy(_.toDate.toEpochDay)(Ordering[Long].reverse)

        val completedReturnsTable = viewModelHelper.getReturnsTable(completedReturns)

        Ok(view(completedReturnsTable, year))
      }
      .recover { case ex =>
        logger.warn(s"Error fetching completed returns data for year $year", ex)
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }
}
