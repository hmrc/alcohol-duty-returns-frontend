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

package pages.dutySuspendedNew

import models.AlcoholRegime
import models.AlcoholRegime.{Beer, Cider, Wine}
import pages.behaviours.PageBehaviours

class DeclareDutySuspenseQuestionPageSpec extends PageBehaviours {
  "DeclareDutySuspenseQuestionPage must" - {

    beRetrievable[Boolean](DeclareDutySuspenseQuestionPage)

    beSettable[Boolean](DeclareDutySuspenseQuestionPage)

    beRemovable[Boolean](DeclareDutySuspenseQuestionPage)

    "cleanup subsequent answers from the journey when false is selected" in {
      val alcoholRegimesSet: Set[AlcoholRegime] = Set(Beer, Cider, Wine)

      val userAnswers    = emptyUserAnswers.set(DutySuspendedAlcoholTypePage, alcoholRegimesSet).success.value
      val updatedAnswers = userAnswers.set(DeclareDutySuspenseQuestionPage, false).success.value

      updatedAnswers.get(DutySuspendedAlcoholTypePage) must be(empty)
    }

    "not cleanup subsequent answers from the journey when true is selected" in {
      val alcoholRegimesSet: Set[AlcoholRegime] = Set(Beer, Cider, Wine)

      val userAnswers    = emptyUserAnswers.set(DutySuspendedAlcoholTypePage, alcoholRegimesSet).success.value
      val updatedAnswers = userAnswers.set(DeclareDutySuspenseQuestionPage, true).success.value

      updatedAnswers.get(DutySuspendedAlcoholTypePage) mustNot be(empty)
    }
  }
}
