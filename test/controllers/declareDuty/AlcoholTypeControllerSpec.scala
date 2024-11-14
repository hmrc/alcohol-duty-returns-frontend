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

package controllers.declareDuty

import base.SpecBase
import forms.declareDuty.AlcoholTypeFormProvider
import models.{AlcoholRegime, AlcoholRegimes, NormalMode}
import navigation.{FakeReturnsNavigator, ReturnsNavigator}
import org.mockito.ArgumentMatchers.any
import pages.declareDuty.AlcoholTypePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.UserAnswersConnector
import models.AlcoholRegime.{Beer, Cider, Wine}
import uk.gov.hmrc.http.HttpResponse
import views.html.declareDuty.AlcoholTypeView

import scala.concurrent.Future

class AlcoholTypeControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  lazy val alcoholTypeRoute = routes.AlcoholTypeController.onPageLoad(NormalMode).url

  val formProvider   = new AlcoholTypeFormProvider()
  val form           = formProvider()
  val alcoholRegimes = AlcoholRegimes(Set(Beer, Cider, Wine))
  "AlcoholType Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, alcoholTypeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AlcoholTypeView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode, emptyUserAnswers.regimes)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val alcoholRegimesValues: Set[AlcoholRegime] = Set(Beer, Cider, Wine)
      val userAnswers                              = emptyUserAnswers.set(AlcoholTypePage, alcoholRegimesValues).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, alcoholTypeRoute)

        val view = application.injector.instanceOf[AlcoholTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(Set("Beer", "Cider", "Wine")),
          NormalMode,
          userAnswers.regimes
        )(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(new FakeReturnsNavigator(onwardRoute)),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, alcoholTypeRoute)
            .withFormUrlEncodedBody(("value[0]", alcoholRegimes.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, alcoholTypeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AlcoholTypeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, emptyUserAnswers.regimes)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, alcoholTypeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, alcoholTypeRoute)
            .withFormUrlEncodedBody(("value[0]", alcoholRegimes.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
