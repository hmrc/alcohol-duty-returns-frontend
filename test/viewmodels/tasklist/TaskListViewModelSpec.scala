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
import models.{AlcoholRegimes, ReturnPeriod}
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.TaskListItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.TaskList

import java.time.{Clock, Instant, ZoneId}
import java.time.temporal.ChronoUnit

class TaskListViewModelSpec extends SpecBase {
  "TaskListViewModel" - {
    "must return an incomplete task list if not all sections are complete" in new SetUp {
      when(mockReturnTaskListCreator.returnSection(userAnswers)).thenReturn(notStartedSection)
      when(mockReturnTaskListCreator.returnAdjustmentSection(userAnswers)).thenReturn(notStartedSection)
      when(mockReturnTaskListCreator.returnDSDSection(userAnswers)).thenReturn(inProgressSection)
      when(mockReturnTaskListCreator.returnQSSection(userAnswers)).thenReturn(completeSection)
      when(mockReturnTaskListCreator.returnCheckAndSubmitSection(1, 4)).thenReturn(cannotStartSection)

      val result = taskListViewModel.getTaskList(userAnswers, validUntil, returnPeriod)

      result mustBe AlcoholDutyTaskList(
        Seq(notStartedSection, notStartedSection, inProgressSection, completeSection, cannotStartSection),
        fromDateString,
        toDateString,
        dueDateString,
        validUntilString
      )

      result.status mustBe Incomplete
      result.totalTasks mustBe 5
      result.completedTasks mustBe 1
    }

    "must return an incomplete task list if all sections other than check and submit are complete" in new SetUp {
      when(mockReturnTaskListCreator.returnSection(userAnswers)).thenReturn(completeSection)
      when(mockReturnTaskListCreator.returnAdjustmentSection(userAnswers)).thenReturn(completeSection)
      when(mockReturnTaskListCreator.returnDSDSection(userAnswers)).thenReturn(completeSection)
      when(mockReturnTaskListCreator.returnQSSection(userAnswers)).thenReturn(completeSection)
      when(mockReturnTaskListCreator.returnCheckAndSubmitSection(4, 4)).thenReturn(notStartedSection)

      val result = taskListViewModel.getTaskList(userAnswers, validUntil, returnPeriod)

      result mustBe AlcoholDutyTaskList(
        Seq(completeSection, completeSection, completeSection, completeSection, notStartedSection),
        fromDateString,
        toDateString,
        dueDateString,
        validUntilString
      )

      result.status mustBe Incomplete
      result.totalTasks mustBe 5
      result.completedTasks mustBe 4
    }

    "must not return the QS section when the feature toggle is off" in new SetUp(
      spiritsAndIngredientsEnabledFeatureToggle = false
    ) {
      when(mockReturnTaskListCreator.returnSection(userAnswers)).thenReturn(notStartedSection)
      when(mockReturnTaskListCreator.returnAdjustmentSection(userAnswers)).thenReturn(notStartedSection)
      when(mockReturnTaskListCreator.returnDSDSection(userAnswers)).thenReturn(inProgressSection)
      when(mockReturnTaskListCreator.returnQSSection(userAnswers)).thenReturn(completeSection)
      when(mockReturnTaskListCreator.returnCheckAndSubmitSection(0, 3)).thenReturn(cannotStartSection)

      val result = taskListViewModel.getTaskList(userAnswers, validUntil, returnPeriod)

      result mustBe AlcoholDutyTaskList(
        Seq(notStartedSection, notStartedSection, inProgressSection, cannotStartSection),
        fromDateString,
        toDateString,
        dueDateString,
        validUntilString
      )

      result.totalTasks mustBe 4

      verify(mockReturnTaskListCreator, never).returnQSSection(userAnswers)
    }

    quarterReturnPeriods.foreach { returnPeriodUnderTest =>
      val periodKeyUnderTest = returnPeriodUnderTest.toPeriodKey
      s"must return the QS section as the period key $periodKeyUnderTest falls on a quarter and the producer has the regime 'Spirits'" in new SetUp {
        val userAnswersForPeriod  = userAnswers.copy(returnId = returnId.copy(periodKey = periodKeyUnderTest))
        val quarterFromDate       = returnPeriodUnderTest.periodFromDate()
        val quarterToDate         = returnPeriodUnderTest.periodToDate()
        val quarterDueDate        = returnPeriodUnderTest.periodDueDate()
        val quarterFromDateString = dateTimeHelper.formatDateMonthYear(quarterFromDate)
        val quarterToDateString   = dateTimeHelper.formatDateMonthYear(quarterToDate)
        val quarterDueDateString  = dateTimeHelper.formatDateMonthYear(quarterDueDate)

        when(mockReturnTaskListCreator.returnSection(userAnswersForPeriod)).thenReturn(notStartedSection)
        when(mockReturnTaskListCreator.returnAdjustmentSection(userAnswersForPeriod)).thenReturn(notStartedSection)
        when(mockReturnTaskListCreator.returnDSDSection(userAnswersForPeriod)).thenReturn(inProgressSection)
        when(mockReturnTaskListCreator.returnQSSection(userAnswersForPeriod)).thenReturn(completeSection)
        when(mockReturnTaskListCreator.returnCheckAndSubmitSection(1, 4)).thenReturn(cannotStartSection)

        val result = taskListViewModel.getTaskList(userAnswersForPeriod, validUntil, returnPeriodUnderTest)

        result mustBe AlcoholDutyTaskList(
          Seq(notStartedSection, notStartedSection, inProgressSection, completeSection, cannotStartSection),
          quarterFromDateString,
          quarterToDateString,
          quarterDueDateString,
          validUntilString
        )

        result.totalTasks mustBe 5

        verify(mockReturnTaskListCreator, times(1)).returnQSSection(userAnswersForPeriod)
      }
    }

    "must not return the QS section as the producer doesn't have the 'Spirits' regime" in new SetUp {
      val userAnswersWithoutSpirits =
        userAnswers.copy(regimes = AlcoholRegimes(Set(Beer, Cider, Wine, OtherFermentedProduct)))
      when(mockReturnTaskListCreator.returnSection(userAnswersWithoutSpirits)).thenReturn(notStartedSection)
      when(mockReturnTaskListCreator.returnAdjustmentSection(userAnswersWithoutSpirits)).thenReturn(notStartedSection)
      when(mockReturnTaskListCreator.returnDSDSection(userAnswersWithoutSpirits)).thenReturn(inProgressSection)
      when(mockReturnTaskListCreator.returnQSSection(userAnswersWithoutSpirits)).thenReturn(completeSection)
      when(mockReturnTaskListCreator.returnCheckAndSubmitSection(0, 3)).thenReturn(cannotStartSection)

      val result = taskListViewModel.getTaskList(userAnswersWithoutSpirits, validUntil, returnPeriod)

      result mustBe AlcoholDutyTaskList(
        Seq(notStartedSection, notStartedSection, inProgressSection, cannotStartSection),
        fromDateString,
        toDateString,
        dueDateString,
        validUntilString
      )

      result.totalTasks mustBe 4

      verify(mockReturnTaskListCreator, never).returnQSSection(userAnswers)
    }

    nonQuarterReturnPeriods.foreach { returnPeriodUnderTest =>
      val periodKeyUnderTest = returnPeriodUnderTest.toPeriodKey
      s"must not return the QS section as the period key $periodKeyUnderTest doesn't fall on a quarter" in new SetUp {
        val userAnswersForPeriod     = userAnswers.copy(returnId = returnId.copy(periodKey = periodKeyUnderTest))
        val nonQuarterFromDate       = returnPeriodUnderTest.periodFromDate()
        val nonQuarterToDate         = returnPeriodUnderTest.periodToDate()
        val nonQuarterDueDate        = returnPeriodUnderTest.periodDueDate()
        val nonQuarterFromDateString = dateTimeHelper.formatDateMonthYear(nonQuarterFromDate)
        val nonQuarterToDateString   = dateTimeHelper.formatDateMonthYear(nonQuarterToDate)
        val nonQuarterDueDateString  = dateTimeHelper.formatDateMonthYear(nonQuarterDueDate)

        when(mockReturnTaskListCreator.returnSection(userAnswersForPeriod)).thenReturn(notStartedSection)
        when(mockReturnTaskListCreator.returnAdjustmentSection(userAnswersForPeriod)).thenReturn(notStartedSection)
        when(mockReturnTaskListCreator.returnDSDSection(userAnswersForPeriod)).thenReturn(inProgressSection)
        when(mockReturnTaskListCreator.returnQSSection(userAnswersForPeriod)).thenReturn(completeSection)
        when(mockReturnTaskListCreator.returnCheckAndSubmitSection(0, 3)).thenReturn(cannotStartSection)

        val result = taskListViewModel.getTaskList(userAnswersForPeriod, validUntil, returnPeriodUnderTest)

        result mustBe AlcoholDutyTaskList(
          Seq(notStartedSection, notStartedSection, inProgressSection, cannotStartSection),
          nonQuarterFromDateString,
          nonQuarterToDateString,
          nonQuarterDueDateString,
          validUntilString
        )

        result.totalTasks mustBe 4

        verify(mockReturnTaskListCreator, never).returnQSSection(userAnswers)
      }
    }
  }

