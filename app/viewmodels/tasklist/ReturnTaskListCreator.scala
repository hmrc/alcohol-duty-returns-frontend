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

import config.{Constants, FrontendAppConfig}
import models.TaskListSection.{AdjustmentSection, DutySuspendedSection, SpiritsSection}
import models.adjustment.AdjustmentType
import models.{AlcoholRegime, AlcoholRegimes, CheckMode, Mode, NormalMode, SpiritType, TaskListSection, UserAnswers}
import pages.QuestionPage
import pages.adjustment._
import pages.declareDuty.{AlcoholDutyPage, AlcoholTypePage, DeclareAlcoholDutyQuestionPage, WhatDoYouNeedToDeclarePage}
import pages.dutySuspended._
import pages.dutySuspendedNew.{DeclareDutySuspenseQuestionPage, DutySuspendedAlcoholTypePage, DutySuspendedFinalVolumesPage}
import pages.spiritsQuestions._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Hint, TaskList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.{TaskListItem, TaskListItemTitle}
import viewmodels.AlcoholRegimesViewOrder
import viewmodels.tasklist.DeclarationState.{Completed, InProgress, NotStarted}

import javax.inject.Inject

class ReturnTaskListCreator @Inject() (appConfig: FrontendAppConfig) {

  private def createSection(
    declareQuestionAnswer: Option[Boolean],
    additionalItems: Seq[TaskListItem],
    declarationController: Mode => String,
    section: TaskListSection
  )(implicit
    messages: Messages
  ): Section = {
    val mainTaskListItem = declareQuestionAnswer match {
      case Some(_) =>
        TaskListItem(
          title = TaskListItemTitle(content = Text(messages(s"taskList.section.${section.name}.needToDeclare"))),
          hint = addHints(section),
          status = AlcoholDutyTaskListItemStatus.completed,
          href = Some(declarationController(CheckMode))
        )
      case None    =>
        TaskListItem(
          title = TaskListItemTitle(
            content = Text(messages(s"taskList.section.${section.name}.needToDeclare"))
          ),
          hint = addHints(section),
          status = AlcoholDutyTaskListItemStatus.notStarted,
          href = Some(declarationController(NormalMode))
        )
    }

    val additionalTasks = declareQuestionAnswer
      .filter(identity)
      .map(_ => additionalItems)
      .getOrElse(Seq.empty)

    Section(
      title = messages(s"taskList.section.${section.name}.heading"),
      taskList = TaskList(items = mainTaskListItem +: additionalTasks, idPrefix = section.name),
      statusCompleted = AlcoholDutyTaskListItemStatus.completed
    )
  }

  private def addHints(section: TaskListSection)(implicit
    messages: Messages
  ): Option[Hint] =
    section match {
      case SpiritsSection =>
        Some(
          Hint(content = Text(messages(s"taskList.section.${section.name}.hint")))
        )
      case _              => None
    }

  private def createDeclarationTask(
    getDeclarationState: () => DeclarationState,
    section: TaskListSection,
    notStartedUrl: String,
    inProgressUrl: String,
    startedOrCompleteUrl: String
  )(implicit messages: Messages): TaskListItem =
    getDeclarationState() match {
      case NotStarted =>
        TaskListItem(
          title = TaskListItemTitle(content = Text(messages(s"taskList.section.${section.name}"))),
          status = AlcoholDutyTaskListItemStatus.notStarted,
          href = Some(notStartedUrl)
        )
      case InProgress =>
        TaskListItem(
          title = TaskListItemTitle(content = Text(messages(s"taskList.section.${section.name}"))),
          status = AlcoholDutyTaskListItemStatus.inProgress,
          href = Some(inProgressUrl)
        )
      case Completed  =>
        TaskListItem(
          title = TaskListItemTitle(content = Text(messages(s"taskList.section.${section.name}"))),
          status = AlcoholDutyTaskListItemStatus.completed,
          href = Some(startedOrCompleteUrl)
        )
    }

