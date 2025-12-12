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

package utils

import base.SpecBase
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.audit.{AuditContinueReturn, AuditObligationData, AuditReturnStarted}
import models.{AlcoholRegimes, ObligationData, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify}
import services.AuditService

import java.time.Instant

class UserAnswersAuditHelperSpec extends SpecBase {

  "UserAnswersAuditHelper" - {

    "auditContinueReturn must call the audit service for existing user answers" in new SetUp {
      userAnswersAuditHelper.auditContinueReturn(emptyUserAnswers, periodKey, appaId, internalId, groupId)
      verify(mockAuditService).audit(eqTo(expectedContinueReturnAuditEvent))(any(), any())
    }

    "auditReturnStarted" - {

      "must call the audit service for newly created user answers" in new SetUp {
        val userAnswers = emptyUserAnswers.set(ObligationData, obligationDataSingleOpen).success.value

        userAnswersAuditHelper.auditReturnStarted(userAnswers)
        verify(mockAuditService).audit(eqTo(expectedReturnStartedAuditEvent))(any(), any())
      }

      "must not call the audit service if obligation data is unexpectedly missing from user answers" in new SetUp {
        val userAnswers = emptyUserAnswers.remove(ObligationData).success.value

        userAnswersAuditHelper.auditReturnStarted(userAnswers)
        verify(mockAuditService, times(0)).audit(any())(any(), any())
      }
    }
  }

  class SetUp {
    val mockAuditService = mock[AuditService]

    val userAnswersAuditHelper = new UserAnswersAuditHelper(mockAuditService, clock)

    val emptyUserAnswers: UserAnswers = UserAnswers(
      returnId,
      groupId,
      internalId,
      regimes = AlcoholRegimes(Set(Beer, Cider, Wine, Spirits, OtherFermentedProduct)),
      startedTime = Instant.now(clock),
      lastUpdated = Instant.now(clock),
      validUntil = Some(Instant.now(clock))
    )

    val expectedContinueReturnAuditEvent = AuditContinueReturn(
      appaId = returnId.appaId,
      periodKey = returnId.periodKey,
      credentialId = emptyUserAnswers.internalId,
      groupId = emptyUserAnswers.groupId,
      returnContinueTime = Instant.now(clock),
      returnStartedTime = Instant.now(clock),
      returnValidUntilTime = Some(Instant.now(clock))
    )

    val expectedReturnStartedAuditEvent = AuditReturnStarted(
      appaId = returnId.appaId,
      periodKey = returnId.periodKey,
      credentialId = emptyUserAnswers.internalId,
      groupId = emptyUserAnswers.groupId,
      obligationData = AuditObligationData(obligationDataSingleOpen),
      returnStartedTime = Instant.now(clock),
      returnValidUntilTime = Some(Instant.now(clock))
    )
  }
}
