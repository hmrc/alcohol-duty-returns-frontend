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

package controllers.returns

import base.SpecBase
import connectors.AlcoholDutyReturnsConnector
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import play.api.test.Helpers._
import viewmodels.returns.ViewPastReturnsHelper
import views.html.returns.ViewPastReturnsView

import scala.concurrent.Future

class ViewPastReturnsControllerSpec extends SpecBase {
  "ViewPastReturns Controller" - {
    "must return OK and the correct view for a GET" in {
      val viewModelHelper                 = new ViewPastReturnsHelper()
      val mockAlcoholDutyReturnsConnector = mock[AlcoholDutyReturnsConnector]
      when(mockAlcoholDutyReturnsConnector.obligationDetails(any())(any())) thenReturn Future.successful(
        Seq(obligationDataSingleOpen, obligationDataSingleFulfilled)
      )
      val application                     = applicationBuilder(userAnswers = None)
        .overrides(bind[AlcoholDutyReturnsConnector].toInstance(mockAlcoholDutyReturnsConnector))
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.returns.routes.ViewPastReturnsController.onPageLoad.url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[ViewPastReturnsView]

        val outstandingReturnsTable =
          viewModelHelper.getReturnsTable(Seq(obligationDataSingleOpen))(getMessages(application))
        val completedReturnsTable   =
          viewModelHelper.getReturnsTable(Seq(obligationDataSingleFulfilled))(getMessages(application))
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(outstandingReturnsTable, completedReturnsTable)(
          request,
          getMessages(application)
        ).toString
      }

    }
    "must redirect to Journey Recovery for a GET on Exception" in {
      val mockAlcoholDutyReturnsConnector = mock[AlcoholDutyReturnsConnector]
      val application                     = applicationBuilder(userAnswers = None).build()
      running(application) {
        when(mockAlcoholDutyReturnsConnector.obligationDetails(any())(any())) thenReturn Future.failed(
          new Exception("test Exception")
        )
        val request = FakeRequest(GET, controllers.returns.routes.ViewPastReturnsController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
