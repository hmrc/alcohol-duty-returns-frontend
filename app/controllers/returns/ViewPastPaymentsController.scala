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
import models.TransactionType.{LPI, PaymentOnAccount, RPI, Return}
import models.{OutstandingPayment, OutstandingPayments, TransactionType}
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

  def onPageLoad: Action[AnyContent] = identify //identify.async
  { implicit request =>
    val appaId                   = request.appaId
//    alcoholDutyAccountConnector.getOutstandingPayments(appaId).map {
//      outstandingPaymentsData =>
    val outstandingPaymentsData  = OutstandingPayments(
      outstandingPayments = Seq(
        OutstandingPayment(
          TransactionType.Return,
          Some(LocalDate.now()),
          Some("XM0026103011594"),
          BigDecimal(-7234),
          BigDecimal(-2345)
        ),
        OutstandingPayment(TransactionType.LPI, None, Some("1234ChangreRef"), BigDecimal(3234), BigDecimal(2345)),
        OutstandingPayment(
          Return,
          Some(LocalDate.of(2024, 8, 25)),
          Some(s"XM0026103011593"),
          BigDecimal(4773),
          BigDecimal(4735)
        ),
        OutstandingPayment(
          Return,
          Some(LocalDate.of(2024, 9, 25)),
          Some(s"XM0026130011595"),
          BigDecimal(4773),
          BigDecimal(3775)
        ),
        OutstandingPayment(
          RPI,
          None,
          Some(s"XM0026130011597"),
          BigDecimal(2011),
          BigDecimal(2011)
        ),
        OutstandingPayment(
          LPI,
          None,
          Some(s"XM0026103011598"),
          BigDecimal(1011),
          BigDecimal(1011)
        ),
        OutstandingPayment(
          Return,
          Some(LocalDate.of(2024, 7, 25)),
          Some(s"XM0026103011596"),
          BigDecimal(2131411),
          BigDecimal(2131411)
        ),
        OutstandingPayment(
          Return,
          Some(LocalDate.of(2024, 6, 25)),
          Some(s"XM00261030115948"),
          BigDecimal(3235),
          BigDecimal(441)
        ),
        OutstandingPayment(
          PaymentOnAccount,
          None,
          None,
          BigDecimal(6441),
          BigDecimal(6141)
        )
      ),
      totalBalance = BigDecimal(1234.45)
    )
    val outstandingPaymentsTable =
      viewPastPaymentsModel.getOutstandingPaymentsTable(outstandingPaymentsData.outstandingPayments)
    Ok(view(outstandingPaymentsTable, outstandingPaymentsData.totalBalance))
//    }.recover { case _ =>
//      logger.warn(s"Unable to fetch outstanding payments data for $appaId")
//      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
//    }
  }
}
