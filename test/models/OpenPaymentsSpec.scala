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

package models

import base.SpecBase

import java.time.LocalDate

class OpenPaymentsSpec extends SpecBase {
  "OutstandingPayment .toCreditAvailablePayment must" - {
    "return a Some(CreditAvailablePayment) if transaction type is RPI or remaining amount is negative" in {
      RPIPayment.toCreditAvailablePayment               mustBe Some(creditAvailablePaymentForRPI)
      outstandingCreditPayment.toCreditAvailablePayment mustBe Some(creditAvailablePaymentForOutstandingCredit)
    }

    "return None otherwise" in {
      outstandingDuePayment.toCreditAvailablePayment mustBe None
      outstandingLPIPayment.toCreditAvailablePayment mustBe None
    }
  }

  "UnallocatedPayment .toCreditAvailablePayment must" - {
    "return a CreditAvailablePayment" in {
      val unallocatedPayment             = UnallocatedPayment(LocalDate.of(2024, 9, 25), BigDecimal(-123))
      val expectedCreditAvailablePayment =
        CreditAvailablePayment(None, LocalDate.of(2024, 9, 25), None, BigDecimal(-123))

      unallocatedPayment.toCreditAvailablePayment mustBe expectedCreditAvailablePayment
    }
  }

  "OpenPayments" - {
    "paymentsForOutstandingTable must return outstanding payments with payment due" in {
      openPaymentsData.paymentsForOutstandingTable mustBe
        Seq(
          outstandingPartialPayment,
          outstandingLPIPayment,
          outstandingOverduePartialPayment,
          outstandingDuePayment
        )
    }

    "paymentsForCreditAvailableTable must return payments with credit available" in {
      openPaymentsData.paymentsForCreditAvailableTable mustBe
        Seq(
          creditAvailablePaymentForOutstandingCredit,
          creditAvailablePaymentForRPI,
          CreditAvailablePayment(None, LocalDate.of(2024, 9, 25), None, BigDecimal(-123)),
          CreditAvailablePayment(None, LocalDate.of(2024, 8, 25), None, BigDecimal(-1273)),
          CreditAvailablePayment(None, LocalDate.of(2024, 7, 25), None, BigDecimal(-1273))
        )
    }
  }
}
