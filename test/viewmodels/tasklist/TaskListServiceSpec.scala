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
import models.TaskListStatus.{Completed, Incomplete}
import play.api.Application
import play.api.i18n.Messages
import services.tasklist.TaskListService
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.{TaskList, TaskListItem}
import viewmodels.govuk.all.FluentInstant

import java.time.{Clock, Instant, ZoneId}
import java.time.temporal.ChronoUnit

class TaskListServiceSpec extends SpecBase {
  "TaskListService" - {
    "must return an incomplete task list if not all the sections are complete" in new SetUp {
      when(mockReturnTaskListCreator.returnSection(emptyUserAnswers)).thenReturn(Right(notStartedSection))
      when(mockReturnTaskListCreator.returnDSDSection(emptyUserAnswers)).thenReturn(Right(inProgressSection))
      when(mockReturnTaskListCreator.returnQSSection(emptyUserAnswers)).thenReturn(Right(completeSection))

      val result = taskListService.getTaskList(emptyUserAnswers, validUntil, periodKeyMar).toOption.get

      result mustBe AlcoholDutyTaskList(
        Seq(notStartedSection, inProgressSection, completeSection),
        validUntilString
      )

      result.status mustBe Incomplete
      result.totalTasks mustBe 3
      result.completedTasks mustBe 1
    }

    "must return an complete task list if all the sections are complete" in new SetUp {
      when(mockReturnTaskListCreator.returnSection(emptyUserAnswers)).thenReturn(Right(completeSection))
      when(mockReturnTaskListCreator.returnDSDSection(emptyUserAnswers)).thenReturn(Right(completeSection))
      when(mockReturnTaskListCreator.returnQSSection(emptyUserAnswers)).thenReturn(Right(completeSection))

      val result = taskListService.getTaskList(emptyUserAnswers, validUntil, periodKeyMar).toOption.get

      result mustBe AlcoholDutyTaskList(
        Seq(completeSection, completeSection, completeSection),
        validUntilString
      )

      result.status mustBe Completed
      result.totalTasks mustBe 3
      result.completedTasks mustBe 3
    }

    "must return an error task list if the return section errors" in new SetUp {
      val errorMsg = "error"

      when(mockReturnTaskListCreator.returnSection(emptyUserAnswers))
        .thenReturn(Left(new RuntimeException(errorMsg)))
      when(mockReturnTaskListCreator.returnDSDSection(emptyUserAnswers)).thenReturn(Right(completeSection))
      when(mockReturnTaskListCreator.returnQSSection(emptyUserAnswers)).thenReturn(Right(completeSection))

      taskListService
        .getTaskList(emptyUserAnswers, validUntil, periodKeyMar)
        .swap
        .toOption
        .get
        .getMessage mustBe errorMsg
    }

    "must return an error task list if the DSD section errors" in new SetUp {
      val errorMsg = "error"

      when(mockReturnTaskListCreator.returnSection(emptyUserAnswers)).thenReturn(Right(completeSection))
      when(mockReturnTaskListCreator.returnDSDSection(emptyUserAnswers))
        .thenReturn(Left(new RuntimeException(errorMsg)))
      when(mockReturnTaskListCreator.returnQSSection(emptyUserAnswers)).thenReturn(Right(completeSection))

      taskListService
        .getTaskList(emptyUserAnswers, validUntil, periodKeyMar)
        .swap
        .toOption
        .get
        .getMessage mustBe errorMsg
    }

    "must return an error task list if the QS section errors" in new SetUp {
      val errorMsg = "error"

      when(mockReturnTaskListCreator.returnSection(emptyUserAnswers)).thenReturn(Right(completeSection))
      when(mockReturnTaskListCreator.returnDSDSection(emptyUserAnswers)).thenReturn(Right(completeSection))
      when(mockReturnTaskListCreator.returnQSSection(emptyUserAnswers))
        .thenReturn(Left(new RuntimeException(errorMsg)))

      taskListService
        .getTaskList(emptyUserAnswers, validUntil, periodKeyMar)
        .swap
        .toOption
        .get
        .getMessage mustBe errorMsg
    }

    quarterPeriodKeys.foreach { periodKey =>
      s"must return the QS section as the period key $periodKey falls on a quarter" in new SetUp {
        when(mockReturnTaskListCreator.returnSection(emptyUserAnswers)).thenReturn(Right(notStartedSection))
        when(mockReturnTaskListCreator.returnDSDSection(emptyUserAnswers)).thenReturn(Right(inProgressSection))
        when(mockReturnTaskListCreator.returnQSSection(emptyUserAnswers)).thenReturn(Right(completeSection))

        val result = taskListService.getTaskList(emptyUserAnswers, validUntil, periodKey).toOption.get

        result mustBe AlcoholDutyTaskList(
          Seq(notStartedSection, inProgressSection, completeSection),
          validUntilString
        )

        result.totalTasks mustBe 3
      }
    }

    nonQuarterPeriodKeys.foreach { periodKey =>
      s"must not return the QS section as the period key $periodKey doesn't fall on a quarter" in new SetUp {
        when(mockReturnTaskListCreator.returnSection(emptyUserAnswers)).thenReturn(Right(notStartedSection))
        when(mockReturnTaskListCreator.returnDSDSection(emptyUserAnswers)).thenReturn(Right(inProgressSection))
        when(mockReturnTaskListCreator.returnQSSection(emptyUserAnswers)).thenReturn(Right(completeSection))

        val result = taskListService.getTaskList(emptyUserAnswers, validUntil, periodKey).toOption.get

        result mustBe AlcoholDutyTaskList(
          Seq(notStartedSection, inProgressSection),
          validUntilString
        )

        result.totalTasks mustBe 2
      }
    }
  }

  class SetUp {
    val application: Application = applicationBuilder().build()
    implicit val msgs: Messages  = messages(application)

    private val instant      = Instant.now.truncatedTo(ChronoUnit.MILLIS)
    private val clock: Clock = Clock.fixed(instant, ZoneId.systemDefault)
    val validUntil           = Instant.now(clock)
    val validUntilString     = validUntil.toLocalDateString()

    val completedStatus = AlcholDutyTaskListItemStatus.completed

    val notStartedTaskList = TaskList(Seq(TaskListItem(status = AlcholDutyTaskListItemStatus.notStarted)))
    val inProgressTaskList = TaskList(Seq(TaskListItem(status = AlcholDutyTaskListItemStatus.inProgress)))
    val completeTaskList   = TaskList(Seq(TaskListItem(status = AlcholDutyTaskListItemStatus.completed)))

    val notStartedSection = Section("title", notStartedTaskList, completedStatus)
    val inProgressSection = Section("title", inProgressTaskList, completedStatus)
    val completeSection   = Section("title", completeTaskList, completedStatus)

    val mockReturnTaskListCreator = mock[ReturnTaskListCreator]
    val taskListService           = new TaskListService(mockReturnTaskListCreator)
  }
}
