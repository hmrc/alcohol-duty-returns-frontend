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
import config.Constants.pastPaymentsSessionKey
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.Session

class CentralAssessmentHelperSpec extends SpecBase {

  implicit val messages: Messages = getMessages(app)
  val centralAssessmentHelper     = new CentralAssessmentHelper(createDateTimeHelper())

  "getCentralAssessmentChargeFromSession must" - {
    "return the required Central Assessment charge and its index" in {
      val outstandingPayments = Seq(
        outstandingDuePayment.copy(chargeReference = Some("AAA")),
        outstandingLPIPayment.copy(chargeReference = Some("BBB")),
        outstandingCAPayment.copy(chargeReference = Some("CCC")),
        outstandingCAPayment,
        outstandingCAIPayment.copy(chargeReference = Some("EEE"))
      )

      val session = Session(data = Map(pastPaymentsSessionKey -> Json.toJson(outstandingPayments).toString))

      val result = centralAssessmentHelper.getCentralAssessmentChargeFromSession(session, chargeReference)

      result mustBe Some((outstandingCAPayment, 3))
    }

    "return None if pastPaymentsSessionKey is not present in the session" in {
      centralAssessmentHelper.getCentralAssessmentChargeFromSession(Session(), chargeReference) mustBe None
    }

    "return None if the session data cannot be parsed as Seq[OutstandingPayment]" in {
      val session = Session(data = Map(pastPaymentsSessionKey -> Json.toJson(historicPayments2025).toString))

      centralAssessmentHelper.getCentralAssessmentChargeFromSession(session, chargeReference) mustBe None
    }

    "return None if the session data does not contain a Central Assessment with the required charge reference" in {
      val outstandingPayments = Seq(
        outstandingDuePayment.copy(chargeReference = Some("AAA")),
        outstandingLPIPayment.copy(chargeReference = Some("BBB")),
        outstandingCAPayment.copy(chargeReference = Some("CCC"))
      )

      val session = Session(data = Map(pastPaymentsSessionKey -> Json.toJson(outstandingPayments).toString))

      centralAssessmentHelper.getCentralAssessmentChargeFromSession(session, chargeReference) mustBe None
    }
  }

  "getCentralAssessmentViewModel must" - {
    "return a ManageCentralAssessmentViewModel given an OutstandingPayment for a Central Assessment" in {
      val result = centralAssessmentHelper.getCentralAssessmentViewModel(outstandingCAPayment)

      result mustBe CentralAssessmentViewModel(
        chargeReference = chargeReference,
        dateFrom = "1 July 2024",
        dateTo = "31 July 2024",
        returnDueDate = "15 August 2024",
        amount = BigDecimal(3234.18)
      )
    }

    "throw an exception if charge reference is missing" in {
      val exception = intercept[IllegalStateException] {
        centralAssessmentHelper.getCentralAssessmentViewModel(outstandingCAPayment.copy(chargeReference = None))
      }
      exception.getMessage mustBe "Charge reference is required for Central Assessment"
    }

    "throw an exception if taxPeriodFrom is missing" in {
      val exception = intercept[IllegalStateException] {
        centralAssessmentHelper.getCentralAssessmentViewModel(outstandingCAPayment.copy(taxPeriodFrom = None))
      }
      exception.getMessage mustBe "taxPeriodFrom is required for Central Assessment"
    }

    "throw an exception if taxPeriodTo is missing" in {
      val exception = intercept[IllegalStateException] {
        centralAssessmentHelper.getCentralAssessmentViewModel(outstandingCAPayment.copy(taxPeriodTo = None))
      }
      exception.getMessage mustBe "taxPeriodTo is required for Central Assessment"
    }
  }

}
