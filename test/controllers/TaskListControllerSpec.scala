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

package controllers

import base.SpecBase
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.inject.bind
import play.api.test.Helpers._
import services.tasklist.TaskListService
import viewmodels.tasklist.AlcoholDutyTaskList
import views.html.TaskListView

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}

class TaskListControllerSpec extends SpecBase {
  "TaskList Controller" - {

    "must return OK and the correct view for a GET" in new SetUp {
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TaskListService].toInstance(mockTaskListService)
        )
        .build()

      when(mockTaskListService.getTaskList(any, eqTo(validUntil), eqTo(periodKey))(any)).thenReturn(taskList)

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taskList)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no validUntil is found" in new SetUp {
      override def userAnswers: UserAnswers = super.userAnswers.copy(validUntil = None)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no period key is found" in new SetUp {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequestWithoutSession(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data nor period key is found" in new SetUp {
      override def userAnswers: UserAnswers = super.userAnswers.copy(validUntil = None)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequestWithoutSession(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  class SetUp {
    private val instant      = Instant.now.truncatedTo(ChronoUnit.MILLIS)
    private val clock: Clock = Clock.fixed(instant, ZoneId.systemDefault)
    private val A_DAY_IN_SEC = 86400
    val validUntil: Instant  = Instant.now(clock).plusSeconds(A_DAY_IN_SEC)

    def userAnswers: UserAnswers = emptyUserAnswers.copy(validUntil = Some(validUntil))

    val taskList = AlcoholDutyTaskList(Seq.empty, "")

    val mockTaskListService = mock[TaskListService]
  }
}
