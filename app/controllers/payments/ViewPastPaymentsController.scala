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

package controllers.payments

import config.Constants.pastPaymentsSessionKey
import connectors.AlcoholDutyAccountConnector
import controllers.actions.IdentifyWithEnrolmentAction
import models.payments.OutstandingPayments
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.payments.ViewPastPaymentsHelper
import views.html.payments.ViewPastPaymentsView

import java.time.{Clock, LocalDate, Year}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ViewPastPaymentsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifyWithEnrolmentAction,
  val controllerComponents: MessagesControllerComponents,
  helper: ViewPastPaymentsHelper,
  view: ViewPastPaymentsView,
  alcoholDutyAccountConnector: AlcoholDutyAccountConnector,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    val appaId                    = request.appaId
    val outstandingPaymentsFuture =
      alcoholDutyAccountConnector.outstandingPayments(appaId).map { outstandingPaymentsData =>
        val sortedOutstandingPaymentsData =
          outstandingPaymentsData.outstandingPayments.sortBy(_.dueDate)(Ordering[LocalDate].reverse)
        val updatedSession                =
          request.session + (pastPaymentsSessionKey -> Json.toJson(sortedOutstandingPaymentsData).toString)
        val outstandingPaymentsTable =
          helper.getOutstandingPaymentsTable(sortedOutstandingPaymentsData)
        val unallocatedPaymentsTable = helper
          .getUnallocatedPaymentsTable(outstandingPaymentsData.unallocatedPayments)

        OutstandingPayments(
          outstandingPaymentsTable,
          unallocatedPaymentsTable,
          outstandingPaymentsData.totalOpenPaymentsAmount,
          updatedSession
        )
      }

    val historicPaymentsFuture =
      alcoholDutyAccountConnector.historicPayments(appaId, Year.now(clock).getValue).map { historicPaymentsData =>
        val historicPaymentsTable = helper.getHistoricPaymentsTable(historicPaymentsData.payments)
        (historicPaymentsTable, historicPaymentsData.year)
      }

    val openAndHistoricPaymentsFuture = for {
      pastPaymentsData              <- outstandingPaymentsFuture
      (historicPaymentsTable, year) <- historicPaymentsFuture
    } yield Ok(
      view(
        pastPaymentsData.outstandingPaymentsTable,
        pastPaymentsData.unallocatedPaymentsTable,
        historicPaymentsTable,
        helper.getViewPastPaymentsViewModel(pastPaymentsData.totalOpenPaymentsAmount, year)
      )
    ).withSession(pastPaymentsData.session)

    openAndHistoricPaymentsFuture.recover { case ex =>
      logger.warn(s"Error fetching payments data for $appaId", ex)
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
