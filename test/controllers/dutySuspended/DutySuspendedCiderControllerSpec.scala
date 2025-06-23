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

package controllers.dutySuspended

import base.SpecBase
import connectors.UserAnswersConnector
import forms.dutySuspended.DutySuspendedCiderFormProvider
import models.NormalMode
import models.dutySuspended.DutySuspendedCider
import navigation.DeclareDutySuspendedDeliveriesNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.dutySuspended.DutySuspendedCiderPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.dutySuspended.DutySuspendedCiderView

import scala.concurrent.Future

class DutySuspendedCiderControllerSpec extends SpecBase {
  def onwardRoute = Call("GET", "/foo")

  val formProvider            = new DutySuspendedCiderFormProvider()
  val form                    = formProvider()
  val validTotalCider         = 45.67
  val validPureAlcoholInCider = 23.45

  lazy val dutySuspendedCiderRoute = routes.DutySuspendedCiderController.onPageLoad(NormalMode).url

  val userAnswers = userAnswersWithCider.copy(data =
    Json.obj(
      DutySuspendedCiderPage.toString -> Json.obj(
        "totalCider"         -> validTotalCider,
        "pureAlcoholInCider" -> validPureAlcoholInCider
      )
    )
  )

  "DutySuspendedCider Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithCider)).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedCiderRoute)

        val view = application.injector.instanceOf[DutySuspendedCiderView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedCiderRoute)

        val view = application.injector.instanceOf[DutySuspendedCiderView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(DutySuspendedCider(validTotalCider, validPureAlcoholInCider)),
          NormalMode
        )(request, getMessages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockUserAnswersConnector             = mock[UserAnswersConnector]
      val mockDutySuspendedDeliveriesNavigator = mock[DeclareDutySuspendedDeliveriesNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockDutySuspendedDeliveriesNavigator.nextPage(eqTo(DutySuspendedCiderPage), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCider))
          .overrides(
            bind[DeclareDutySuspendedDeliveriesNavigator].toInstance(mockDutySuspendedDeliveriesNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedCiderRoute)
            .withFormUrlEncodedBody(
              ("totalCider", validTotalCider.toString),
              ("pureAlcoholInCider", validPureAlcoholInCider.toString)
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockDutySuspendedDeliveriesNavigator, times(1))
          .nextPage(eqTo(DutySuspendedCiderPage), eqTo(NormalMode), any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithCider)).build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedCiderRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[DutySuspendedCiderView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedCiderRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery if regime is missing for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutCider)).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedCiderRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedCiderRoute)
            .withFormUrlEncodedBody(
              ("totalCider", validTotalCider.toString),
              ("pureAlcoholInCider", validPureAlcoholInCider.toString)
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery if regime is missing for a POST" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutCider)).build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedCiderRoute)
            .withFormUrlEncodedBody(
              ("totalCider", validTotalCider.toString),
              ("pureAlcoholInCider", validPureAlcoholInCider.toString)
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
