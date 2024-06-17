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

import models.AlcoholRegimeName.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.{AlcoholRegimeName, CheckMode, Mode, NormalMode, SpiritType, UserAnswers}
import pages.dutySuspended.{DeclareDutySuspendedDeliveriesQuestionPage, DutySuspendedBeerPage, DutySuspendedCiderPage, DutySuspendedOtherFermentedPage, DutySuspendedSpiritsPage, DutySuspendedWinePage}
import pages.returns.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage, WhatDoYouNeedToDeclarePage}
import pages.spiritsQuestions.{AlcoholUsedPage, DeclareQuarterlySpiritsPage, DeclareSpiritsTotalPage, EthyleneGasOrMolassesUsedPage, GrainsUsedPage, OtherIngredientsUsedPage, OtherMaltedGrainsPage, OtherSpiritsProducedPage, SpiritTypePage, WhiskyPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.TaskList
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.{TaskListItem, TaskListItemTitle}
import viewmodels.tasklist.DeclarationState.{Completed, InProgress, NotStarted}
import viewmodels.tasklist.ReturnTaskListHelper.{alcoholRegimeViewOrder, returnJourneyTaskListItems}

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

  private val alcoholRegimeViewOrder: Seq[AlcoholRegimeName] = Seq(Beer, Cider, Wine, Spirits, OtherFermentedProduct)

  private def returnJourneyTaskListItem(regimes: Seq[AlcoholRegimeName], userAnswers: UserAnswers)(implicit messages: Messages): Seq[TaskListItem] = {
      for (regime <- regimes.sortBy(alcoholRegimeViewOrder.indexOf))
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

//def returnSection(regimes: Seq[AlcoholRegimeName], userAnswers: UserAnswers)(implicit messages: Messages): Section =
//  createSection(
//    userAnswers.get(DeclareAlcoholDutyQuestionPage),
//    () => returnJourneyTaskListItem(regimes, userAnswers),
//    controllers.returns.routes.DeclareAlcoholDutyQuestionController.onPageLoad(_).url,
//    sectionName = "returns"
//  )

  def returnSection(regimes: Seq[AlcoholRegimeName], userAnswers: UserAnswers)(implicit messages: Messages): Section = {

    val declareDutyQuestion = userAnswers.get(DeclareAlcoholDutyQuestionPage) match {
      case Some(true)  =>
        Seq(
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages("taskList.section.returns.needToDeclare.yes"))),
            status = AlcholDutyTaskListItemStatus.completed,
            href = Some(controllers.returns.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url)
          )
        ) ++ returnJourneyTaskListItems(regimes, userAnswers)
      case Some(false) =>
        Seq(
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages("taskList.section.returns.needToDeclare.no"))),
            status = AlcholDutyTaskListItemStatus.completed,
            href = Some(controllers.returns.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url)
          )
        )
      case None        =>
        Seq(
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages("taskList.section.returns.needToDeclare.notStarted"))),
            status = AlcholDutyTaskListItemStatus.notStarted,
            href = Some(controllers.returns.routes.DeclareAlcoholDutyQuestionController.onPageLoad(NormalMode).url)
          )
        )
    }

    Section(
      title = messages("taskList.section.returns.heading"),
      taskList = TaskList(items = declareDutyQuestion),
      statusCompleted = AlcholDutyTaskListItemStatus.completed
    )
  }


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
