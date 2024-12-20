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

package models.declareDuty

import base.SpecBase
import models.UnitsOfMeasure
import models.checkAndSubmit.AdrUnitOfMeasure

class AdrReturnSubmissionSpec extends SpecBase {
  "AdrUnitOfMeasure" - {
    "should construct from UnitsOfMeasure to AdrUnitOfMeasure" in {
      AdrUnitOfMeasure(UnitsOfMeasure.Tonnes) mustBe AdrUnitOfMeasure.Tonnes
      AdrUnitOfMeasure(UnitsOfMeasure.Litres) mustBe AdrUnitOfMeasure.Litres
    }
  }

}
