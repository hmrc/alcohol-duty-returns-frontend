/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.adjustment

import base.SpecBase
import models.adjustment.AdjustmentType
import models.adjustment.AdjustmentType.Overdeclaration

class AdjustmentTaxTypeHelperSpec extends SpecBase {

  "AdjustmentTaxTypeHelper" - {
    "createViewModel method must create a view model with the declared adjustment type and correct url" in new Setup {
      val testAdjustmentType: AdjustmentType = Overdeclaration

      testHelper.createViewModel(testAdjustmentType) mustBe AdjustmentTaxTypeViewModel(Overdeclaration)
    }
  }

  class Setup {

    val testHelper = app.injector.instanceOf[AdjustmentTaxTypeHelper]

  }

}
