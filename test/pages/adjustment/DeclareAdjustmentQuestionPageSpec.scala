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

package pages.adjustment

import pages.behaviours.PageBehaviours

class DeclareAdjustmentQuestionPageSpec extends PageBehaviours {
  "DeclareAdjustmentQuestionPage must" - {

    beRetrievable[Boolean](DeclareAdjustmentQuestionPage)

    beSettable[Boolean](DeclareAdjustmentQuestionPage)

    beRemovable[Boolean](DeclareAdjustmentQuestionPage)

    "cleanup subsequent answers from the journey when false is selected" in {
      val updatedAnswers = fullUserAnswers.set(DeclareAdjustmentQuestionPage, false).success.value

      updatedAnswers.get(AdjustmentEntryListPage)    must be(empty)
      updatedAnswers.get(AdjustmentTotalPage)        must be(empty)
      updatedAnswers.get(UnderDeclarationTotalPage)  must be(empty)
      updatedAnswers.get(OverDeclarationTotalPage)   must be(empty)
      updatedAnswers.get(UnderDeclarationReasonPage) must be(empty)
      updatedAnswers.get(OverDeclarationReasonPage)  must be(empty)
    }

    "not cleanup subsequent answers from the journey when true is selected" in {
      val updatedAnswers = fullUserAnswers.set(DeclareAdjustmentQuestionPage, true).success.value

      updatedAnswers.get(AdjustmentEntryListPage)    mustNot be(empty)
      updatedAnswers.get(AdjustmentTotalPage)        mustNot be(empty)
      updatedAnswers.get(UnderDeclarationTotalPage)  mustNot be(empty)
      updatedAnswers.get(OverDeclarationTotalPage)   mustNot be(empty)
      updatedAnswers.get(UnderDeclarationReasonPage) mustNot be(empty)
      updatedAnswers.get(OverDeclarationReasonPage)  mustNot be(empty)
    }
  }
}
