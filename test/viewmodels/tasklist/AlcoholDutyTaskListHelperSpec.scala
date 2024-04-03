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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AlcoholDutyTaskListHelperSpec extends SpecBase {

  val dateFormatter               = DateTimeFormatter.ofPattern("dd LLLL yyyy")
  val application: Application    = applicationBuilder().build()
  val sessionExpiryDays           = 90
  implicit val messages: Messages = messages(application)

  "AlcoholDutyTaskListHelper" - {
    "must return an incomplete task list" in {

      val expectedSections = Seq(ReturnTaskListHelper.returnSection(emptyUserAnswers))

      val result = AlcoholDutyTaskListHelper.getTaskList(emptyUserAnswers, sessionExpiryDays)(messages(application))

      result mustBe AlcoholDutyTaskList(
        expectedSections,
        sessionExpiryDays
      )

      result.status mustBe "incomplete"
      result.sections mustBe expectedSections
      result.totalTask mustBe expectedSections.size
      result.completedTask mustBe 0
      result.sessionExpiryDate mustBe dateFormatter.format(LocalDate.now().plusDays(sessionExpiryDays))
    }

    "must return a completed task list" in {

      val userAnswers = emptyUserAnswers
        .set(DeclareAlcoholDutyQuestionPage, false)
        .success
        .value

      val expectedSections = Seq(ReturnTaskListHelper.returnSection(userAnswers))

      val result = AlcoholDutyTaskListHelper.getTaskList(userAnswers, sessionExpiryDays)(messages(application))

      result mustBe AlcoholDutyTaskList(
        expectedSections,
        sessionExpiryDays
      )

      result.status mustBe "completed"
      result.sections mustBe expectedSections
      result.totalTask mustBe expectedSections.size
      result.completedTask mustBe 1
      result.sessionExpiryDate mustBe dateFormatter.format(LocalDate.now().plusDays(sessionExpiryDays))
    }
  }

}
