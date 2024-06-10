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
import forms.dutySuspended.DutySuspendedWineFormProvider
import models.NormalMode
import models.dutySuspended.DutySuspendedWine
import navigation.{DeclareDutySuspendedDeliveriesNavigator, FakeDeclareDutySuspendedDeliveriesNavigator}
import org.mockito.ArgumentMatchers.any
import pages.dutySuspended.DutySuspendedWinePage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.CacheConnector
import uk.gov.hmrc.http.HttpResponse
import views.html.dutySuspended.DutySuspendedWineView

import scala.concurrent.Future

class DutySuspendedWineControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new DutySuspendedWineFormProvider()
  val form         = formProvider()

  lazy val dutySuspendedWineRoute = routes.DutySuspendedWineController.onPageLoad(NormalMode).url
  val validTotalWine              = 23.45
  val validPureAlcoholInWine      = 16.46

  val userAnswers = emptyUserAnswers.copy(data =
    Json.obj(
      DutySuspendedWinePage.toString -> Json.obj(
        "totalWine"         -> validTotalWine,
        "pureAlcoholInWine" -> validPureAlcoholInWine
      )
    )
  )

  "DutySuspendedWine Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedWineRoute)

        val view = application.injector.instanceOf[DutySuspendedWineView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedWineRoute)

        val view = application.injector.instanceOf[DutySuspendedWineView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(DutySuspendedWine(validTotalWine, validPureAlcoholInWine)),
          NormalMode
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[DeclareDutySuspendedDeliveriesNavigator]
              .toInstance(new FakeDeclareDutySuspendedDeliveriesNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedWineRoute)
            .withFormUrlEncodedBody(
              ("totalWine", validTotalWine.toString),
              ("pureAlcoholInWine", validPureAlcoholInWine.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedWineRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[DutySuspendedWineView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedWineRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedWineRoute)
            .withFormUrlEncodedBody(
              ("totalWine", validTotalWine.toString),
              ("pureAlcoholInWine", validPureAlcoholInWine.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
