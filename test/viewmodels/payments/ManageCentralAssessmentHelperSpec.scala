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

import base.SpecBase
import play.api.i18n.Messages

class ManageCentralAssessmentHelperSpec extends SpecBase {

  implicit val messages: Messages   = getMessages(app)
  val manageCentralAssessmentHelper = new ManageCentralAssessmentHelper(createDateTimeHelper())

  "getCentralAssessmentViewModel must" - {
    "return a ManageCentralAssessmentViewModel given an OutstandingPayment for a Central Assessment" in {
      val result = manageCentralAssessmentHelper.getCentralAssessmentViewModel(outstandingCAPayment)

      result mustBe ManageCentralAssessmentViewModel(
        chargeReference = chargeReference,
        dateFrom = "1 July 2024",
        dateTo = "31 July 2024",
        returnDueDate = "15 August 2024",
        amount = BigDecimal(3234.18)
      )
    }

    "throw an exception if charge reference is missing" in {
      val exception = intercept[IllegalStateException] {
        manageCentralAssessmentHelper.getCentralAssessmentViewModel(outstandingCAPayment.copy(chargeReference = None))
      }
      exception.getMessage mustBe "Charge reference is required for Central Assessment"
    }

    "throw an exception if taxPeriodFrom is missing" in {
      val exception = intercept[IllegalStateException] {
        manageCentralAssessmentHelper.getCentralAssessmentViewModel(outstandingCAPayment.copy(taxPeriodFrom = None))
      }
      exception.getMessage mustBe "taxPeriodFrom is required for Central Assessment"
    }

    "throw an exception if taxPeriodTo is missing" in {
      val exception = intercept[IllegalStateException] {
        manageCentralAssessmentHelper.getCentralAssessmentViewModel(outstandingCAPayment.copy(taxPeriodTo = None))
      }
      exception.getMessage mustBe "taxPeriodTo is required for Central Assessment"
    }
  }

}
