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
import connectors.CacheConnector
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import viewmodels.tasklist.AlcoholDutyTaskListHelper
import views.html.TaskListView

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.Future

class TaskListControllerSpec extends SpecBase {
  private val instant      = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val clock: Clock = Clock.fixed(instant, ZoneId.systemDefault)
  private val A_DAY_IN_SEC = 86400
  private val validUntil   = Instant.now(clock).plusSeconds(A_DAY_IN_SEC)
  private val userAnswers  = UserAnswers(
    "userId",
    lastUpdated = Instant.now(clock),
    validUntil = Some(validUntil)
  )

  "TaskList Controller" - {

    val mockCacheConnector = mock[CacheConnector]
    when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
    when(mockCacheConnector.get(any())(any())) thenReturn Future.successful(Some(userAnswers))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        val view             = application.injector.instanceOf[TaskListView]
        val expectedTaskList =
          AlcoholDutyTaskListHelper.getTaskList(emptyUserAnswers, validUntil)(messages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(expectedTaskList)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET if the userAnswer does not exist yet" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        val view             = application.injector.instanceOf[TaskListView]
        val expectedTaskList =
          AlcoholDutyTaskListHelper.getTaskList(emptyUserAnswers, validUntil)(messages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(expectedTaskList)(request, messages(application)).toString
      }
    }
  }
}
