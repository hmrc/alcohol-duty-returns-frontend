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

package pages.declareDuty

import pages.behaviours.PageBehaviours

class DeclareAlcoholDutyQuestionPageSpec extends PageBehaviours {
  "DeclareAlcoholDutyQuestionPage must" - {

    beRetrievable[Boolean](DeclareAlcoholDutyQuestionPage)

    beSettable[Boolean](DeclareAlcoholDutyQuestionPage)

    beRemovable[Boolean](DeclareAlcoholDutyQuestionPage)

    "cleanup subsequent answers from the journey when false is selected" in {
      val updatedAnswers = fullUserAnswers.set(DeclareAlcoholDutyQuestionPage, false).success.value

      updatedAnswers.get(AlcoholDutyPage) must be(empty)
    }

    "not cleanup subsequent answers from the journey when true is selected" in {
      val updatedAnswers = fullUserAnswers.set(DeclareAlcoholDutyQuestionPage, true).success.value

      updatedAnswers.get(AlcoholDutyPage) mustNot be(empty)
    }
  }
}
