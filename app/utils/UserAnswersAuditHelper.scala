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

import com.google.inject.{Inject, Singleton}
import models.audit.{AuditContinueReturn, AuditObligationData, AuditReturnStarted}
import models.{ObligationData, UserAnswers}
import play.api.Logging
import services.AuditService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant}

@Singleton
class UserAnswersAuditHelper @Inject() (
  auditService: AuditService,
  clock: Clock
) extends Logging {

  def auditContinueReturn(
    userAnswers: UserAnswers,
    periodKey: String,
    appaId: String,
    credentialId: String,
    groupId: String
  )(implicit
    hc: HeaderCarrier
  ): Unit = {
    val returnContinueTime = Instant.now(clock)
    val eventDetail        = AuditContinueReturn(
      appaId = appaId,
      periodKey = periodKey,
      credentialId = credentialId,
      groupId = groupId,
      returnContinueTime = returnContinueTime,
      returnStartedTime = userAnswers.startedTime,
      returnValidUntilTime = userAnswers.validUntil
    )
    auditService.audit(eventDetail)
  }

  def auditReturnStarted(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier): Unit =
    userAnswers.get(ObligationData) match {
      case Some(obligationData) =>
        val auditReturnStarted = AuditReturnStarted(
          appaId = userAnswers.returnId.appaId,
          periodKey = userAnswers.returnId.periodKey,
          credentialId = userAnswers.internalId,
          groupId = userAnswers.groupId,
          obligationData = AuditObligationData(obligationData),
          returnStartedTime = userAnswers.startedTime,
          returnValidUntilTime = userAnswers.validUntil
        )
        auditService.audit(auditReturnStarted)
      case None                 =>
        logger.warn(
          "[UserAnswersAuditHelper] [auditReturnStarted] Impossible to create Return Started Audit Event, obligation data unexpectedly missing from user answers"
        )
    }

}
