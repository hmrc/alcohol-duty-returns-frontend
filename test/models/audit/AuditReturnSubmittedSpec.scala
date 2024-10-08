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

package models.audit

import base.SpecBase
import cats.data.EitherT
import models.ReturnId
import org.mockito.ArgumentMatchers.any
import services.checkAndSubmit.AdrReturnSubmissionService

class AuditReturnSubmittedSpec extends SpecBase {

  "AuditReturnSubmitted" - {
    val isSpiritEnable = true

    val adrReturnSubmissionService = mock[AdrReturnSubmissionService]

    when(adrReturnSubmissionService.getDutyDeclared(any()))
      .thenReturn(EitherT.rightT(fullReturn.dutyDeclared))

    when(adrReturnSubmissionService.getAdjustments(any()))
      .thenReturn(EitherT.rightT(fullReturn.adjustments))

    when(adrReturnSubmissionService.getDutySuspended(any()))
      .thenReturn(EitherT.rightT(fullReturn.dutySuspended))

    when(adrReturnSubmissionService.getSpirits(any()))
      .thenReturn(EitherT.rightT(fullReturn.spirits))

    when(adrReturnSubmissionService.getTotals(any())(any()))
      .thenReturn(EitherT.rightT(fullReturn.totals))

    "should be created from UserAnswers with Spirits" in {

      whenReady(AuditReturnSubmitted(fullUserAnswers, isSpiritEnable, adrReturnSubmissionService).value) { result =>
        result.isRight mustBe true
        result.map { auditEvent =>
          auditEvent.prePopulatedData.appaId mustBe fullUserAnswers.returnId.appaId
          auditEvent.prePopulatedData.periodKey mustBe fullUserAnswers.returnId.periodKey
          auditEvent.prePopulatedData.groupId mustBe fullUserAnswers.groupId
          auditEvent.prePopulatedData.credentialId mustBe fullUserAnswers.internalId

          auditEvent.dutyDeclared mustBe fullReturn.dutyDeclared.declared
          auditEvent.dutyDeclaredItems mustBe fullReturn.dutyDeclared.dutyDeclaredItems

          auditEvent.overDeclarationDeclared mustBe fullReturn.adjustments.overDeclarationDeclared
          auditEvent.overDeclarationProducts mustBe fullReturn.adjustments.overDeclarationProducts

          auditEvent.underDeclarationDeclared mustBe fullReturn.adjustments.underDeclarationDeclared
          auditEvent.underDeclarationProducts mustBe fullReturn.adjustments.underDeclarationProducts

          auditEvent.spoiltProductDeclared mustBe fullReturn.adjustments.spoiltProductDeclared
          auditEvent.spoiltProducts mustBe fullReturn.adjustments.spoiltProducts

          auditEvent.drawbackDeclared mustBe fullReturn.adjustments.drawbackDeclared
          auditEvent.drawbackProducts mustBe fullReturn.adjustments.drawbackProducts

          auditEvent.repackagedDraughtDeclared mustBe fullReturn.adjustments.repackagedDraughtDeclared
          auditEvent.repackagedDraughtProducts mustBe fullReturn.adjustments.repackagedDraughtProducts

          auditEvent.dutySuspendedDeclared mustBe fullReturn.dutySuspended.declared
          auditEvent.dutySuspendedProducts mustBe fullReturn.dutySuspended.dutySuspendedProducts

          auditEvent.spiritsProduced.isDefined mustBe true
          auditEvent.spiritsDeclared mustBe fullReturn.spirits.map(_.spiritsDeclared).get
          auditEvent.spiritsProduced mustBe fullReturn.spirits.map(_.spiritsProduced).get

          auditEvent.totals mustBe fullReturn.totals
        }

      }
    }

    "should be created from UserAnswers without Spirits if spirits is disabled" in {
      val isSpiritEnable = false
      whenReady(AuditReturnSubmitted(fullUserAnswers, isSpiritEnable, adrReturnSubmissionService).value) { result =>
        result.isRight mustBe true
        result.map { auditEvent =>
          auditEvent.spiritsDeclared mustBe false
          auditEvent.spiritsProduced mustBe None
        }
      }
    }

    "should be created from UserAnswers without Spirits if spirits is enabled but the return period does not require quarterly spirits return" in {
      val nonQuarterlySpirits = nonQuarterReturnPeriodGen.sample.get
      val userAnswer          =
        fullUserAnswers.copy(returnId = ReturnId(fullUserAnswers.returnId.appaId, nonQuarterlySpirits.toPeriodKey))
      whenReady(AuditReturnSubmitted(userAnswer, isSpiritEnable, adrReturnSubmissionService).value) { result =>
        result.isRight mustBe true
        result.map { auditEvent =>
          auditEvent.spiritsDeclared mustBe false
          auditEvent.spiritsProduced mustBe None
        }
      }
    }

    "should return an error if the Period Key is not valid" in {
      val userAnswer = fullUserAnswers.copy(returnId = ReturnId(fullUserAnswers.returnId.appaId, "AAAA"))
      whenReady(AuditReturnSubmitted(userAnswer, isSpiritEnable, adrReturnSubmissionService).value) { result =>
        result.isLeft mustBe true
      }
    }
  }

}
