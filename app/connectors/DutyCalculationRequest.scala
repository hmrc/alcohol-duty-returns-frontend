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

package connectors

import models.returns.VolumeAndRateByTaxType
import models.adjustment.AdjustmentTypes
import play.api.libs.json.{Json, OFormat}

case class DutyCalculationRequest(
  adjustmentType: AdjustmentTypes,
  pureAlcoholVolume: BigDecimal,
  rate: BigDecimal
)

object DutyCalculationRequest {
  implicit val formats: OFormat[DutyCalculationRequest] = Json.format[DutyCalculationRequest]
}

case class TotalDutyCalculationRequest(
  dutiesByTaxType: Seq[VolumeAndRateByTaxType]
)

object TotalDutyCalculationRequest {
  implicit val formats: OFormat[TotalDutyCalculationRequest] = Json.format[TotalDutyCalculationRequest]
}

case class AdjustmentDutyCalculationRequest(
  newDuty: BigDecimal,
  oldDuty: BigDecimal
)
object AdjustmentDutyCalculationRequest {
  implicit val formats: OFormat[AdjustmentDutyCalculationRequest] = Json.format[AdjustmentDutyCalculationRequest]
}
