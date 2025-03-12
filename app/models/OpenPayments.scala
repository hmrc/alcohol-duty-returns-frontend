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

package models

import enumeratum.{Enum, EnumEntry, PlayEnum, PlayJsonEnum}
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

sealed trait TransactionType extends EnumEntry

object TransactionType extends Enum[TransactionType] with PlayJsonEnum[TransactionType] {
  val values = findValues

  case object Return extends TransactionType

  case object PaymentOnAccount extends TransactionType

  case object LPI extends TransactionType

  case object RPI extends TransactionType
}

sealed trait OpenPayment

case class OutstandingPayment(
  transactionType: TransactionType,
  dueDate: LocalDate,
  chargeReference: Option[String],
  remainingAmount: BigDecimal
) extends OpenPayment

object OutstandingPayment {
  implicit val outstandingPaymentFormat: OFormat[OutstandingPayment] = Json.format[OutstandingPayment]
}

case class CreditAvailablePayment(
  transactionType: TransactionType,
  paymentDate: LocalDate,
  chargeReference: Option[String],
  amount: BigDecimal
)

object CreditAvailablePayment {
  implicit val creditAvailablePaymentFormat: OFormat[CreditAvailablePayment] = Json.format[CreditAvailablePayment]
}

case class OpenPayments(
  outstandingPayments: Seq[OutstandingPayment],
  totalOutstandingPayments: BigDecimal,
  creditAvailablePayments: Seq[CreditAvailablePayment],
  totalCreditAvailable: BigDecimal,
  totalOpenPaymentsAmount: BigDecimal
)

object OpenPayments {
  implicit val openPaymentsPaymentsFormat: OFormat[OpenPayments] = Json.format[OpenPayments]
}
sealed trait OutstandingPaymentStatusToDisplay extends EnumEntry

object OutstandingPaymentStatusToDisplay extends PlayEnum[OutstandingPaymentStatusToDisplay] {
  val values = findValues

  case object Due extends OutstandingPaymentStatusToDisplay
  case object Overdue extends OutstandingPaymentStatusToDisplay
  case object NothingToPay extends OutstandingPaymentStatusToDisplay
}
