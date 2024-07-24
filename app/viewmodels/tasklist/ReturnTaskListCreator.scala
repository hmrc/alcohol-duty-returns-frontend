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
import pages.adjustment.{AdjustmentEntryListPage, AdjustmentListPage, DeclareAdjustmentQuestionPage}
import pages.dutySuspended.{DeclareDutySuspendedDeliveriesQuestionPage, DutySuspendedBeerPage, DutySuspendedCiderPage, DutySuspendedOtherFermentedPage, DutySuspendedSpiritsPage, DutySuspendedWinePage}
import pages.returns.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage, WhatDoYouNeedToDeclarePage}
import pages.spiritsQuestions.{AlcoholUsedPage, DeclareQuarterlySpiritsPage, DeclareSpiritsTotalPage, EthyleneGasOrMolassesUsedPage, GrainsUsedPage, OtherIngredientsUsedPage, OtherMaltedGrainsPage, OtherSpiritsProducedPage, SpiritTypePage, WhiskyPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.TaskList
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.{TaskListItem, TaskListItemTitle}
import viewmodels.AlcoholRegimesViewOrder
import viewmodels.tasklist.DeclarationState.{Completed, InProgress, NotStarted}

import javax.inject.Inject

class ReturnTaskListCreator @Inject() () {
  private def createSection(
    declareQuestionAnswer: Option[Boolean],
    createTaskListSection: () => Seq[TaskListItem],
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
          )
        ) ++ createTaskListSection()

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
    notStartedUrl: String,
    inProgressUrl: String,
    startedOrCompleteUrl: String
  )(implicit messages: Messages): TaskListItem =
    getDeclarationState() match {
      case NotStarted =>
        TaskListItem(
          title = TaskListItemTitle(content = Text(messages(s"taskList.section.$sectionName.notStarted"))),
          status = AlcholDutyTaskListItemStatus.notStarted,
          href = Some(notStartedUrl)
        )
      case InProgress =>
        TaskListItem(
          title = TaskListItemTitle(content = Text(messages(s"taskList.section.$sectionName.inProgress"))),
          status = AlcholDutyTaskListItemStatus.inProgress,
          href = Some(inProgressUrl)
        )
      case Completed  =>
        TaskListItem(
          title = TaskListItemTitle(content = Text(messages(s"taskList.section.$sectionName.completed"))),
          status = AlcholDutyTaskListItemStatus.completed,
          href = Some(startedOrCompleteUrl)
        )
    }

  private def returnJourneyTaskListItem(userAnswers: UserAnswers)(implicit
    messages: Messages
  ): Seq[TaskListItem] =
    for (regime <- AlcoholRegimesViewOrder.regimesInViewOrder(userAnswers.regimes))
      yield (
        userAnswers.getByKey(AlcoholDutyPage, regime),
        userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime)
      ) match {
        case (Some(_), _)    =>
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages(s"taskList.section.returns.$regime"))),
            status = AlcholDutyTaskListItemStatus.completed,
            href = Some(controllers.returns.routes.CheckYourAnswersController.onPageLoad(regime).url)
          )
        case (None, Some(_)) =>
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages(s"taskList.section.returns.$regime"))),
            status = AlcholDutyTaskListItemStatus.inProgress,
            href = Some(controllers.returns.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regime).url)
          )
        case _               =>
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages(s"taskList.section.returns.$regime"))),
            status = AlcholDutyTaskListItemStatus.notStarted,
            href = Some(controllers.returns.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regime).url)
          )

      }

  private def returnAdjustmentJourneyTaskListItem(
    userAnswers: UserAnswers
  )(implicit messages: Messages): TaskListItem = {
    val getDeclarationState = () => {
      (userAnswers.get(AdjustmentListPage), userAnswers.get(AdjustmentEntryListPage)) match {
        case (Some(false), Some(_)) => Completed
        case (_, Some(_))           => InProgress
        case (_, _)                 => NotStarted
      }
    }

    createDeclarationTask(
      getDeclarationState,
      "adjustment",
      controllers.adjustment.routes.AdjustmentListController.onPageLoad().url,
      controllers.adjustment.routes.AdjustmentListController.onPageLoad().url,
      controllers.adjustment.routes.AdjustmentListController.onPageLoad().url
    )
  }

  private def returnDSDJourneyTaskListItem(userAnswers: UserAnswers)(implicit messages: Messages): TaskListItem = {
    val getDeclarationState = () => {
      val regimes             = userAnswers.regimes
      val maybeBeer           = if (regimes.hasBeer()) Some(userAnswers.get(DutySuspendedBeerPage).isDefined) else None
      val maybeCider          = if (regimes.hasCider()) Some(userAnswers.get(DutySuspendedCiderPage).isDefined) else None
      val maybeWine           = if (regimes.hasWine()) Some(userAnswers.get(DutySuspendedWinePage).isDefined) else None
      val maybeSpirits        =
        if (regimes.hasSpirits()) Some(userAnswers.get(DutySuspendedSpiritsPage).isDefined) else None
      val maybeOtherFermented =
        if (regimes.hasOtherFermentedProduct()) Some(userAnswers.get(DutySuspendedOtherFermentedPage).isDefined)
        else None

      val pagesCompleted = Seq(maybeBeer, maybeCider, maybeWine, maybeSpirits, maybeOtherFermented).flatten

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
      controllers.dutySuspended.routes.DutySuspendedDeliveriesGuidanceController.onPageLoad().url,
      controllers.dutySuspended.routes.DutySuspendedDeliveriesGuidanceController.onPageLoad().url,
      controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad().url
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
      controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode).url,
      controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode).url,
      controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url
    )
  }

  def returnSection(userAnswers: UserAnswers)(implicit messages: Messages): Section =
    createSection(
      userAnswers.get(DeclareAlcoholDutyQuestionPage),
      () => returnJourneyTaskListItem(userAnswers),
      controllers.returns.routes.DeclareAlcoholDutyQuestionController.onPageLoad(_).url,
      sectionName = "returns"
    )

  def returnAdjustmentSection(userAnswers: UserAnswers)(implicit messages: Messages): Section =
    createSection(
      userAnswers.get(DeclareAdjustmentQuestionPage),
      () => Seq(returnAdjustmentJourneyTaskListItem(userAnswers)),
      controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(_).url,
      sectionName = "adjustment"
    )

  def returnDSDSection(userAnswers: UserAnswers)(implicit messages: Messages): Section =
    createSection(
      userAnswers.get(DeclareDutySuspendedDeliveriesQuestionPage),
      () => Seq(returnDSDJourneyTaskListItem(userAnswers)),
      controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(_).url,
      sectionName = "dutySuspended"
    )

  def returnQSSection(userAnswers: UserAnswers)(implicit messages: Messages): Section =
    createSection(
      userAnswers.get(DeclareQuarterlySpiritsPage),
      () => Seq(returnQSJourneyTaskListItem(userAnswers)),
      controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(_).url,
      sectionName = "spirits"
    )
}
