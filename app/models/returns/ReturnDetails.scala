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

package models.returns

import play.api.libs.json.{Json, OFormat}

import java.time.Instant

case class ReturnDetails(
  identification: ReturnDetailsIdentification,
  alcoholDeclared: ReturnAlcoholDeclared,
  adjustments: ReturnAdjustments,
  totalDutyDue: ReturnTotalDutyDue
)

object ReturnDetails {
  implicit val returnDetailsFormat: OFormat[ReturnDetails] = Json.format[ReturnDetails]
}

case class ReturnDetailsIdentification(periodKey: String, submittedTime: Instant)

object ReturnDetailsIdentification {
  implicit val returnDetailsIdentificationFormat: OFormat[ReturnDetailsIdentification] =
    Json.format[ReturnDetailsIdentification]
}

case class ReturnAlcoholDeclared(alcoholDeclaredDetails: Option[Seq[ReturnAlcoholDeclaredRow]], total: BigDecimal)

object ReturnAlcoholDeclared {
  implicit val returnAlcoholDeclaredFormat: OFormat[ReturnAlcoholDeclared] = Json.format[ReturnAlcoholDeclared]
}

case class ReturnAlcoholDeclaredRow(
  taxType: String,
  litresOfPureAlcohol: BigDecimal,
  dutyRate: BigDecimal,
  dutyValue: BigDecimal
)

object ReturnAlcoholDeclaredRow {
  implicit val returnAlcoholDeclaredRowRowFormat: OFormat[ReturnAlcoholDeclaredRow] =
    Json.format[ReturnAlcoholDeclaredRow]

  implicit val ordering: Ordering[ReturnAlcoholDeclaredRow] =
    (row1: ReturnAlcoholDeclaredRow, row2: ReturnAlcoholDeclaredRow) => row1.taxType.compareTo(row2.taxType)
}

case class ReturnAdjustments(adjustmentDetails: Option[Seq[ReturnAdjustmentsRow]], total: BigDecimal)

object ReturnAdjustments {
  val underDeclaredKey     = "underdeclaration"
  val overDeclaredKey      = "overdeclaration"
  val repackagedDraughtKey = "repackagedDraught"
  val spoiltKey            = "spoilt"
  val drawbackKey          = "drawback"

  implicit val returnAdjustmentsFormat: OFormat[ReturnAdjustments] = Json.format[ReturnAdjustments]
}

case class ReturnAdjustmentsRow(
  adjustmentTypeKey: String,
  taxType: String,
  litresOfPureAlcohol: BigDecimal,
  dutyRate: BigDecimal,
  dutyValue: BigDecimal
)

object ReturnAdjustmentsRow {
  implicit val returnAdjustmentsRowFormat: OFormat[ReturnAdjustmentsRow] = Json.format[ReturnAdjustmentsRow]

  private val keyOrder =
    Seq(
      ReturnAdjustments.underDeclaredKey,
      ReturnAdjustments.overDeclaredKey,
      ReturnAdjustments.repackagedDraughtKey,
      ReturnAdjustments.spoiltKey,
      ReturnAdjustments.drawbackKey
    ).zipWithIndex.toMap

  implicit val ordering: Ordering[ReturnAdjustmentsRow] =
    (row1: ReturnAdjustmentsRow, row2: ReturnAdjustmentsRow) => {
      val keyCompare = keyOrder
        .getOrElse(row1.adjustmentTypeKey, Int.MaxValue) - keyOrder.getOrElse(row2.adjustmentTypeKey, Int.MaxValue)

      if (keyCompare == 0) {
        row1.taxType.compareTo(row2.taxType)
      } else {
        keyCompare
      }
    }
}

case class ReturnTotalDutyDue(totalDue: BigDecimal)

object ReturnTotalDutyDue {
  implicit val returnTotalDutyDueFormat: OFormat[ReturnTotalDutyDue] = Json.format[ReturnTotalDutyDue]
}
