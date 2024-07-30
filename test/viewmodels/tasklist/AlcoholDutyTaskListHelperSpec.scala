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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.adjustment.DeclareAdjustmentQuestionPage
import pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage
import pages.returns.DeclareAlcoholDutyQuestionPage
import pages.spiritsQuestions.DeclareQuarterlySpiritsPage
import play.api.Application
import play.api.i18n.Messages
import viewmodels.govuk.all.FluentInstant

import java.time.Instant

class AlcoholDutyTaskListHelperSpec extends SpecBase with ScalaCheckPropertyChecks {
  val application: Application    = applicationBuilder().build()
  private val validUntil          = Instant.now(clock)
  implicit val messages: Messages = getMessages(application)
  val returnTaskListCreator       = new ReturnTaskListCreator()
  val taskListViewModel           = new TaskListViewModel(returnTaskListCreator)

  "AlcoholDutyTaskListHelper" - {
    "must return an incomplete task list" in {

      val expectedSections = Seq(
        returnTaskListCreator.returnSection(emptyUserAnswers),
        returnTaskListCreator.returnAdjustmentSection(emptyUserAnswers),
        returnTaskListCreator.returnDSDSection(emptyUserAnswers),
        returnTaskListCreator.returnQSSection(emptyUserAnswers),
        returnTaskListCreator.returnCheckAndSubmitSection(0, 4)
      )

      val result           =
        taskListViewModel.getTaskList(emptyUserAnswers, validUntil, periodKeyMar)(getMessages(application))
      val validUntilString = validUntil.toLocalDateString()

      result mustBe AlcoholDutyTaskList(
        expectedSections,
        validUntilString
      )

      result.status mustBe Incomplete
      result.totalTasks mustBe expectedSections.size
      result.completedTasks mustBe 0
    }

    "must return an incomplete task list when all sections are completed except for check and submit" in {

      val userAnswers = emptyUserAnswers
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
          returnTaskListCreator.returnSection(userAnswers),
          returnTaskListCreator.returnAdjustmentSection(userAnswers),
          returnTaskListCreator.returnDSDSection(userAnswers),
          returnTaskListCreator.returnQSSection(userAnswers),
          returnTaskListCreator.returnCheckAndSubmitSection(4, 4)
        )

      val result           =
        taskListViewModel.getTaskList(userAnswers, validUntil, periodKeyMar)(getMessages(application))
      val validUntilString = validUntil.toLocalDateString()

      result mustBe AlcoholDutyTaskList(
        expectedSections,
        validUntilString
      )

      result.status mustBe Incomplete
      result.totalTasks mustBe expectedSections.size
      result.completedTasks mustBe 4
    }

    "must return a the quarter spirits task only in Mar, Jun, Sep and Dec" in {
      val expectedSectionsWithQS = Seq(
        returnTaskListCreator.returnSection(emptyUserAnswers),
        returnTaskListCreator.returnAdjustmentSection(emptyUserAnswers),
        returnTaskListCreator.returnDSDSection(emptyUserAnswers),
        returnTaskListCreator.returnQSSection(emptyUserAnswers),
        returnTaskListCreator.returnCheckAndSubmitSection(0, 4)
      )

      val expectedSectionsWithoutQS = Seq(
        returnTaskListCreator.returnSection(emptyUserAnswers),
        returnTaskListCreator.returnAdjustmentSection(emptyUserAnswers),
        returnTaskListCreator.returnDSDSection(emptyUserAnswers),
        returnTaskListCreator.returnCheckAndSubmitSection(0, 3)
      )

      forAll(periodKeyGen) { case periodKey =>
        val result =
          taskListViewModel.getTaskList(emptyUserAnswers, validUntil, periodKey)(getMessages(application))

        val periodQuarters = "CFIL"
        val lastChar       = periodKey.last

        if (periodQuarters.contains(lastChar)) {
          result.sections mustBe expectedSectionsWithQS
        } else {
          result.sections mustBe expectedSectionsWithoutQS
        }
      }
    }
  }
}
