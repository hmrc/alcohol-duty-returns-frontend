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

package services.tasklist

import models.UserAnswers
import play.api.i18n.Messages
import viewmodels.govuk.all.FluentInstant
import viewmodels.tasklist.{AlcoholDutyTaskList, ReturnTaskListCreator}

import java.time.Instant
import javax.inject.Inject

class TaskListService @Inject() (returnTaskListCreator: ReturnTaskListCreator) {
  def getTaskList(userAnswers: UserAnswers, validUntil: Instant, periodKey: String)(implicit
    messages: Messages
  ): AlcoholDutyTaskList =
    AlcoholDutyTaskList(
      Seq(
        Some(returnTaskListCreator.returnSection(userAnswers)),
        Some(returnTaskListCreator.returnDSDSection(userAnswers)),
        if (shouldIncludeQSSection(periodKey)) Some(returnTaskListCreator.returnQSSection(userAnswers)) else None
      ).flatten,
      validUntil.toLocalDateString()
    )

  private def shouldIncludeQSSection(periodKey: String): Boolean = periodKey.last match {
    case 'C' | 'F' | 'I' | 'L' => true
    case _                     => false
  }
}