  private def returnsAlcoholByRegimesTask(userAnswers: UserAnswers, alcoholTypes: Set[AlcoholRegime])(implicit
    messages: Messages
  ): Seq[TaskListItem] =
    AlcoholRegimesViewOrder
      .regimesInViewOrder(AlcoholRegimes(alcoholTypes))
      .map(regime =>
        (
          userAnswers.getByKey(AlcoholDutyPage, regime),
          userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime)
        ) match {
          case (Some(_), _)    =>
            TaskListItem(
              title = TaskListItemTitle(content = Text(messages(s"taskList.section.returns.$regime"))),
              status = AlcoholDutyTaskListItemStatus.completed,
              href = Some(controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime).url)
            )
          case (None, Some(_)) =>
            TaskListItem(
              title = TaskListItemTitle(content = Text(messages(s"taskList.section.returns.$regime"))),
              status = AlcoholDutyTaskListItemStatus.inProgress,
              href =
                Some(controllers.declareDuty.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regime).url)
            )
          case _               =>
            TaskListItem(
              title = TaskListItemTitle(content = Text(messages(s"taskList.section.returns.$regime"))),
              status = AlcoholDutyTaskListItemStatus.notStarted,
              href =
                Some(controllers.declareDuty.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regime).url)
            )
        }
      )

  private def returnAdjustmentJourneyTaskListItem(
    userAnswers: UserAnswers
  )(implicit messages: Messages): TaskListItem = {
    val getDeclarationState = () => {
      (
        userAnswers.get(AdjustmentListPage), // No more adjustments to declare
        userAnswers.get(AdjustmentEntryListPage), // Adjustments entered
        userAnswers.get(CurrentAdjustmentEntryPage).flatMap(_.adjustmentType) // Adjustments in progress
      ) match {
        case (Some(false), Some(_), _) => Completed // Declared one and nothing more to declare
        case (_, None, None)           => // Nothing declared and nothing in progress
          NotStarted
        case (_, _, _)                 => InProgress // Something declared or in progress and unknown if more to declare
      }
    }

    val inProgressRoute: String =
      (
        userAnswers.get(AdjustmentEntryListPage),
        userAnswers.get(CurrentAdjustmentEntryPage).flatMap(_.adjustmentType)
      ) match {
        case (Some(_), None) => controllers.adjustment.routes.AdjustmentListController.onPageLoad(1).url
        case (_, _)          => controllers.adjustment.routes.AdjustmentTypeController.onPageLoad(NormalMode).url
      }

    createDeclarationTask(
      getDeclarationState,
      AdjustmentSection,
      controllers.adjustment.routes.AdjustmentTypeController.onPageLoad(NormalMode).url,
      inProgressRoute,
      controllers.adjustment.routes.AdjustmentListController.onPageLoad(1).url
    )
  }

  private def returnAdjustmentJourneyUnderDeclarationTaskListItem(userAnswers: UserAnswers)(implicit
    messages: Messages
  ): TaskListItem =
    createAdjustmentReasonTaskListItem(
      userAnswers,
      AdjustmentType.Underdeclaration,
      UnderDeclarationReasonPage,
      controllers.adjustment.routes.UnderDeclarationReasonController.onPageLoad(NormalMode).url
    )

  private def returnAdjustmentJourneyOverDeclarationTaskListItem(userAnswers: UserAnswers)(implicit
    messages: Messages
  ): TaskListItem =
    createAdjustmentReasonTaskListItem(
      userAnswers,
      AdjustmentType.Overdeclaration,
      OverDeclarationReasonPage,
      controllers.adjustment.routes.OverDeclarationReasonController.onPageLoad(NormalMode).url
    )

  private def createAdjustmentReasonTaskListItem(
    userAnswers: UserAnswers,
    adjustmentType: AdjustmentType,
    reasonPage: QuestionPage[String],
    href: String
  )(implicit messages: Messages): TaskListItem = {
    val title  = TaskListItemTitle(content = Text(messages(s"taskList.section.adjustment.$adjustmentType")))
    val status = userAnswers
      .get(AdjustmentListPage)
      .filter(!_)
      .flatMap(_ => userAnswers.get(reasonPage))
      .fold(AlcoholDutyTaskListItemStatus.notStarted)(_ => AlcoholDutyTaskListItemStatus.completed)

    TaskListItem(
      title = title,
      status = status,
      href = Some(href)
    )
  }

  private def returnDSDJourneyTaskListItem(userAnswers: UserAnswers)(implicit messages: Messages): TaskListItem = {
    val getDeclarationState = () => {
      val regimes             = userAnswers.regimes
      val maybeBeer           = if (regimes.hasBeer) Some(userAnswers.get(DutySuspendedBeerPage).isDefined) else None
      val maybeCider          = if (regimes.hasCider) Some(userAnswers.get(DutySuspendedCiderPage).isDefined) else None
      val maybeWine           = if (regimes.hasWine) Some(userAnswers.get(DutySuspendedWinePage).isDefined) else None
      val maybeSpirits        =
        if (regimes.hasSpirits) Some(userAnswers.get(DutySuspendedSpiritsPage).isDefined) else None
      val maybeOtherFermented =
        if (regimes.hasOtherFermentedProduct) { Some(userAnswers.get(DutySuspendedOtherFermentedPage).isDefined) }
        else { None }

      val pagesCompleted = Seq(maybeBeer, maybeCider, maybeWine, maybeSpirits, maybeOtherFermented).flatten

      getCompletionStatus(pagesCompleted)
    }

    createDeclarationTask(
      getDeclarationState,
      DutySuspendedSection,
      controllers.dutySuspended.routes.DutySuspendedDeliveriesGuidanceController.onPageLoad().url,
      controllers.dutySuspended.routes.DutySuspendedDeliveriesGuidanceController.onPageLoad().url,
      controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad().url
    )
  }

  private def returnDSDNewJourneyTaskListItem(userAnswers: UserAnswers)(implicit messages: Messages): TaskListItem = {
    val getDeclarationState = () => {
      val dutySuspendedAlcoholTypes = userAnswers.get(DutySuspendedAlcoholTypePage).isDefined
      val dutySuspendedVolumes      = userAnswers
        .get(DutySuspendedAlcoholTypePage)
        .fold(false) { alcoholTypes =>
          alcoholTypes.forall(regime => userAnswers.getByKey(DutySuspendedFinalVolumesPage, regime).isDefined)
        }

      val pagesCompleted = Seq(dutySuspendedAlcoholTypes, dutySuspendedVolumes)

      getCompletionStatus(pagesCompleted)
    }

    val incompleteSubTaskUrl = if (userAnswers.regimes.regimes.size > 1) {
      controllers.dutySuspendedNew.routes.DutySuspendedAlcoholTypeController.onPageLoad(NormalMode).url
    } else {
      controllers.dutySuspendedNew.routes.DutySuspendedQuantitiesController
        .onPageLoad(NormalMode, userAnswers.regimes.regimes.head)
        .url
    }

    createDeclarationTask(
      getDeclarationState,
      DutySuspendedSection,
      incompleteSubTaskUrl,
      incompleteSubTaskUrl,
      controllers.dutySuspendedNew.routes.CheckYourAnswersController.onPageLoad().url
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
      val pagesCompleted      = Seq(declareSpiritsTotal, whisky, spiritsType)

      getCompletionStatus(pagesCompleted)
    }

    createDeclarationTask(
      getDeclarationState,
      SpiritsSection,
      controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode).url,
      controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode).url,
      controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url
    )
  }

  def returnCheckAndSubmitSection(completedTasksCount: Int, totalTasksCount: Int)(implicit
    messages: Messages
  ): Section = {

    val (item, status) =
      if (completedTasksCount == totalTasksCount) {
        (
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages("taskList.section.checkAndSubmit.needToDeclare"))),
            status = AlcoholDutyTaskListItemStatus.notStarted,
            href = Some(
              controllers.checkAndSubmit.routes.DutyDueForThisReturnController
                .onPageLoad()
                .url
            )
          ),
          AlcoholDutyTaskListItemStatus.completed
        )
      } else {
        (
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages("taskList.section.checkAndSubmit.needToDeclare"))),
            status = AlcoholDutyTaskListItemStatus.cannotStart,
            href = None,
            hint = Some(Hint(content = Text(messages("taskList.section.checkAndSubmit.hint"))))
          ),
          AlcoholDutyTaskListItemStatus.completed
        )
      }
    Section(
      title = messages("taskList.section.checkAndSubmit.heading"),
      taskList = TaskList(items = Seq(item), idPrefix = "checkAndSubmit"),
      statusCompleted = status
    )
  }

  def returnSection(userAnswers: UserAnswers)(implicit messages: Messages): Section = {
    val taskListItems = (userAnswers.get(DeclareAlcoholDutyQuestionPage), userAnswers.get(AlcoholTypePage)) match {
      case (Some(true), Some(alcoholType)) =>
        val declareAlcoholQuestionTask =
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages("taskList.section.returns.needToDeclare"))),
            status = AlcoholDutyTaskListItemStatus.completed,
            href = Some(
              controllers.declareDuty.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
            )
          )
        declareAlcoholQuestionTask +: returnsAlcoholByRegimesTask(userAnswers, alcoholType)
      case (Some(true), None)              =>
        Seq(
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages("taskList.section.returns.needToDeclare"))),
            status = AlcoholDutyTaskListItemStatus.inProgress,
            href = Some(
              controllers.declareDuty.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
            )
          )
        )
      case (Some(false), _)                =>
        Seq(
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages("taskList.section.returns.needToDeclare"))),
            status = AlcoholDutyTaskListItemStatus.completed,
            href = Some(
              controllers.declareDuty.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
            )
          )
        )
      case (_, _)                          =>
        Seq(
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages("taskList.section.returns.needToDeclare"))),
            status = AlcoholDutyTaskListItemStatus.notStarted,
            href = Some(
              controllers.declareDuty.routes.DeclareAlcoholDutyQuestionController.onPageLoad(NormalMode).url
            )
          )
        )
    }

    Section(
      title = messages("taskList.section.returns.heading"),
      taskList = TaskList(items = taskListItems, idPrefix = "returns"),
      statusCompleted = AlcoholDutyTaskListItemStatus.completed
    )
  }

  def returnAdjustmentSection(userAnswers: UserAnswers)(implicit messages: Messages): Section = {
    val overDeclarationTotal   = userAnswers.get(OverDeclarationTotalPage).getOrElse(BigDecimal(0))
    val underDeclarationTotal  = userAnswers.get(UnderDeclarationTotalPage).getOrElse(BigDecimal(0))
    val adjustmentListQuestion = userAnswers.get(AdjustmentListPage).getOrElse(true)
    val taskListItems          = Seq(
      Some(returnAdjustmentJourneyTaskListItem(userAnswers)),
      if (underDeclarationTotal >= Constants.overUnderDeclarationThreshold && !adjustmentListQuestion) {
        Some(returnAdjustmentJourneyUnderDeclarationTaskListItem(userAnswers))
      } else { None },
      if (overDeclarationTotal.abs >= Constants.overUnderDeclarationThreshold && !adjustmentListQuestion) {
        Some(returnAdjustmentJourneyOverDeclarationTaskListItem(userAnswers))
      } else { None }
    ).flatten
    createSection(
      userAnswers.get(DeclareAdjustmentQuestionPage),
      taskListItems,
      controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(_).url,
      section = AdjustmentSection
    )
  }

  def returnDSDSection(userAnswers: UserAnswers)(implicit messages: Messages): Section =
    if (appConfig.dutySuspendedNewJourneyEnabled) {
      createSection(
        userAnswers.get(DeclareDutySuspenseQuestionPage),
        Seq(returnDSDNewJourneyTaskListItem(userAnswers)),
        controllers.dutySuspendedNew.routes.DeclareDutySuspenseQuestionController.onPageLoad(_).url,
        section = DutySuspendedSection
      )
    } else {
      createSection(
        userAnswers.get(DeclareDutySuspendedDeliveriesQuestionPage),
        Seq(returnDSDJourneyTaskListItem(userAnswers)),
        controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(_).url,
        section = DutySuspendedSection
      )
    }

  def returnQSSection(userAnswers: UserAnswers)(implicit messages: Messages): Section =
    createSection(
      userAnswers.get(DeclareQuarterlySpiritsPage),
      Seq(returnQSJourneyTaskListItem(userAnswers)),
      controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(_).url,
      section = SpiritsSection
    )

  private def getCompletionStatus(pagesCompleted: Seq[Boolean]): DeclarationState =
    if (pagesCompleted.forall(!_)) {
      NotStarted
    } else if (pagesCompleted.forall(identity)) {
      Completed
    } else {
      InProgress
    }
}
