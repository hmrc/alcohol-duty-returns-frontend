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
import controllers.actions._
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.returns.ViewPastReturnsHelper
import views.html.returns.ViewPastReturnsView

import java.time.{Clock, Year}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ViewPastReturnsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifyWithEnrolmentAction,
  val controllerComponents: MessagesControllerComponents,
  viewModelHelper: ViewPastReturnsHelper,
  view: ViewPastReturnsView,
  alcoholDutyReturnsConnector: AlcoholDutyReturnsConnector,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    val appaId      = request.appaId
    val currentYear = Year.now(clock).getValue

    val openObligationsFuture = alcoholDutyReturnsConnector.openObligations(appaId).map { openObligations =>
      viewModelHelper.getReturnsTable(openObligations)
    }

    val fulfilledObligationsFuture =
      alcoholDutyReturnsConnector.fulfilledObligations(appaId).map { fulfilledObligationsData =>
        val currentYearFulfilledObligations = fulfilledObligationsData
          .find(_.year == currentYear)
          .getOrElse(throw new IllegalStateException("Current year fulfilled obligations list not found"))
        val fulfilledObligationsTable       = viewModelHelper.getReturnsTable(currentYearFulfilledObligations.obligations)
        val pastYears                       =
          fulfilledObligationsData
            .filter(o => o.year != currentYear && o.obligations.nonEmpty)
            .map(_.year)
            .sorted
            .reverse
        (fulfilledObligationsTable, pastYears)
      }

    val obligationsFuture = for {
      openObligationsTable                   <- openObligationsFuture
      (fulfilledObligationsTable, pastYears) <- fulfilledObligationsFuture
    } yield Ok(view(openObligationsTable, fulfilledObligationsTable, currentYear, pastYears))

    obligationsFuture.recover { case ex =>
      logger.warn(s"[ViewPastReturnsController] [onPageLoad] Error fetching obligation data for $appaId", ex)
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
