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
import TaskListStatus.Incomplete
import config.FrontendAppConfig
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Wine}
import models.AlcoholRegimes
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.TaskListItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.TaskList

import java.time.{Clock, Instant, ZoneId}
import java.time.temporal.ChronoUnit

class TaskListViewModelSpec extends SpecBase {
  "TaskListViewModel" - {
    "must return an incomplete task list if not all sections are complete" in new SetUp {
      when(mockReturnTaskListCreator.returnSection(emptyUserAnswers)).thenReturn(notStartedSection)
      when(mockReturnTaskListCreator.returnAdjustmentSection(emptyUserAnswers)).thenReturn(notStartedSection)
      when(mockReturnTaskListCreator.returnDSDSection(emptyUserAnswers)).thenReturn(inProgressSection)
      when(mockReturnTaskListCreator.returnQSSection(emptyUserAnswers)).thenReturn(completeSection)
      when(mockReturnTaskListCreator.returnCheckAndSubmitSection(1, 4)).thenReturn(cannotStartSection)

      val result = taskListViewModel.getTaskList(emptyUserAnswers, validUntil, returnPeriodJan)

      result mustBe AlcoholDutyTaskList(
        Seq(notStartedSection, notStartedSection, inProgressSection, completeSection, cannotStartSection),
        validUntilString
      )

      result.status mustBe Incomplete
      result.totalTasks mustBe 5
      result.completedTasks mustBe 1
    }

    "must return an incomplete task list if all sections other than check and submit are complete" in new SetUp {
      when(mockReturnTaskListCreator.returnSection(emptyUserAnswers)).thenReturn(completeSection)
      when(mockReturnTaskListCreator.returnAdjustmentSection(emptyUserAnswers)).thenReturn(completeSection)
      when(mockReturnTaskListCreator.returnDSDSection(emptyUserAnswers)).thenReturn(completeSection)
      when(mockReturnTaskListCreator.returnQSSection(emptyUserAnswers)).thenReturn(completeSection)
      when(mockReturnTaskListCreator.returnCheckAndSubmitSection(4, 4)).thenReturn(notStartedSection)

      val result = taskListViewModel.getTaskList(emptyUserAnswers, validUntil, returnPeriodJan)

      result mustBe AlcoholDutyTaskList(
        Seq(completeSection, completeSection, completeSection, completeSection, notStartedSection),
        validUntilString
      )

      result.status mustBe Incomplete
      result.totalTasks mustBe 5
      result.completedTasks mustBe 4
    }

    "must not return the QS section when the feature toggle is off" in new SetUp(false) {
      when(mockReturnTaskListCreator.returnSection(emptyUserAnswers)).thenReturn(notStartedSection)
      when(mockReturnTaskListCreator.returnAdjustmentSection(emptyUserAnswers)).thenReturn(notStartedSection)
      when(mockReturnTaskListCreator.returnDSDSection(emptyUserAnswers)).thenReturn(inProgressSection)
      when(mockReturnTaskListCreator.returnQSSection(emptyUserAnswers)).thenReturn(completeSection)
      when(mockReturnTaskListCreator.returnCheckAndSubmitSection(0, 3)).thenReturn(cannotStartSection)

      val result = taskListViewModel.getTaskList(emptyUserAnswers, validUntil, returnPeriodJan)

      result mustBe AlcoholDutyTaskList(
        Seq(notStartedSection, notStartedSection, inProgressSection, cannotStartSection),
        validUntilString
      )

      result.totalTasks mustBe 4

      verify(mockReturnTaskListCreator, never).returnQSSection(emptyUserAnswers)
    }

    quarterReturnPeriods.foreach { returnPeriod =>
      s"must return the QS section as the period key $returnPeriod falls on a quarter and the producer has the regime 'Spirits'" in new SetUp {
        when(mockReturnTaskListCreator.returnSection(emptyUserAnswers)).thenReturn(notStartedSection)
        when(mockReturnTaskListCreator.returnAdjustmentSection(emptyUserAnswers)).thenReturn(notStartedSection)
        when(mockReturnTaskListCreator.returnDSDSection(emptyUserAnswers)).thenReturn(inProgressSection)
        when(mockReturnTaskListCreator.returnQSSection(emptyUserAnswers)).thenReturn(completeSection)
        when(mockReturnTaskListCreator.returnCheckAndSubmitSection(1, 4)).thenReturn(cannotStartSection)

        val result = taskListViewModel.getTaskList(emptyUserAnswers, validUntil, returnPeriod)

        result mustBe AlcoholDutyTaskList(
          Seq(notStartedSection, notStartedSection, inProgressSection, completeSection, cannotStartSection),
          validUntilString
        )

        result.totalTasks mustBe 5

        verify(mockReturnTaskListCreator, times(1)).returnQSSection(emptyUserAnswers)
      }
    }

    quarterReturnPeriods.foreach { returnPeriod =>
      s"must not return the QS section as the period key $returnPeriod doesn't fall on a quarter" in new SetUp {
        val userAnswers = emptyUserAnswers.copy(regimes = AlcoholRegimes(Set(Beer, Cider, Wine, OtherFermentedProduct)))
        when(mockReturnTaskListCreator.returnSection(userAnswers)).thenReturn(notStartedSection)
        when(mockReturnTaskListCreator.returnAdjustmentSection(userAnswers)).thenReturn(notStartedSection)
        when(mockReturnTaskListCreator.returnDSDSection(userAnswers)).thenReturn(inProgressSection)
        when(mockReturnTaskListCreator.returnQSSection(userAnswers)).thenReturn(completeSection)
        when(mockReturnTaskListCreator.returnCheckAndSubmitSection(0, 3)).thenReturn(cannotStartSection)

        val result = taskListViewModel.getTaskList(userAnswers, validUntil, returnPeriod)

        result mustBe AlcoholDutyTaskList(
          Seq(notStartedSection, notStartedSection, inProgressSection, cannotStartSection),
          validUntilString
        )

        result.totalTasks mustBe 4

        verify(mockReturnTaskListCreator, never).returnQSSection(userAnswers)
      }
    }

    nonQuarterReturnPeriods.foreach { periodKey =>
      s"must not return the QS section as the period key $periodKey fall on a quarter but the producer doesn't have the regime 'Spirits'" in new SetUp {
        when(mockReturnTaskListCreator.returnSection(emptyUserAnswers)).thenReturn(notStartedSection)
        when(mockReturnTaskListCreator.returnAdjustmentSection(emptyUserAnswers)).thenReturn(notStartedSection)
        when(mockReturnTaskListCreator.returnDSDSection(emptyUserAnswers)).thenReturn(inProgressSection)
        when(mockReturnTaskListCreator.returnQSSection(emptyUserAnswers)).thenReturn(completeSection)
        when(mockReturnTaskListCreator.returnCheckAndSubmitSection(0, 3)).thenReturn(cannotStartSection)

        val result = taskListViewModel.getTaskList(emptyUserAnswers, validUntil, periodKey)

        result mustBe AlcoholDutyTaskList(
          Seq(notStartedSection, notStartedSection, inProgressSection, cannotStartSection),
          validUntilString
        )

        result.totalTasks mustBe 4

        verify(mockReturnTaskListCreator, never).returnQSSection(emptyUserAnswers)
      }
    }
  }

