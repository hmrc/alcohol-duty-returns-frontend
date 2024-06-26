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
import play.api.test.Helpers._
import viewmodels.tasklist.AlcoholDutyTaskListHelper
import views.html.TaskListView

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}

class TaskListControllerSpec extends SpecBase {
  private val instant      = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val clock: Clock = Clock.fixed(instant, ZoneId.systemDefault)
  private val A_DAY_IN_SEC = 86400
  private val validUntil   = Instant.now(clock).plusSeconds(A_DAY_IN_SEC)
  private val userAnswers  = emptyUserAnswers.copy(validUntil = Some(validUntil))

  "TaskList Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        val view             = application.injector.instanceOf[TaskListView]
        val expectedTaskList =
          AlcoholDutyTaskListHelper.getTaskList(emptyUserAnswers, validUntil, periodKey)(
            messages(application)
          )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(expectedTaskList)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers.copy(validUntil = None))).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no period key is found" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequestWithoutSession(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data or period key is found" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers.copy(validUntil = None))).build()

      running(application) {
        val request = FakeRequestWithoutSession(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
