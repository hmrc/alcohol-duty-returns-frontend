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

package pages.dutySuspended

import pages.behaviours.PageBehaviours

class DeclareDutySuspenseQuestionPageSpec extends PageBehaviours {
  "DeclareDutySuspenseQuestionPage must" - {

    beRetrievable[Boolean](DeclareDutySuspenseQuestionPage)

    beSettable[Boolean](DeclareDutySuspenseQuestionPage)

    beRemovable[Boolean](DeclareDutySuspenseQuestionPage)

    "cleanup subsequent answers from the journey when false is selected" in {
      val updatedAnswers = userAnswersWithDutySuspendedData.set(DeclareDutySuspenseQuestionPage, false).success.value

      updatedAnswers.get(DutySuspendedAlcoholTypePage)  must be(empty)
      updatedAnswers.get(DutySuspendedQuantitiesPage)   must be(empty)
      updatedAnswers.get(DutySuspendedFinalVolumesPage) must be(empty)
    }

    "not cleanup subsequent answers from the journey when true is selected" in {
      val updatedAnswers = userAnswersWithDutySuspendedData.set(DeclareDutySuspenseQuestionPage, true).success.value

      updatedAnswers.get(DutySuspendedAlcoholTypePage)  mustNot be(empty)
      updatedAnswers.get(DutySuspendedQuantitiesPage)   mustNot be(empty)
      updatedAnswers.get(DutySuspendedFinalVolumesPage) mustNot be(empty)
    }
  }
}
