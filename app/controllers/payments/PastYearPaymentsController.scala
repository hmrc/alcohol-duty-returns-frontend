/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.payments

import connectors.AlcoholDutyAccountConnector
import controllers.actions.IdentifyWithEnrolmentAction
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.payments.ViewPastPaymentsHelper
import views.html.payments.PastYearPaymentsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PastYearPaymentsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifyWithEnrolmentAction,
  val controllerComponents: MessagesControllerComponents,
  helper: ViewPastPaymentsHelper,
  view: PastYearPaymentsView,
  alcoholDutyAccountConnector: AlcoholDutyAccountConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(year: Int): Action[AnyContent] = identify.async { implicit request =>
    val appaId = request.appaId

    val historicPaymentsFuture = alcoholDutyAccountConnector.historicPayments(appaId).map { historicPaymentsData =>
      historicPaymentsData.find(_.year == year) match {
        case Some(historicPayments) if historicPayments.payments.nonEmpty =>
          val historicPaymentsTable = helper.getHistoricPaymentsTable(historicPayments.payments)
          Ok(view(year, historicPaymentsTable))
        case _                                                            =>
          logger.warn(s"No past payments available for year $year (appaId: $appaId)")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

    historicPaymentsFuture.recover { case ex =>
      logger.warn(s"Error fetching past payments data for $appaId, year $year", ex)
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
