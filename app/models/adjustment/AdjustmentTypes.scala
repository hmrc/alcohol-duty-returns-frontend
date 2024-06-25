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

package models.adjustment

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

sealed trait AdjustmentTypes extends EnumEntry

object AdjustmentTypes extends Enum[AdjustmentTypes] with PlayJsonEnum[AdjustmentTypes] {
  val values = findValues

  case object Underdeclaration extends AdjustmentTypes
  case object Overdeclaration extends AdjustmentTypes
  case object Spoilt extends AdjustmentTypes
  case object RepackagedDraughtProducts extends AdjustmentTypes
  case object Drawback extends AdjustmentTypes

  override def toString: String = "adjustmentType"
}
