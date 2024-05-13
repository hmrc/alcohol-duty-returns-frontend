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

import models.{CheckMode, Mode, NormalMode, SpiritType, UserAnswers}
import pages.dutySuspended.{DeclareDutySuspendedDeliveriesQuestionPage, DutySuspendedBeerPage, DutySuspendedCiderPage, DutySuspendedOtherFermentedPage, DutySuspendedSpiritsPage, DutySuspendedWinePage}
import pages.productEntry.{DeclareAlcoholDutyQuestionPage, ProductEntryListPage, ProductListPage}
import pages.spiritsQuestions.{AlcoholUsedPage, DeclareQuarterlySpiritsPage, DeclareSpiritsTotalPage, EthyleneGasOrMolassesUsedPage, GrainsUsedPage, OtherIngredientsUsedPage, OtherMaltedGrainsPage, OtherSpiritsProducedPage, SpiritTypePage, WhiskyPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.TaskList
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.{TaskListItem, TaskListItemTitle}
import viewmodels.tasklist.DeclarationState.{Completed, InProgress, NotStarted}

object ReturnTaskListHelper {
  private def createSection(
    declareQuestionAnswer: Option[Boolean],
    createTaskListSection: () => TaskListItem,
    declarationController: Mode => String,
    sectionName: String
  )(implicit
    messages: Messages
  ): Section = {
    val taskListItems = declareQuestionAnswer match {
      case Some(true) =>
        Seq(
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages(s"taskList.section.$sectionName.needToDeclare.yes"))),
            status = AlcholDutyTaskListItemStatus.completed,
            href = Some(declarationController(CheckMode))
          ),
          createTaskListSection()
        )

      case Some(false) =>
        Seq(
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages(s"taskList.section.$sectionName.needToDeclare.no"))),
            status = AlcholDutyTaskListItemStatus.completed,
            href = Some(declarationController(CheckMode))
          )
        )

      case None =>
        Seq(
          TaskListItem(
            title =
              TaskListItemTitle(content = Text(messages(s"taskList.section.$sectionName.needToDeclare.notStarted"))),
            status = AlcholDutyTaskListItemStatus.notStarted,
            href = Some(declarationController(NormalMode))
          )
        )
    }

    Section(
      title = messages(s"taskList.section.$sectionName.heading"),
      taskList = TaskList(items = taskListItems),
      statusCompleted = AlcholDutyTaskListItemStatus.completed
    )
  }

  private def createDeclarationTask(
    getDeclarationState: () => DeclarationState,
    sectionName: String,
    notStartedUrl: Option[String],
    inProgressUrl: Option[String],
    startedOrCompleteUrl: Option[String]
  )(implicit messages: Messages): TaskListItem =
    getDeclarationState() match {
      case NotStarted =>
        TaskListItem(
          title = TaskListItemTitle(content = Text(messages(s"taskList.section.$sectionName.notStarted"))),
          status = AlcholDutyTaskListItemStatus.notStarted,
          href = notStartedUrl
        )
      case InProgress =>
        TaskListItem(
          title = TaskListItemTitle(content = Text(messages(s"taskList.section.$sectionName.inProgress"))),
          status = AlcholDutyTaskListItemStatus.inProgress,
          href = inProgressUrl
        )
      case Completed  =>
        TaskListItem(
          title = TaskListItemTitle(content = Text(messages(s"taskList.section.$sectionName.completed"))),
          status = AlcholDutyTaskListItemStatus.completed,
          href = startedOrCompleteUrl
        )
    }

  private def returnJourneyTaskListItem(userAnswers: UserAnswers)(implicit messages: Messages): TaskListItem = {
    val getDeclarationState = () =>
      (userAnswers.get(ProductListPage), userAnswers.get(ProductEntryListPage)) match {
        case (Some(false), Some(list)) if list.nonEmpty => Completed
        case (_, Some(list)) if list.nonEmpty           => InProgress
        case _                                          => NotStarted
      }

    createDeclarationTask(
      getDeclarationState,
      "returns.products",
      Some(controllers.productEntry.routes.ProductEntryGuidanceController.onPageLoad().url),
      Some(controllers.productEntry.routes.ProductListController.onPageLoad().url),
      Some(controllers.productEntry.routes.ProductListController.onPageLoad().url)
    )
  }

  private def returnDSDJourneyTaskListItem(userAnswers: UserAnswers)(implicit messages: Messages): TaskListItem = {
    val getDeclarationState = () => {
      val beer           = userAnswers.get(DutySuspendedBeerPage).isDefined
      val cider          = userAnswers.get(DutySuspendedCiderPage).isDefined
      val wine           = userAnswers.get(DutySuspendedWinePage).isDefined
      val spirits        = userAnswers.get(DutySuspendedSpiritsPage).isDefined
      val otherFermented = userAnswers.get(DutySuspendedOtherFermentedPage).isDefined

      val pagesCompleted = Seq(beer, cider, wine, spirits, otherFermented)

      if (pagesCompleted.forall(_ == false)) {
        NotStarted
      } else if (pagesCompleted.forall(_ == true)) {
        Completed
      } else {
        InProgress
      }
    }

    createDeclarationTask(
      getDeclarationState,
      "dutySuspended",
      Some(controllers.dutySuspended.routes.DutySuspendedDeliveriesGuidanceController.onPageLoad().url),
      Some(controllers.dutySuspended.routes.DutySuspendedDeliveriesGuidanceController.onPageLoad().url),
      Some(controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad().url)
    )
  }

  private def returnQSJourneyTaskListItem(userAnswers: UserAnswers)(implicit messages: Messages): TaskListItem = {
    val getDeclarationState = () => {
      val declareSpiritsTotal = userAnswers.get(DeclareSpiritsTotalPage).isDefined
      val whisky              = userAnswers.get(WhiskyPage).isDefined
      val spiritsType         = userAnswers
        .get(SpiritTypePage)
        .fold(false)(spiritsTypePage =>
          !spiritsTypePage.contains(SpiritType.Other) || userAnswers.get(OtherSpiritsProducedPage).isDefined
        )
      val grainsUsed          = userAnswers
        .get(GrainsUsedPage)
        .fold(false)(grainsUsed =>
          !grainsUsed.usedMaltedGrainNotBarley || userAnswers.get(OtherMaltedGrainsPage).isDefined
        )
      val alcoholUsed         = userAnswers.get(AlcoholUsedPage).isDefined
      val ingredientsUsed     = userAnswers
        .get(EthyleneGasOrMolassesUsedPage)
        .fold(false)(ethyleneGasOrMolassesUsedPage =>
          !ethyleneGasOrMolassesUsedPage.otherIngredients || userAnswers.get(OtherIngredientsUsedPage).isDefined
        )

      val pagesCompleted =
        Seq(declareSpiritsTotal, whisky, spiritsType, grainsUsed, alcoholUsed, ingredientsUsed)

      if (pagesCompleted.forall(_ == false))
        NotStarted
      else if (pagesCompleted.forall(_ == true))
        Completed
      else
        InProgress
    }

    createDeclarationTask(
      getDeclarationState,
      "spirits",
      Some(controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode).url),
      Some(controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode).url),
      Some(controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url)
    )
  }

  def returnSection(userAnswers: UserAnswers)(implicit messages: Messages): Section =
    createSection(
      userAnswers.get(DeclareAlcoholDutyQuestionPage),
      () => returnJourneyTaskListItem(userAnswers),
      controllers.productEntry.routes.DeclareAlcoholDutyQuestionController.onPageLoad(_).url,
      sectionName = "returns"
    )

  def returnDSDSection(userAnswers: UserAnswers)(implicit messages: Messages): Section =
    createSection(
      userAnswers.get(DeclareDutySuspendedDeliveriesQuestionPage),
      () => returnDSDJourneyTaskListItem(userAnswers),
      controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(_).url,
      sectionName = "dutySuspended"
    )

  def returnQSSection(userAnswers: UserAnswers)(implicit messages: Messages): Section =
    createSection(
      userAnswers.get(DeclareQuarterlySpiritsPage),
      () => returnQSJourneyTaskListItem(userAnswers),
      controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(_).url,
      sectionName = "spirits"
    )
}
