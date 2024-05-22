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

import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.RateType.{Core, DraughtAndSmallProducerRelief, DraughtRelief, SmallProducerRelief}
import models.{AlcoholRegime, CheckMode, NormalMode, RateBand, UserAnswers}
import pages.dutySuspended.{DeclareDutySuspendedDeliveriesQuestionPage, DutySuspendedBeerPage, DutySuspendedCiderPage, DutySuspendedOtherFermentedPage, DutySuspendedSpiritsPage, DutySuspendedWinePage}
import pages.returns.{DeclareAlcoholDutyQuestionPage, HowMuchDoYouNeedToDeclarePage, WhatDoYouNeedToDeclarePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.TaskList
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.{TaskListItem, TaskListItemTitle}

object ReturnTaskListHelper {
  def returnSection(regimes: Seq[AlcoholRegime], userAnswers: UserAnswers)(implicit messages: Messages): Section = {

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

  private val alcoholRegimeViewOrder: Seq[AlcoholRegime] = Seq(Beer, Cider, Wine, Spirits, OtherFermentedProduct)

  private def returnJourneyTaskListItems(regimes: Seq[AlcoholRegime], userAnswers: UserAnswers)(implicit
    messages: Messages
  ): Seq[TaskListItem] =
    for (regime <- regimes.sortBy(alcoholRegimeViewOrder.indexOf))
      yield (userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime)) match {
        case Some(rateBands) if rateBands.nonEmpty =>
          returnJourneyCompletionTaskListItem(regime, rateBands, userAnswers)
        case None                                  =>
          TaskListItem(
            title = TaskListItemTitle(content = Text(messages(s"taskList.section.returns.$regime"))),
            status = AlcholDutyTaskListItemStatus.notStarted,
            href = Some(controllers.returns.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regime).url)
          )
      }

  // TODO: refactor this
  private def returnJourneyCompletionTaskListItem(
    regime: AlcoholRegime,
    rateBands: Set[RateBand],
    userAnswers: UserAnswers
  )(implicit
    messages: Messages
  ): TaskListItem = {
    val rateTypes = rateBands.map(_.rateType)

    val coreAndDraughtCompleted = if (rateTypes.intersect(Set(Core, DraughtRelief)).nonEmpty) {
      userAnswers.getByKey(HowMuchDoYouNeedToDeclarePage, regime) match {
        case Some(duties) if duties.nonEmpty => true
        case None                            => false
      }
    } else true

    val sprCompleted =
      if (
        rateTypes
          .intersect(Set(SmallProducerRelief, DraughtAndSmallProducerRelief))
          .nonEmpty
      ) false
      else true // TODO: update when SPR is developed

    if (coreAndDraughtCompleted && sprCompleted) {
      TaskListItem(
        title = TaskListItemTitle(content = Text(messages(s"taskList.section.returns.$regime"))),
        status = AlcholDutyTaskListItemStatus.completed,
        href = Some(controllers.returns.routes.CheckYourAnswersController.onPageLoad(regime).url)
      )
    } else {
      TaskListItem(
        title = TaskListItemTitle(content = Text(messages(s"taskList.section.returns.$regime"))),
        status = AlcholDutyTaskListItemStatus.inProgress,
        href = Some(controllers.returns.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regime).url)
      )
    }

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
