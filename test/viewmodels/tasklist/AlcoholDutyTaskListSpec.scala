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
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.{TaskList, TaskListItem}
import viewmodels.tasklist.TaskListStatus.{Completed, Incomplete}

class AlcoholDutyTaskListSpec extends SpecBase {
  "AlcoholDutyTaskList" - {
    "should classify the completed tasks when there aren't any" in new SetUp {
      val taskList = AlcoholDutyTaskList(Seq.empty, "expiry")

      taskList.completedTasks mustBe 0
      taskList.totalTasks mustBe 0
      taskList.status mustBe Completed
    }

    "should classify the completed tasks when not all are complete" in new SetUp {
      val taskList = AlcoholDutyTaskList(
        Seq(
          Section(
            "task1",
            TaskList(
              Seq(
                TaskListItem(status = AlcoholDutyTaskListItemStatus.completed),
                TaskListItem(status = AlcoholDutyTaskListItemStatus.completed)
              )
            ),
            AlcoholDutyTaskListItemStatus.completed
          ),
          Section(
            "task2",
            TaskList(
              Seq(
                TaskListItem(status = AlcoholDutyTaskListItemStatus.completed),
                TaskListItem(status = AlcoholDutyTaskListItemStatus.notStarted)
              )
            ),
            AlcoholDutyTaskListItemStatus.completed
          ),
          Section(
            "task3",
            TaskList(Seq(TaskListItem(status = AlcoholDutyTaskListItemStatus.notStarted))),
            AlcoholDutyTaskListItemStatus.completed
          )
        ),
        "expiry"
      )

      taskList.completedTasks mustBe 1
      taskList.totalTasks mustBe 3
      taskList.status mustBe Incomplete
    }

    "should classify the completed tasks when all are complete" in new SetUp {
      val taskList = AlcoholDutyTaskList(
        Seq(
          Section(
            "task1",
            TaskList(
              Seq(
                TaskListItem(status = AlcoholDutyTaskListItemStatus.completed),
                TaskListItem(status = AlcoholDutyTaskListItemStatus.completed)
              )
            ),
            AlcoholDutyTaskListItemStatus.completed
          ),
          Section(
            "task2",
            TaskList(
              Seq(
                TaskListItem(status = AlcoholDutyTaskListItemStatus.completed),
                TaskListItem(status = AlcoholDutyTaskListItemStatus.completed)
              )
            ),
            AlcoholDutyTaskListItemStatus.completed
          ),
          Section(
            "task3",
            TaskList(Seq(TaskListItem(status = AlcoholDutyTaskListItemStatus.completed))),
            AlcoholDutyTaskListItemStatus.completed
          )
        ),
        "expiry"
      )

      taskList.completedTasks mustBe 3
      taskList.totalTasks mustBe 3
      taskList.status mustBe Completed
    }
  }

  class SetUp {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)
  }
}
