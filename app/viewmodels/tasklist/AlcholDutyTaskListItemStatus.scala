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

import config.Constants.Css
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.TaskListItemStatus

object AlcholDutyTaskListItemStatus {
  def completed(implicit messages: Messages): TaskListItemStatus   = TaskListItemStatus(
    content = Text(messages("taskList.section.status.completed"))
  )
  def notStarted(implicit messages: Messages): TaskListItemStatus  = TaskListItemStatus(
    tag = Some(Tag(content = Text(messages("taskList.section.status.notStarted")), classes = Css.blueTagCssClass))
  )
  def inProgress(implicit messages: Messages): TaskListItemStatus  = TaskListItemStatus(
    tag = Some(Tag(content = Text(messages("taskList.section.status.inProgress")), classes = Css.blueTagCssClass))
  )
  def cannotStart(implicit messages: Messages): TaskListItemStatus = TaskListItemStatus(
    tag = Some(Tag(content = Text(messages("taskList.section.status.cannotStart")), classes = Css.greyTagCssClass))
  )
}
