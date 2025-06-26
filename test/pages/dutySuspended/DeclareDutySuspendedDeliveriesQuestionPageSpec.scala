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

class DeclareDutySuspendedDeliveriesQuestionPageSpec extends PageBehaviours {
  "DeclareDutySuspendedDeliveriesQuestionPage must" - {

    beRetrievable[Boolean](DeclareDutySuspendedDeliveriesQuestionPage)

    beSettable[Boolean](DeclareDutySuspendedDeliveriesQuestionPage)

    beRemovable[Boolean](DeclareDutySuspendedDeliveriesQuestionPage)

    "cleanup subsequent answers from the journey when false is selected" in {
      val updatedAnswers = fullUserAnswers.set(DeclareDutySuspendedDeliveriesQuestionPage, false).success.value

      updatedAnswers.get(DutySuspendedBeerPage)           must be(empty)
      updatedAnswers.get(DutySuspendedCiderPage)          must be(empty)
      updatedAnswers.get(DutySuspendedWinePage)           must be(empty)
      updatedAnswers.get(DutySuspendedSpiritsPage)        must be(empty)
      updatedAnswers.get(DutySuspendedOtherFermentedPage) must be(empty)
    }

    "not cleanup subsequent answers from the journey when true is selected" in {
      val updatedAnswers = fullUserAnswers.set(DeclareDutySuspendedDeliveriesQuestionPage, true).success.value

      updatedAnswers.get(DutySuspendedBeerPage)           mustNot be(empty)
      updatedAnswers.get(DutySuspendedCiderPage)          mustNot be(empty)
      updatedAnswers.get(DutySuspendedWinePage)           mustNot be(empty)
      updatedAnswers.get(DutySuspendedSpiritsPage)        mustNot be(empty)
      updatedAnswers.get(DutySuspendedOtherFermentedPage) mustNot be(empty)
    }
  }
}
