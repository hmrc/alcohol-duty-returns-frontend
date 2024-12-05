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

import base.SpecBase
import play.api.libs.json.Json

import java.time.Instant

class ReturnDetailsSpec extends SpecBase {
  "ReturnDetails must" - {
    "deserialise from json" in new SetUp {
      Json.parse(json).as[ReturnDetails] mustBe returnDetails
    }
  }

  class SetUp {
    val periodKey: String  = "24AA"
    val periodKey2: String = "23AL"
    val periodKey3: String = "23AK"
    val periodKey4: String = "23AJ"
    val periodKey5: String = "23AI"
    val periodKey6: String = "23AH"
    val now                = Instant.now(clock)

    val returnDetails = exampleReturnDetails(periodKey, now)

    val json =
      s"""{"identification":{"periodKey":"$periodKey","chargeReference":"$chargeReference","submittedTime":"2024-06-11T15:07:47.838Z"},"alcoholDeclared":{"alcoholDeclaredDetails":[{"taxType":"311","litresOfPureAlcohol":450,"dutyRate":9.27,"dutyValue":4171.50},{"taxType":"321","litresOfPureAlcohol":450,"dutyRate":21.01,"dutyValue":9454.50},{"taxType":"331","litresOfPureAlcohol":450,"dutyRate":28.50,"dutyValue":12825.00},{"taxType":"341","litresOfPureAlcohol":450,"dutyRate":31.64,"dutyValue":14238.00},{"taxType":"351","litresOfPureAlcohol":450,"dutyRate":8.42,"dutyValue":3789.00},{"taxType":"356","litresOfPureAlcohol":450,"dutyRate":19.08,"dutyValue":8586.00},{"taxType":"361","litresOfPureAlcohol":450,"dutyRate":8.40,"dutyValue":3780.00},{"taxType":"366","litresOfPureAlcohol":450,"dutyRate":16.47,"dutyValue":7411.50},{"taxType":"371","litresOfPureAlcohol":450,"dutyRate":8.20,"dutyValue":3960.00},{"taxType":"376","litresOfPureAlcohol":450,"dutyRate":15.63,"dutyValue":7033.50}],"total":75249.00},"adjustments":{"adjustmentDetails":[{"adjustmentTypeKey":"underdeclaration","returnPeriodAffected":"$periodKey2","taxType":"321","litresOfPureAlcohol":150,"dutyRate":21.01,"dutyValue":3151.50},{"adjustmentTypeKey":"overdeclaration","returnPeriodAffected":"$periodKey3","taxType":"321","litresOfPureAlcohol":1150,"dutyRate":21.01,"dutyValue":-24161.50},{"adjustmentTypeKey":"spoilt","returnPeriodAffected":"$periodKey4","taxType":"321","litresOfPureAlcohol":1150,"dutyRate":21.01,"dutyValue":-24161.50},{"adjustmentTypeKey":"drawback","returnPeriodAffected":"$periodKey5","taxType":"321","litresOfPureAlcohol":75,"dutyRate":21.01,"dutyValue":-1575.50},{"adjustmentTypeKey":"repackagedDraught","returnPeriodAffected":"$periodKey6","taxType":"321","litresOfPureAlcohol":150,"dutyRate":21.01,"dutyValue":3151.50}],"total":-19434},"totalDutyDue":{"totalDue":55815},"netDutySuspension":{"totalLtsBeer":0.15,"totalLtsWine":0.44,"totalLtsCider":0.38,"totalLtsSpirit":0.02,"totalLtsOtherFermented":0.02,"totalLtsPureAlcoholBeer":0.4248,"totalLtsPureAlcoholWine":0.5965,"totalLtsPureAlcoholCider":0.0379,"totalLtsPureAlcoholSpirit":0.2492,"totalLtsPureAlcoholOtherFermented":0.1894},"spirits":{"spiritsVolumes":{"totalSpirits":0.05,"scotchWhisky":0.26,"irishWhiskey":0.16},"typesOfSpirit":["NeutralAgricultural"],"otherSpiritTypeName":"Coco Pops Vodka"}}"""
  }
}
