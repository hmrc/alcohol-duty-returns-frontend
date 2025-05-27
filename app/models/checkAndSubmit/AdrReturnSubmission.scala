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

package models.checkAndSubmit

import play.api.libs.json.{Json, OFormat}

import java.time.{Instant, LocalDate}

case class AdrAlcoholQuantity(
  litres: BigDecimal,
  lpa: BigDecimal
)

object AdrAlcoholQuantity {
  implicit val format: OFormat[AdrAlcoholQuantity] = Json.format[AdrAlcoholQuantity]
}

case class AdrDuty(
  taxCode: String,
  dutyRate: BigDecimal,
  dutyDue: BigDecimal
)

object AdrDuty {
  implicit val format: OFormat[AdrDuty] = Json.format[AdrDuty]
}

case class AdrDutyDeclaredItem(
  quantityDeclared: AdrAlcoholQuantity,
  dutyDue: AdrDuty
)

object AdrDutyDeclaredItem {
  implicit val format: OFormat[AdrDutyDeclaredItem] = Json.format[AdrDutyDeclaredItem]
}

case class AdrDutyDeclared(
  declared: Boolean,
  dutyDeclaredItems: Seq[AdrDutyDeclaredItem]
)

object AdrDutyDeclared {
  implicit val format: OFormat[AdrDutyDeclared] = Json.format[AdrDutyDeclared]
}

case class AdrAdjustmentItem(
  returnPeriod: String,
  adjustmentQuantity: AdrAlcoholQuantity,
  dutyDue: AdrDuty
)

object AdrAdjustmentItem {
  implicit val format: OFormat[AdrAdjustmentItem] = Json.format[AdrAdjustmentItem]
}

case class AdrRepackagedDraughtAdjustmentItem(
  returnPeriod: String,
  originalTaxCode: String,
  originalDutyRate: BigDecimal,
  newTaxCode: String,
  newDutyRate: BigDecimal,
  repackagedQuantity: AdrAlcoholQuantity,
  dutyAdjustment: BigDecimal
)

object AdrRepackagedDraughtAdjustmentItem {
  implicit val format: OFormat[AdrRepackagedDraughtAdjustmentItem] = Json.format[AdrRepackagedDraughtAdjustmentItem]
}

case class AdrAdjustments(
  overDeclarationDeclared: Boolean,
  reasonForOverDeclaration: Option[String],
  overDeclarationProducts: Seq[AdrAdjustmentItem],
  underDeclarationDeclared: Boolean,
  reasonForUnderDeclaration: Option[String],
  underDeclarationProducts: Seq[AdrAdjustmentItem],
  spoiltProductDeclared: Boolean,
  spoiltProducts: Seq[AdrAdjustmentItem],
  drawbackDeclared: Boolean,
  drawbackProducts: Seq[AdrAdjustmentItem],
  repackagedDraughtDeclared: Boolean,
  repackagedDraughtProducts: Seq[AdrRepackagedDraughtAdjustmentItem]
)

object AdrAdjustments {
  implicit val format: OFormat[AdrAdjustments] = Json.format[AdrAdjustments]
}

case class AdrDutySuspendedProduct(
  regime: AdrDutySuspendedAlcoholRegime,
  suspendedQuantity: AdrAlcoholQuantity
)

object AdrDutySuspendedProduct {
  implicit val format: OFormat[AdrDutySuspendedProduct] = Json.format[AdrDutySuspendedProduct]
}

case class AdrDutySuspended(
  declared: Boolean,
  dutySuspendedProducts: Seq[AdrDutySuspendedProduct]
)

object AdrDutySuspended {
  implicit val format: OFormat[AdrDutySuspended] = Json.format[AdrDutySuspended]
}

case class AdrSpiritsVolumes(
  totalSpirits: BigDecimal,
  scotchWhisky: BigDecimal,
  irishWhiskey: BigDecimal
)

object AdrSpiritsVolumes {
  implicit val format: OFormat[AdrSpiritsVolumes] = Json.format[AdrSpiritsVolumes]
}

case class AdrSpiritsProduced(
  spiritsVolumes: AdrSpiritsVolumes,
  typesOfSpirit: Set[AdrTypeOfSpirit],
  otherSpiritTypeName: Option[String]
)

object AdrSpiritsProduced {
  implicit val format: OFormat[AdrSpiritsProduced] = Json.format[AdrSpiritsProduced]
}

case class AdrSpirits(
  spiritsDeclared: Boolean,
  spiritsProduced: Option[AdrSpiritsProduced]
)

object AdrSpirits {
  implicit val format: OFormat[AdrSpirits] = Json.format[AdrSpirits]
}

case class AdrTotals(
  declaredDutyDue: BigDecimal,
  overDeclaration: BigDecimal,
  underDeclaration: BigDecimal,
  spoiltProduct: BigDecimal,
  drawback: BigDecimal,
  repackagedDraught: BigDecimal,
  totalDutyDue: BigDecimal
)

object AdrTotals {
  implicit val format: OFormat[AdrTotals] = Json.format[AdrTotals]
}

case class AdrReturnSubmission(
  dutyDeclared: AdrDutyDeclared,
  adjustments: AdrAdjustments,
  dutySuspended: AdrDutySuspended,
  spirits: Option[AdrSpirits],
  totals: AdrTotals
)

object AdrReturnSubmission {
  implicit val format: OFormat[AdrReturnSubmission] = Json.format[AdrReturnSubmission]
}

case class AdrReturnCreatedDetails(
  processingDate: Instant,
  amount: BigDecimal,
  chargeReference: Option[String],
  paymentDueDate: Option[LocalDate]
) {
  val isAmountZero: Boolean            = amount == 0
  val isAmountLessThanZero: Boolean    = amount < 0
  val isAmountGreaterThanZero: Boolean = amount > 0
}

object AdrReturnCreatedDetails {
  implicit val format: OFormat[AdrReturnCreatedDetails] = Json.format[AdrReturnCreatedDetails]
}
