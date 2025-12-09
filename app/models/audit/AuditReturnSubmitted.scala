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

import models.{AlcoholRegime, UserAnswers}
import models.audit.AuditType.ReturnSubmitted
import models.checkAndSubmit._
import play.api.libs.json.{Json, OFormat}

import java.time.Instant

case class PrePopulatedData(
  appaId: String,
  periodKey: String,
  credentialId: String,
  groupId: String,
  returnSubmittedTime: Instant,
  alcoholRegimes: Set[AlcoholRegime]
)

object PrePopulatedData {
  implicit val format: OFormat[PrePopulatedData] = Json.format[PrePopulatedData]

  def apply(userAnswers: UserAnswers, submissionTime: Instant): PrePopulatedData = PrePopulatedData(
    appaId = userAnswers.returnId.appaId,
    periodKey = userAnswers.returnId.periodKey,
    credentialId = userAnswers.internalId,
    groupId = userAnswers.groupId,
    returnSubmittedTime = submissionTime,
    alcoholRegimes = userAnswers.regimes.regimes
  )

}

case class AuditReturnSubmitted(
  prePopulatedData: PrePopulatedData,
  dutyDeclared: Boolean,
  dutyDeclaredItems: Seq[AdrDutyDeclaredItem],
  overDeclarationDeclared: Boolean,
  overDeclarationProducts: Seq[AdrAdjustmentItem],
  overDeclarationReason: Option[String],
  underDeclarationDeclared: Boolean,
  underDeclarationProducts: Seq[AdrAdjustmentItem],
  underDeclarationReason: Option[String],
  spoiltProductDeclared: Boolean,
  spoiltProducts: Seq[AdrAdjustmentItem],
  drawbackDeclared: Boolean,
  drawbackProducts: Seq[AdrAdjustmentItem],
  repackagedDraughtDeclared: Boolean,
  repackagedDraughtProducts: Seq[AdrRepackagedDraughtAdjustmentItem],
  dutySuspendedDeclared: Boolean,
  dutySuspendedProducts: Seq[AdrDutySuspendedProduct],
  spiritsDeclared: Boolean,
  spiritsProduced: Option[AdrSpiritsProduced],
  totals: AdrTotals
) extends AuditEventDetail {
  protected val _auditType = ReturnSubmitted
}

object AuditReturnSubmitted {
  implicit val format: OFormat[AuditReturnSubmitted] = Json.format[AuditReturnSubmitted]
  def apply(
    userAnswers: UserAnswers,
    adrReturnSubmission: AdrReturnSubmission,
    submissionTime: Instant
  ): AuditReturnSubmitted =
    AuditReturnSubmitted(
      prePopulatedData = PrePopulatedData(userAnswers, submissionTime),
      dutyDeclared = adrReturnSubmission.dutyDeclared.declared,
      dutyDeclaredItems = adrReturnSubmission.dutyDeclared.dutyDeclaredItems,
      overDeclarationDeclared = adrReturnSubmission.adjustments.overDeclarationDeclared,
      overDeclarationProducts = adrReturnSubmission.adjustments.overDeclarationProducts,
      overDeclarationReason = adrReturnSubmission.adjustments.reasonForOverDeclaration,
      underDeclarationDeclared = adrReturnSubmission.adjustments.underDeclarationDeclared,
      underDeclarationProducts = adrReturnSubmission.adjustments.underDeclarationProducts,
      underDeclarationReason = adrReturnSubmission.adjustments.reasonForUnderDeclaration,
      spoiltProductDeclared = adrReturnSubmission.adjustments.spoiltProductDeclared,
      spoiltProducts = adrReturnSubmission.adjustments.spoiltProducts,
      drawbackDeclared = adrReturnSubmission.adjustments.drawbackDeclared,
      drawbackProducts = adrReturnSubmission.adjustments.drawbackProducts,
      repackagedDraughtDeclared = adrReturnSubmission.adjustments.repackagedDraughtDeclared,
      repackagedDraughtProducts = adrReturnSubmission.adjustments.repackagedDraughtProducts,
      dutySuspendedDeclared = adrReturnSubmission.dutySuspended.declared,
      dutySuspendedProducts = adrReturnSubmission.dutySuspended.dutySuspendedProducts,
      spiritsDeclared = adrReturnSubmission.spirits.exists(_.spiritsDeclared),
      spiritsProduced = adrReturnSubmission.spirits.flatMap(_.spiritsProduced),
      totals = adrReturnSubmission.totals
    )
}