  class SetUp(spiritsAndIngredientsEnabledFeatureToggle: Boolean = true) {
    val additionalConfig             = Map("features.spirits-and-ingredients" -> spiritsAndIngredientsEnabledFeatureToggle)
    val application: Application     = applicationBuilder().configure(additionalConfig).build()
    val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
    implicit val msgs: Messages      = getMessages(application)

    private val instant      = Instant.now.truncatedTo(ChronoUnit.MILLIS)
    private val clock: Clock = Clock.fixed(instant, ZoneId.systemDefault)
    val validUntil           = Instant.now(clock)
    val validUntilString     =
      createDateTimeHelper().formatDateMonthYear(createDateTimeHelper().instantToLocalDate(validUntil))

    val completedStatus = AlcoholDutyTaskListItemStatus.completed

    val notStartedTaskList  = TaskList(items = Seq(TaskListItem(status = AlcoholDutyTaskListItemStatus.notStarted)))
    val inProgressTaskList  = TaskList(items = Seq(TaskListItem(status = AlcoholDutyTaskListItemStatus.inProgress)))
    val completeTaskList    = TaskList(items = Seq(TaskListItem(status = AlcoholDutyTaskListItemStatus.completed)))
    val cannotStartTaskList = TaskList(items = Seq(TaskListItem(status = AlcoholDutyTaskListItemStatus.cannotStart)))

    val notStartedSection  = Section("title", notStartedTaskList, completedStatus)
    val inProgressSection  = Section("title", inProgressTaskList, completedStatus)
    val completeSection    = Section("title", completeTaskList, completedStatus)
    val cannotStartSection = Section("title", cannotStartTaskList, AlcoholDutyTaskListItemStatus.notStarted)

    val mockReturnTaskListCreator = mock[ReturnTaskListCreator]
    val taskListViewModel         = new TaskListViewModel(createDateTimeHelper(), mockReturnTaskListCreator, appConfig)
  }
}
