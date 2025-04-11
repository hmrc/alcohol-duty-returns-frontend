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

package pages.spiritsQuestions

import pages.behaviours.PageBehaviours

class DeclareQuarterlySpiritsPageSpec extends PageBehaviours {
  "DeclareQuarterlySpiritsPage must" - {

    beRetrievable[Boolean](DeclareQuarterlySpiritsPage)

    beSettable[Boolean](DeclareQuarterlySpiritsPage)

    beRemovable[Boolean](DeclareQuarterlySpiritsPage)

    "cleanup subsequent answers from the journey when false is selected" in {
      val updatedAnswers = fullUserAnswers.set(DeclareQuarterlySpiritsPage, false).success.value

      updatedAnswers.get(DeclareSpiritsTotalPage)  must be(empty)
      updatedAnswers.get(WhiskyPage)               must be(empty)
      updatedAnswers.get(SpiritTypePage)           must be(empty)
      updatedAnswers.get(OtherSpiritsProducedPage) must be(empty)
    }

    "not cleanup subsequent answers from the journey when true is selected" in {
      val updatedAnswers = fullUserAnswers.set(DeclareQuarterlySpiritsPage, true).success.value

      updatedAnswers.get(DeclareSpiritsTotalPage)  mustNot be(empty)
      updatedAnswers.get(WhiskyPage)               mustNot be(empty)
      updatedAnswers.get(SpiritTypePage)           mustNot be(empty)
      updatedAnswers.get(OtherSpiritsProducedPage) mustNot be(empty)
    }
  }
}
