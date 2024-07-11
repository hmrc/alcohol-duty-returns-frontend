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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.dutySuspended.DeclareDutySuspendedDeliveriesQuestionPage
import pages.returns.DeclareAlcoholDutyQuestionPage
import pages.spiritsQuestions.DeclareQuarterlySpiritsPage
import play.api.Application
import play.api.i18n.Messages
import viewmodels.govuk.all.FluentInstant

import java.time.{Clock, Instant, ZoneId}
import java.time.temporal.ChronoUnit

class AlcoholDutyTaskListHelperSpec extends SpecBase with ScalaCheckPropertyChecks {
  val application: Application    = applicationBuilder().build()
  private val instant             = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val clock: Clock        = Clock.fixed(instant, ZoneId.systemDefault)
  private val validUntil          = Instant.now(clock)
  implicit val messages: Messages = messages(application)
  val returnTaskListCreator       = new ReturnTaskListCreator()
  val taskListViewModel           = new TaskListViewModel(returnTaskListCreator)

  "AlcoholDutyTaskListHelper" - {
    "must return an incomplete task list" in {

      val expectedSections = Seq(
        returnTaskListCreator.returnSection(emptyUserAnswers),
        returnTaskListCreator.returnDSDSection(emptyUserAnswers),
        returnTaskListCreator.returnQSSection(emptyUserAnswers)
      )

      val result           =
        taskListViewModel.getTaskList(emptyUserAnswers, validUntil, periodKeyMar)(
          messages(application)
        )
      val validUntilString = validUntil.toLocalDateString()

      result mustBe AlcoholDutyTaskList(
        expectedSections,
        validUntilString
      )

      result.status mustBe Incomplete
      result.totalTasks mustBe expectedSections.size
      result.completedTasks mustBe 0
    }

    "must return a completed task list" in {

      val userAnswers = emptyUserAnswers
        .set(DeclareAlcoholDutyQuestionPage, false)
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
          returnTaskListCreator.returnDSDSection(userAnswers),
          returnTaskListCreator.returnQSSection(userAnswers)
        )

      val result           =
        taskListViewModel.getTaskList(userAnswers, validUntil, periodKeyMar)(
          messages(application)
        )
      val validUntilString = validUntil.toLocalDateString()

      result mustBe AlcoholDutyTaskList(
        expectedSections,
        validUntilString
      )

      result.status mustBe Completed
      result.totalTasks mustBe expectedSections.size
      result.completedTasks mustBe 3
    }

    "must return a the quarter spirits task only in Mar, Jun, Sep and Dec" in {
      val expectedSectionsWithQS = Seq(
        returnTaskListCreator.returnSection(emptyUserAnswers),
        returnTaskListCreator.returnDSDSection(emptyUserAnswers),
        returnTaskListCreator.returnQSSection(emptyUserAnswers)
      )

      val expectedSectionsWithoutQS = Seq(
        returnTaskListCreator.returnSection(emptyUserAnswers),
        returnTaskListCreator.returnDSDSection(emptyUserAnswers)
      )

      forAll(periodKeyGen) { case periodKey =>
        val result =
          taskListViewModel.getTaskList(emptyUserAnswers, validUntil, periodKey)(
            messages(application)
          )

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
