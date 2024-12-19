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

import config.FrontendAppConfig
import models.{ReturnPeriod, UserAnswers}
import play.api.i18n.Messages
import viewmodels.DateTimeHelper

import java.time.Instant
import javax.inject.Inject

class TaskListViewModel @Inject() (
  dateTimeHelper: DateTimeHelper,
  returnTaskListCreator: ReturnTaskListCreator,
  appConfig: FrontendAppConfig
) {
  def getTaskList(userAnswers: UserAnswers, validUntil: Instant, returnPeriod: ReturnPeriod)(implicit
    messages: Messages
  ): AlcoholDutyTaskList =
    AlcoholDutyTaskList(
      sections(userAnswers, returnPeriod),
      dateTimeHelper.formatDateMonthYear(returnPeriod.periodFromDate()),
      dateTimeHelper.formatDateMonthYear(returnPeriod.periodToDate()),
      dateTimeHelper.formatDateMonthYear(returnPeriod.periodDueDate()),
      dateTimeHelper.formatDateMonthYear(dateTimeHelper.instantToLocalDate(validUntil))
    )

  def hasSpiritsTask(userAnswers: UserAnswers, returnPeriod: ReturnPeriod): Boolean =
    appConfig.spiritsAndIngredientsEnabled && userAnswers.regimes.hasSpirits() && returnPeriod.hasQuarterlySpirits

  private def getDeclarationSections(userAnswers: UserAnswers, returnPeriod: ReturnPeriod)(implicit
    message: Messages
  ): Seq[Section] =
    Seq(
      Some(returnTaskListCreator.returnSection(userAnswers)),
      Some(returnTaskListCreator.returnAdjustmentSection(userAnswers)),
      Some(returnTaskListCreator.returnDSDSection(userAnswers)),
      Some(returnTaskListCreator.returnQSSection(userAnswers)).filter(_ => hasSpiritsTask(userAnswers, returnPeriod))
    ).flatten

  private def sections(userAnswers: UserAnswers, returnPeriod: ReturnPeriod)(implicit
    messages: Messages
  ): Seq[Section] = {
    val declarationSections = getDeclarationSections(userAnswers, returnPeriod)

    val completedTasks = declarationSections.count(_.completedTask)
    val totalTasks     = declarationSections.size

    val checkAndSubmitSection = returnTaskListCreator.returnCheckAndSubmitSection(completedTasks, totalTasks)

    declarationSections :+ checkAndSubmitSection
  }

  def checkAllDeclarationSectionsCompleted(userAnswers: UserAnswers, returnPeriod: ReturnPeriod)(implicit
    messages: Messages
  ): Boolean = {
    val declarationSections = getDeclarationSections(userAnswers, returnPeriod)

    val completedTasks = declarationSections.count(_.completedTask)
    val totalTasks     = declarationSections.size

    completedTasks == totalTasks
  }
}
