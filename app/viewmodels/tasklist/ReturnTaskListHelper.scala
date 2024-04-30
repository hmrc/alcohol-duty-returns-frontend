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

import models.{CheckMode, NormalMode, UserAnswers}
import pages.dutySuspended.{DeclareDutySuspendedDeliveriesQuestionPage, DutySuspendedBeerPage, DutySuspendedCiderPage, DutySuspendedOtherFermentedPage, DutySuspendedSpiritsPage, DutySuspendedWinePage}
import pages.productEntry.{DeclareAlcoholDutyQuestionPage, ProductEntryListPage, ProductListPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.TaskList
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.{TaskListItem, TaskListItemTitle}

object ReturnTaskListHelper {
  def returnSection(userAnswers: UserAnswers)(implicit messages: Messages): Section = {
    val declareDutyQuestion = userAnswers.get(DeclareAlcoholDutyQuestionPage) match {
      case Some(true)  =>
        Seq(
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages("taskList.section.returns.needToDeclare.yes"))),
            status = AlcholDutyTaskListItemStatus.completed,
            href = Some(controllers.productEntry.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url)
          ),
          returnJourneyTaskListItem(userAnswers)
        )
      case Some(false) =>
        Seq(
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages("taskList.section.returns.needToDeclare.no"))),
            status = AlcholDutyTaskListItemStatus.completed,
            href = Some(controllers.productEntry.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url)
          )
        )
      case None        =>
        Seq(
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages("taskList.section.returns.needToDeclare.notStarted"))),
            status = AlcholDutyTaskListItemStatus.notStarted,
            href = Some(controllers.productEntry.routes.DeclareAlcoholDutyQuestionController.onPageLoad(NormalMode).url)
          )
        )
    }

    Section(
      title = messages("taskList.section.returns.heading"),
      taskList = TaskList(items = declareDutyQuestion),
      statusCompleted = AlcholDutyTaskListItemStatus.completed
    )
  }

  def returnDSDSection(userAnswers: UserAnswers)(implicit messages: Messages): Section = {
    val declareDSDQuestion = userAnswers.get(DeclareDutySuspendedDeliveriesQuestionPage) match {
      case Some(true)  =>
        Seq(
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages("taskList.section.dutySuspended.needToDeclare.yes"))),
            status = AlcholDutyTaskListItemStatus.completed,
            href = Some(
              controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController
                .onPageLoad(CheckMode)
                .url
            )
          ),
          returnDSDJourneyTaskListItem(userAnswers)
        )
      case Some(false) =>
        Seq(
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages("taskList.section.dutySuspended.needToDeclare.no"))),
            status = AlcholDutyTaskListItemStatus.completed,
            href = Some(
              controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController
                .onPageLoad(CheckMode)
                .url
            )
          )
        )
      case None        =>
        Seq(
          TaskListItem(
            title =
              TaskListItemTitle(content = Text(messages("taskList.section.dutySuspended.needToDeclare.notStarted"))),
            status = AlcholDutyTaskListItemStatus.notStarted,
            href = Some(
              controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController
                .onPageLoad(NormalMode)
                .url
            )
          )
        )
    }

    Section(
      title = messages("taskList.section.dutySuspended.heading"),
      taskList = TaskList(items = declareDSDQuestion),
      statusCompleted = AlcholDutyTaskListItemStatus.completed
    )
  }

  private def returnJourneyTaskListItem(userAnswers: UserAnswers)(implicit messages: Messages): TaskListItem =
    (userAnswers.get(ProductListPage), userAnswers.get(ProductEntryListPage)) match {
      case (Some(false), Some(list)) if list.nonEmpty =>
        TaskListItem(
          title = TaskListItemTitle(content = Text(messages("taskList.section.returns.products.completed"))),
          status = AlcholDutyTaskListItemStatus.completed,
          href = Some(controllers.productEntry.routes.ProductListController.onPageLoad().url)
        )
      case (_, Some(list)) if list.nonEmpty           =>
        TaskListItem(
          title = TaskListItemTitle(content = Text(messages("taskList.section.returns.products.inProgress"))),
          status = AlcholDutyTaskListItemStatus.inProgress,
          href = Some(controllers.productEntry.routes.ProductListController.onPageLoad().url)
        )
      case (_, _)                                     =>
        TaskListItem(
          title = TaskListItemTitle(content = Text(messages("taskList.section.returns.products.notStarted"))),
          status = AlcholDutyTaskListItemStatus.notStarted,
          href = Some(controllers.productEntry.routes.ProductEntryGuidanceController.onPageLoad().url)
        )
    }

  private def returnDSDJourneyTaskListItem(userAnswers: UserAnswers)(implicit messages: Messages): TaskListItem = {
    val beer           = userAnswers.get(DutySuspendedBeerPage).isDefined
    val cider          = userAnswers.get(DutySuspendedCiderPage).isDefined
    val wine           = userAnswers.get(DutySuspendedWinePage).isDefined
    val spirits        = userAnswers.get(DutySuspendedSpiritsPage).isDefined
    val otherFermented = userAnswers.get(DutySuspendedOtherFermentedPage).isDefined
    val definedCount   = Seq(beer, cider, wine, spirits, otherFermented).count(identity)

    definedCount match {
      case 0 =>
        TaskListItem(
          title = TaskListItemTitle(content = Text(messages("taskList.section.dutySuspended.notStarted"))),
          status = AlcholDutyTaskListItemStatus.notStarted,
          href = Some(controllers.dutySuspended.routes.DutySuspendedDeliveriesGuidanceController.onPageLoad().url)
        )

      case 5 =>
        TaskListItem(
          title = TaskListItemTitle(content = Text(messages("taskList.section.dutySuspended.completed"))),
          status = AlcholDutyTaskListItemStatus.completed,
          href =
            Some(controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad().url)
        )

      case _ =>
        TaskListItem(
          title = TaskListItemTitle(content = Text(messages("taskList.section.dutySuspended.inProgress"))),
          status = AlcholDutyTaskListItemStatus.inProgress,
          href = Some(controllers.dutySuspended.routes.DutySuspendedDeliveriesGuidanceController.onPageLoad().url)
        )

    }
  }

}
