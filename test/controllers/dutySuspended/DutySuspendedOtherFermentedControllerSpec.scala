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
import forms.dutySuspended.DutySuspendedOtherFermentedFormProvider
import models.NormalMode
import models.dutySuspended.DutySuspendedOtherFermented
import navigation.{DeclareDutySuspendedDeliveriesNavigator, FakeDeclareDutySuspendedDeliveriesNavigator}
import org.mockito.ArgumentMatchers.any
import pages.dutySuspended.DutySuspendedOtherFermentedPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.CacheConnector
import uk.gov.hmrc.http.HttpResponse
import views.html.dutySuspended.DutySuspendedOtherFermentedView

import scala.concurrent.Future

class DutySuspendedOtherFermentedControllerSpec extends SpecBase {
  def onwardRoute = Call("GET", "/foo")

  val formProvider                     = new DutySuspendedOtherFermentedFormProvider()
  val form                             = formProvider()
  val validTotalOtherFermented         = 45.67
  val validPureAlcoholInOtherFermented = 23.45

  lazy val dutySuspendedOtherFermentedRoute = routes.DutySuspendedOtherFermentedController.onPageLoad(NormalMode).url

  val userAnswers = userAnswersWithOtherFermentedProduct.copy(data =
    Json.obj(
      DutySuspendedOtherFermentedPage.toString -> Json.obj(
        "totalOtherFermented"         -> validTotalOtherFermented,
        "pureAlcoholInOtherFermented" -> validPureAlcoholInOtherFermented
      )
    )
  )

  "DutySuspendedOtherFermented Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithOtherFermentedProduct)).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedOtherFermentedRoute)

        val view = application.injector.instanceOf[DutySuspendedOtherFermentedView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedOtherFermentedRoute)

        val view = application.injector.instanceOf[DutySuspendedOtherFermentedView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(DutySuspendedOtherFermented(validTotalOtherFermented, validPureAlcoholInOtherFermented)),
          NormalMode
        )(request, getMessages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithOtherFermentedProduct))
          .overrides(
            bind[DeclareDutySuspendedDeliveriesNavigator]
              .toInstance(new FakeDeclareDutySuspendedDeliveriesNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedOtherFermentedRoute)
            .withFormUrlEncodedBody(
              ("totalOtherFermented", validTotalOtherFermented.toString),
              ("pureAlcoholInOtherFermented", validPureAlcoholInOtherFermented.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithOtherFermentedProduct)).build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedOtherFermentedRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[DutySuspendedOtherFermentedView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedOtherFermentedRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery if regime is missing for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutOtherFermentedProduct)).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedOtherFermentedRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedOtherFermentedRoute)
            .withFormUrlEncodedBody(("totalOtherFermented", "value 1"), ("pureAlcoholInOtherFermented", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery if regime is missing for a POST" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutOtherFermentedProduct)).build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedOtherFermentedRoute)
            .withFormUrlEncodedBody(
              ("totalOtherFermented", validTotalOtherFermented.toString),
              ("pureAlcoholInOtherFermented", validPureAlcoholInOtherFermented.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
