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

package viewmodels.tasklist

import base.SpecBase
import generators.ModelGenerators
import models.{CheckMode, NormalMode}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.productEntry.{DeclareAlcoholDutyQuestionPage, ProductEntryListPage, ProductListPage}
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class ReturnTaskListHelperSpec extends SpecBase with ModelGenerators {
  val application: Application    = applicationBuilder().build()
  implicit val messages: Messages = messages(application)

  "ReturnTaskListHelper" - {
    "must return the not started return section if the user answers object is empty" in {
      val result = ReturnTaskListHelper.returnSection(emptyUserAnswers)

      result.completedTask                     shouldBe false
      result.taskList.items.size               shouldBe 1
      result.title                             shouldBe messages("taskList.section.returns.heading")
      result.taskList.items.head.title.content shouldBe Text(
        messages("taskList.section.returns.needToDeclare.notStarted")
      )
      result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.notStarted
      result.taskList.items.head.href          shouldBe Some(
        controllers.productEntry.routes.DeclareAlcoholDutyQuestionController.onPageLoad(NormalMode).url
      )
    }

    "must return a complete section if the user answers no to DeclareAlcoholDuty question" in {
      val userAnswers = emptyUserAnswers
        .set(DeclareAlcoholDutyQuestionPage, false)
        .success
        .value
      val result      = ReturnTaskListHelper.returnSection(userAnswers)

      result.completedTask                     shouldBe true
      result.taskList.items.size               shouldBe 1
      result.title                             shouldBe messages("taskList.section.returns.heading")
      result.taskList.items.head.title.content shouldBe Text(
        messages("taskList.section.returns.needToDeclare.no")
      )
      result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
      result.taskList.items.head.href          shouldBe Some(
        controllers.productEntry.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
      )
    }

    "when Declare Alcohol duty is yes, the Product List Return task" - {
      val declaredAlcoholDutyUserAnswer = emptyUserAnswers
        .set(DeclareAlcoholDutyQuestionPage, true)
        .success
        .value

      "must have a link to ProductEntryGuidanceController if the user has not answer any other question" in {
        val result = ReturnTaskListHelper.returnSection(declaredAlcoholDutyUserAnswer)

        result.completedTask                     shouldBe false
        result.taskList.items.size               shouldBe 2
        result.title                             shouldBe messages("taskList.section.returns.heading")
        result.taskList.items.head.title.content shouldBe Text(
          messages("taskList.section.returns.needToDeclare.yes")
        )
        result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items.head.href          shouldBe Some(
          controllers.productEntry.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
        )

        result.taskList.items(1).title.content shouldBe Text(
          messages("taskList.section.returns.products.notStarted")
        )
        result.taskList.items(1).status        shouldBe AlcholDutyTaskListItemStatus.notStarted
        result.taskList.items(1).href          shouldBe Some(
          controllers.productEntry.routes.ProductEntryGuidanceController.onPageLoad().url
        )
      }

      "must have a link to ProductEntryGuidanceController if the user answer yes to ProductListPage question and the list is empty" in {
        val userAnswers = declaredAlcoholDutyUserAnswer
          .set(ProductEntryListPage, Seq.empty)
          .success
          .value
          .set(ProductListPage, true)
          .success
          .value
        val result      = ReturnTaskListHelper.returnSection(userAnswers)

        result.completedTask                     shouldBe false
        result.taskList.items.size               shouldBe 2
        result.title                             shouldBe messages("taskList.section.returns.heading")
        result.taskList.items.head.title.content shouldBe Text(
          messages("taskList.section.returns.needToDeclare.yes")
        )
        result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items.head.href          shouldBe Some(
          controllers.productEntry.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
        )

        result.taskList.items(1).title.content shouldBe Text(
          messages("taskList.section.returns.products.notStarted")
        )
        result.taskList.items(1).status        shouldBe AlcholDutyTaskListItemStatus.notStarted
        result.taskList.items(1).href          shouldBe Some(
          controllers.productEntry.routes.ProductEntryGuidanceController.onPageLoad().url
        )
      }

      "must have a link to ProductListController if the user answer yes to ProductListPage question and the list is not empty" in {
        val productEntry = productEntryGen.sample.get
        val userAnswers  = declaredAlcoholDutyUserAnswer
          .set(ProductEntryListPage, Seq(productEntry))
          .success
          .value
          .set(ProductListPage, true)
          .success
          .value
        val result       = ReturnTaskListHelper.returnSection(userAnswers)

        result.completedTask                     shouldBe false
        result.taskList.items.size               shouldBe 2
        result.title                             shouldBe messages("taskList.section.returns.heading")
        result.taskList.items.head.title.content shouldBe Text(
          messages("taskList.section.returns.needToDeclare.yes")
        )
        result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items.head.href          shouldBe Some(
          controllers.productEntry.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
        )

        result.taskList.items(1).title.content shouldBe Text(
          messages("taskList.section.returns.products.inProgress")
        )
        result.taskList.items(1).status        shouldBe AlcholDutyTaskListItemStatus.inProgress
        result.taskList.items(1).href          shouldBe Some(
          controllers.productEntry.routes.ProductListController.onPageLoad().url
        )
      }

      "must have a link to ProductListController if the user answer no to ProductListPage question and the list is not empty" in {
        val productEntry = productEntryGen.sample.get
        val userAnswers  = declaredAlcoholDutyUserAnswer
          .set(ProductEntryListPage, Seq(productEntry))
          .success
          .value
          .set(ProductListPage, false)
          .success
          .value
        val result       = ReturnTaskListHelper.returnSection(userAnswers)

        result.completedTask                     shouldBe true
        result.taskList.items.size               shouldBe 2
        result.title                             shouldBe messages("taskList.section.returns.heading")
        result.taskList.items.head.title.content shouldBe Text(
          messages("taskList.section.returns.needToDeclare.yes")
        )
        result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items.head.href          shouldBe Some(
          controllers.productEntry.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
        )

        result.taskList.items(1).title.content shouldBe Text(
          messages("taskList.section.returns.products.completed")
        )
        result.taskList.items(1).status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items(1).href          shouldBe Some(
          controllers.productEntry.routes.ProductListController.onPageLoad().url
        )
      }
    }
  }
}
