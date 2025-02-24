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

import java.time.Instant

class AuditReturnSubmittedSpec extends SpecBase {

  "AuditReturnSubmitted" - {

    "must be created from UserAnswers with Spirits" in {
      val auditEvent = AuditReturnSubmitted(fullUserAnswers, fullReturn, Instant.now(clock))

      auditEvent.prePopulatedData.appaId       mustBe fullUserAnswers.returnId.appaId
      auditEvent.prePopulatedData.periodKey    mustBe fullUserAnswers.returnId.periodKey
      auditEvent.prePopulatedData.groupId      mustBe fullUserAnswers.groupId
      auditEvent.prePopulatedData.credentialId mustBe fullUserAnswers.internalId

      auditEvent.dutyDeclared      mustBe fullReturn.dutyDeclared.declared
      auditEvent.dutyDeclaredItems mustBe fullReturn.dutyDeclared.dutyDeclaredItems

      auditEvent.overDeclarationDeclared mustBe fullReturn.adjustments.overDeclarationDeclared
      auditEvent.overDeclarationProducts mustBe fullReturn.adjustments.overDeclarationProducts

      auditEvent.underDeclarationDeclared mustBe fullReturn.adjustments.underDeclarationDeclared
      auditEvent.underDeclarationProducts mustBe fullReturn.adjustments.underDeclarationProducts

      auditEvent.spoiltProductDeclared mustBe fullReturn.adjustments.spoiltProductDeclared
      auditEvent.spoiltProducts        mustBe fullReturn.adjustments.spoiltProducts

      auditEvent.drawbackDeclared mustBe fullReturn.adjustments.drawbackDeclared
      auditEvent.drawbackProducts mustBe fullReturn.adjustments.drawbackProducts

      auditEvent.repackagedDraughtDeclared mustBe fullReturn.adjustments.repackagedDraughtDeclared
      auditEvent.repackagedDraughtProducts mustBe fullReturn.adjustments.repackagedDraughtProducts

      auditEvent.dutySuspendedDeclared mustBe fullReturn.dutySuspended.declared
      auditEvent.dutySuspendedProducts mustBe fullReturn.dutySuspended.dutySuspendedProducts

      auditEvent.spiritsProduced.isDefined mustBe true
      auditEvent.spiritsDeclared           mustBe fullReturn.spirits.map(_.spiritsDeclared).get
      auditEvent.spiritsProduced           mustBe fullReturn.spirits.map(_.spiritsProduced).get

      auditEvent.totals mustBe fullReturn.totals
    }
  }
}
