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
import pages.productEntry.DeclareAlcoholDutyQuestionPage
import play.api.Application
import play.api.i18n.Messages
import viewmodels.govuk.all.FluentInstant

import java.time.{Clock, Instant, ZoneId}
import java.time.temporal.ChronoUnit

class AlcoholDutyTaskListHelperSpec extends SpecBase {

  val application: Application    = applicationBuilder().build()
  private val instant             = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val clock: Clock        = Clock.fixed(instant, ZoneId.systemDefault)
  private val validUntil          = Instant.now(clock)
  implicit val messages: Messages = messages(application)

  "AlcoholDutyTaskListHelper" - {
    "must return an incomplete task list" in {

      val expectedSections = Seq(ReturnTaskListHelper.returnSection(emptyUserAnswers))

      val result           = AlcoholDutyTaskListHelper.getTaskList(emptyUserAnswers, validUntil)(messages(application))
      val validUntilString = validUntil.toLocalDateString()

      result mustBe AlcoholDutyTaskList(
        expectedSections,
        validUntilString
      )

      result.status mustBe "incomplete"
      result.sections mustBe expectedSections
      result.totalTask mustBe expectedSections.size
      result.completedTask mustBe 0
    }

    "must return a completed task list" in {

      val userAnswers = emptyUserAnswers
        .set(DeclareAlcoholDutyQuestionPage, false)
        .success
        .value

      val expectedSections = Seq(ReturnTaskListHelper.returnSection(userAnswers))

      val result           = AlcoholDutyTaskListHelper.getTaskList(userAnswers, validUntil)(messages(application))
      val validUntilString = validUntil.toLocalDateString()

      result mustBe AlcoholDutyTaskList(
        expectedSections,
        validUntilString
      )

      result.status mustBe "completed"
      result.sections mustBe expectedSections
      result.totalTask mustBe expectedSections.size
      result.completedTask mustBe 1
    }
  }

}