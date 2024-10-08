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

import cats.data.EitherT
import models.{AlcoholRegime, ReturnPeriod, UserAnswers}
import models.audit.AuditType.ReturnSubmitted
import models.returns.{AdrAdjustmentItem, AdrDutyDeclaredItem, AdrDutySuspendedProduct, AdrRepackagedDraughtAdjustmentItem, AdrSpiritsProduced, AdrTotals}
import play.api.libs.json.{Json, OFormat}
import services.checkAndSubmit.AdrReturnSubmissionService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

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

  def apply(userAnswers: UserAnswers): PrePopulatedData = PrePopulatedData(
    appaId = userAnswers.returnId.appaId,
    periodKey = userAnswers.returnId.periodKey,
    credentialId = userAnswers.internalId,
    groupId = userAnswers.groupId,
    returnSubmittedTime = Instant.now,
    alcoholRegimes = userAnswers.regimes.regimes
  )

}

case class AuditReturnSubmitted(
  prePopulatedData: PrePopulatedData,
  dutyDeclared: Boolean,
  dutyDeclaredItems: Seq[AdrDutyDeclaredItem],
  overDeclarationDeclared: Boolean,
  overDeclarationProducts: Seq[AdrAdjustmentItem],
  underDeclarationDeclared: Boolean,
  underDeclarationProducts: Seq[AdrAdjustmentItem],
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
    spiritsAndIngredientsEnabled: Boolean,
    submissionService: AdrReturnSubmissionService
  )(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): EitherT[Future, String, AuditReturnSubmitted] =
    for {
      dutyDeclared  <- submissionService.getDutyDeclared(userAnswers)
      returnPeriod  <-
        EitherT
          .fromOption[Future](ReturnPeriod.fromPeriodKey(userAnswers.returnId.periodKey), "Return period not defined")
      spirits       <-
        if (userAnswers.regimes.hasSpirits() && returnPeriod.hasQuarterlySpirits && spiritsAndIngredientsEnabled) {
          submissionService.getSpirits(userAnswers)
        } else {
          EitherT.rightT[Future, String](None)
        }
      adjustments   <- submissionService.getAdjustments(userAnswers)
      dutySuspended <- submissionService.getDutySuspended(userAnswers)
      totals        <- submissionService.getTotals(userAnswers)
    } yield AuditReturnSubmitted(
      prePopulatedData = PrePopulatedData(userAnswers),
      dutyDeclared = dutyDeclared.declared,
      dutyDeclaredItems = dutyDeclared.dutyDeclaredItems,
      overDeclarationDeclared = adjustments.overDeclarationDeclared,
      overDeclarationProducts = adjustments.overDeclarationProducts,
      underDeclarationDeclared = adjustments.underDeclarationDeclared,
      underDeclarationProducts = adjustments.underDeclarationProducts,
      spoiltProductDeclared = adjustments.spoiltProductDeclared,
      spoiltProducts = adjustments.spoiltProducts,
      drawbackDeclared = adjustments.drawbackDeclared,
      drawbackProducts = adjustments.drawbackProducts,
      repackagedDraughtDeclared = adjustments.repackagedDraughtDeclared,
      repackagedDraughtProducts = adjustments.repackagedDraughtProducts,
      dutySuspendedDeclared = dutySuspended.declared,
      dutySuspendedProducts = dutySuspended.dutySuspendedProducts,
      spiritsDeclared = spirits.exists(_.spiritsDeclared),
      spiritsProduced = spirits.flatMap(_.spiritsProduced),
      totals = totals
    )
}
