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
import config.FrontendAppConfig
import connectors.CacheConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import viewmodels.tasklist.AlcoholDutyTaskListHelper
import views.html.TaskListView

import scala.concurrent.Future

class TaskListControllerSpec extends SpecBase {

  "TaskList Controller" - {

    val mockCacheConnector = mock[CacheConnector]
    when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

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
        val appConfig        = application.injector.instanceOf[FrontendAppConfig]
        val expectedTaskList =
          AlcoholDutyTaskListHelper.getTaskList(emptyUserAnswers, appConfig.cacheTtlInDays)(messages(application))

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
        val appConfig        = application.injector.instanceOf[FrontendAppConfig]
        val expectedTaskList =
          AlcoholDutyTaskListHelper.getTaskList(emptyUserAnswers, appConfig.cacheTtlInDays)(messages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(expectedTaskList)(request, messages(application)).toString
      }
    }
  }
}
