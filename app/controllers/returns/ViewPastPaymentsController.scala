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

import connectors.{AlcoholDutyAccountConnector, AlcoholDutyReturnsConnector}
import controllers.actions.IdentifierAction
import models.TransactionType.{LPI, RPI, Return}
import models.{OutstandingPayment, TransactionType}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.returns.ViewPastPaymentsViewModel
import views.html.returns.ViewPastPaymentsView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ViewPastPaymentsController @Inject() (
                                             override val messagesApi: MessagesApi,
                                             identify: IdentifierAction,
                                             val controllerComponents: MessagesControllerComponents,
                                             viewPastPaymentsModel: ViewPastPaymentsViewModel,
                                             view: ViewPastPaymentsView,
                                             alcoholDutyAccountConnector: AlcoholDutyAccountConnector
                                           )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = identify//identify.async
  { implicit request =>
    val appaId = request.appaId
//    alcoholDutyAccountConnector.getOutstandingPayments(appaId).map {
//      outstandingPaymentsData =>
    val outstandingPaymentsData = Seq(
  OutstandingPayment(TransactionType.Return, Some(LocalDate.now()), "1234ChangreRef", BigInt(-1234), BigInt(-2345)),
  OutstandingPayment(TransactionType.LPI, None, "1234ChangreRef", BigInt(3234), BigInt(2345)),
  OutstandingPayment(
    transactionType = Return,
    date = Some(LocalDate.of(2024, 8, 25)),
    chargeReference = s"XM012345678890123",
    totalAmountPence = BigInt(477335),
    remainingAmountPence = BigInt(477335)
  ),
  OutstandingPayment(
    transactionType = RPI,
    date = None,
    chargeReference = s"XM1112223334445",
    totalAmountPence = BigInt(20111),
    remainingAmountPence = BigInt(20111)
  ),
  OutstandingPayment(
    transactionType = LPI,
    date = None,
    chargeReference = s"XA1234123412341",
    totalAmountPence = BigInt(10111),
    remainingAmountPence = BigInt(10111)
  ),
  OutstandingPayment(
    transactionType = Return,
    date = Some(LocalDate.of(2024, 7, 25)),
    chargeReference = s"XA1122334455667",
    totalAmountPence = BigInt(2131411),
    remainingAmountPence = BigInt(2131411)
  ),
  OutstandingPayment(
    transactionType = Return,
    date = Some(LocalDate.of(2024, 6, 25)),
    chargeReference = s"XA3210987654321",
    totalAmountPence = BigInt(77335),
    remainingAmountPence = BigInt(61441)
  ))
        val outstandingPaymentsTable = viewPastPaymentsModel.getOutstandingPaymentsTable(outstandingPaymentsData)
        Ok(view(outstandingPaymentsTable))
//    }.recover { case _ =>
//      logger.warn(s"Unable to fetch outstanding payments data for $appaId")
//      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
//    }
  }
}
