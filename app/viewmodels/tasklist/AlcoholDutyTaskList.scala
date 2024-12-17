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

import TaskListStatus.{Completed, Incomplete}

case class AlcoholDutyTaskList(
  sections: Seq[Section],
  periodStartDate: String,
  periodEndDate: String,
  dueDate: String,
  sessionExpiryDate: String
) {

  def completedTasks: Int = sections.count(_.completedTask)
  def totalTasks: Int     = sections.size

  def status: TaskListStatus = if (completedTasks == totalTasks) {
    Completed
  } else {
    Incomplete
  }
}