  class SetUp(spiritsAndIngredientsEnabledFeatureToggle: Boolean = true) {
    val additionalConfig             = Map("features.spirits-and-ingredients" -> spiritsAndIngredientsEnabledFeatureToggle)
    val application: Application     = applicationBuilder().configure(additionalConfig).build()
    val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
    implicit val messages: Messages  = getMessages(application)

    val returnId       = emptyUserAnswers.returnId.copy(periodKey = periodKeyJan)
    val userAnswers    = emptyUserAnswers.copy(returnId = returnId)
    val returnPeriod   = ReturnPeriod.fromPeriodKeyOrThrow(periodKeyJan)
    val dateTimeHelper = createDateTimeHelper()

    private val instant      = Instant.now.truncatedTo(ChronoUnit.MILLIS)
    private val clock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

    val fromDate         = returnPeriod.periodFromDate()
    val toDate           = returnPeriod.periodToDate()
    val dueDate          = returnPeriod.periodDueDate()
    val validUntil       = Instant.now(clock)
    val fromDateString   = dateTimeHelper.formatDateMonthYear(fromDate)
    val toDateString     = dateTimeHelper.formatDateMonthYear(toDate)
    val dueDateString    = dateTimeHelper.formatDateMonthYear(dueDate)
    val validUntilString =
      dateTimeHelper.formatDateMonthYear(dateTimeHelper.instantToLocalDate(validUntil))

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
    val taskListViewModel         = new TaskListViewModel(dateTimeHelper, mockReturnTaskListCreator, appConfig)
  }
}
