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

package viewmodels.payments

import config.Constants.pastPaymentsSessionKey
import models.TransactionType.CA
import models.{OutstandingPayment, ReturnPeriod}
import play.api.Logging
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.Session
import viewmodels.DateTimeHelper

import javax.inject.Inject

case class CentralAssessmentViewModel(
  chargeReference: String,
  dateFrom: String,
  dateTo: String,
  returnDueDate: String,
  amount: BigDecimal
)

class CentralAssessmentHelper @Inject() (dateTimeHelper: DateTimeHelper) extends Logging {

  def getCentralAssessmentChargeFromSession(session: Session, chargeRef: String): Option[(OutstandingPayment, Int)] =
    session.get(pastPaymentsSessionKey) match {
      case None                 =>
        logger.warn(
          "[CentralAssessmentHelper] [getCentralAssessmentChargeFromSession] Outstanding payment details not present in session"
        )
        None
      case Some(paymentDetails) =>
        Json.parse(paymentDetails).asOpt[Seq[OutstandingPayment]] match {
          case Some(outstandingPayments) =>
            outstandingPayments.find(p => p.chargeReference.contains(chargeRef) && p.transactionType == CA) match {
              case Some(charge) => Some((charge, outstandingPayments.indexOf(charge)))
              case None         =>
                logger.warn(
                  "[CentralAssessmentHelper] [getCentralAssessmentChargeFromSession] Could not find required Central Assessment charge in session"
                )
                None
            }
          case None                      =>
            throw new RuntimeException("Could not parse outstanding payment details in session")
        }
    }

  def getCentralAssessmentViewModel(
    charge: OutstandingPayment
  )(implicit messages: Messages): CentralAssessmentViewModel = {
    val chargeReference = charge.chargeReference.getOrElse(
      throw new IllegalStateException("Charge reference is required for Central Assessment")
    )
    val dateFrom        = charge.taxPeriodFrom
      .map(dateTimeHelper.formatDateMonthYear)
      .getOrElse(
        throw new IllegalStateException("taxPeriodFrom is required for Central Assessment")
      )
    val taxPeriodTo     = charge.taxPeriodTo.getOrElse(
      throw new IllegalStateException("taxPeriodTo is required for Central Assessment")
    )
    val dateTo          = dateTimeHelper.formatDateMonthYear(taxPeriodTo)
    val returnDueDate   = dateTimeHelper.formatDateMonthYear(ReturnPeriod.fromDateInPeriod(taxPeriodTo).periodDueDate())

    CentralAssessmentViewModel(chargeReference, dateFrom, dateTo, returnDueDate, charge.remainingAmount)
  }
}
