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

package services

import base.SpecBase
import models.audit.AuditPaymentStarted
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.verify
import services.AuditService
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.Instant

class AuditServiceSpec extends SpecBase {

  private val mockAuditConnector = mock[AuditConnector]

  private val auditService = new AuditService(mockAuditConnector)

  "AuditService" - {

    "send audit event correctly" in {
      val testDetail = AuditPaymentStarted(
        appaId = appaId,
        credentialID = "xyz",
        paymentStartedTime = Instant.now(clock),
        journeyId = "xyz",
        chargeReference = chargeReference,
        amountInPence = BigInt(123)
      )
      auditService.audit(testDetail)

      verify(mockAuditConnector).sendExplicitAudit(eqTo("PaymentStarted"), eqTo(testDetail))(any(), any(), any())
    }
  }
}
