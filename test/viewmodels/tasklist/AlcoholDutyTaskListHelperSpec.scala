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
import config.FrontendAppConfig
import models.ReturnPeriod
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.adjustment.DeclareAdjustmentQuestionPage
import pages.declareDuty.DeclareAlcoholDutyQuestionPage
import pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage
import pages.spiritsQuestions.DeclareQuarterlySpiritsPage
import play.api.Application
import play.api.i18n.Messages
import viewmodels.tasklist.TaskListStatus.Incomplete

import java.time.Instant

class AlcoholDutyTaskListHelperSpec extends SpecBase with ScalaCheckPropertyChecks {
  "AlcoholDutyTaskListHelper" - {
    "must return an incomplete task list" in new SetUp {
      val expectedSections = Seq(
        returnTaskListCreator.returnSection(userAnswers),
        returnTaskListCreator.returnAdjustmentSection(userAnswers),
        returnTaskListCreator.returnDSDSection(userAnswers),
        returnTaskListCreator.returnQSSection(userAnswers),
        returnTaskListCreator.returnCheckAndSubmitSection(0, 4)
      )

      val result =
        taskListViewModel.getTaskList(userAnswers, validUntil, returnPeriod)(getMessages(application))

      result mustBe AlcoholDutyTaskList(
        expectedSections,
        fromDateString,
        toDateString,
        dueDateString,
        validUntilString
      )

      result.status         mustBe Incomplete
      result.totalTasks     mustBe expectedSections.size
      result.completedTasks mustBe 0
    }

    "must return an incomplete task list when all sections are completed except for check and submit" in new SetUp {
      val userAnswersAllOtherSectionsCompleted = userAnswers
        .set(DeclareAlcoholDutyQuestionPage, false)
        .success
        .value
        .set(DeclareAdjustmentQuestionPage, false)
        .success
        .value
        .set(DeclareDutySuspendedDeliveriesQuestionPage, false)
        .success
        .value
        .set(DeclareQuarterlySpiritsPage, false)
        .success
        .value

      val expectedSections =
        Seq(
          returnTaskListCreator.returnSection(userAnswersAllOtherSectionsCompleted),
          returnTaskListCreator.returnAdjustmentSection(userAnswersAllOtherSectionsCompleted),
          returnTaskListCreator.returnDSDSection(userAnswersAllOtherSectionsCompleted),
          returnTaskListCreator.returnQSSection(userAnswersAllOtherSectionsCompleted),
          returnTaskListCreator.returnCheckAndSubmitSection(4, 4)
        )

      val result =
        taskListViewModel.getTaskList(userAnswersAllOtherSectionsCompleted, validUntil, returnPeriod)(
          getMessages(application)
        )

      result mustBe AlcoholDutyTaskList(
        expectedSections,
        fromDateString,
        toDateString,
        dueDateString,
        validUntilString
      )

      result.status         mustBe Incomplete
      result.totalTasks     mustBe expectedSections.size
      result.completedTasks mustBe 4
    }

    "must return a the quarter spirits task only in Mar, Jun, Sept and December" in new SetUp {
      val expectedSectionsWithQS = Seq(
        returnTaskListCreator.returnSection(userAnswers),
        returnTaskListCreator.returnAdjustmentSection(userAnswers),
        returnTaskListCreator.returnDSDSection(userAnswers),
        returnTaskListCreator.returnQSSection(userAnswers),
        returnTaskListCreator.returnCheckAndSubmitSection(0, 4)
      )

      val expectedSectionsWithoutQS = Seq(
        returnTaskListCreator.returnSection(userAnswers),
        returnTaskListCreator.returnAdjustmentSection(userAnswers),
        returnTaskListCreator.returnDSDSection(userAnswers),
        returnTaskListCreator.returnCheckAndSubmitSection(0, 3)
      )

      forAll(periodKeyGen) { case periodKey =>
        val returnPeriod = ReturnPeriod.fromPeriodKey(periodKey).get

        val result =
          taskListViewModel.getTaskList(userAnswers, validUntil, returnPeriod)(getMessages(application))

        val periodQuarterChars = Set('C', 'F', 'I', 'L')
        val lastChar           = periodKey.last

        if (periodQuarterChars.contains(lastChar)) {
          result.sections mustBe expectedSectionsWithQS
        } else {
          result.sections mustBe expectedSectionsWithoutQS
        }
      }
    }
  }

  class SetUp {
    val additionalConfig             = Map("features.duty-suspended-new-journey" -> false)
    val application: Application     = applicationBuilder().configure(additionalConfig).build()
    val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
    implicit val messages: Messages  = getMessages(application)
    private val periodKey            = periodKeyDec23
    val userAnswers                  = emptyUserAnswers.copy(returnId = emptyUserAnswers.returnId.copy(periodKey = periodKey))
    val returnPeriod                 = ReturnPeriod.fromPeriodKeyOrThrow(periodKey)
    private val dateTimeHelper       = createDateTimeHelper()
    val fromDate                     = returnPeriod.periodFromDate()
    val toDate                       = returnPeriod.periodToDate()
    val dueDate                      = returnPeriod.periodDueDate()
    val validUntil                   = Instant.now(clock)
    val fromDateString               = dateTimeHelper.formatDateMonthYear(fromDate)
    val toDateString                 = dateTimeHelper.formatDateMonthYear(toDate)
    val dueDateString                = dateTimeHelper.formatDateMonthYear(dueDate)
    val validUntilString             = dateTimeHelper.formatDateMonthYear(dateTimeHelper.instantToLocalDate(validUntil))
    val returnTaskListCreator        = new ReturnTaskListCreator(appConfig)
    val taskListViewModel            = new TaskListViewModel(createDateTimeHelper(), returnTaskListCreator)
  }
}
