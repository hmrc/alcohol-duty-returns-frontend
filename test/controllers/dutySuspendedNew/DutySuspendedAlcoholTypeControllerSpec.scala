/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.dutySuspendedNew

import base.SpecBase
import connectors.UserAnswersConnector
import forms.dutySuspendedNew.DutySuspendedAlcoholTypeFormProvider
import models.AlcoholRegime.{Beer, Cider, Wine}
import models.{AlcoholRegime, NormalMode}
import navigation.DutySuspendedNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.dutySuspendedNew._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.dutySuspendedNew.DutySuspendedAlcoholTypeView

import scala.concurrent.Future

class DutySuspendedAlcoholTypeControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  lazy val dutySuspendedAlcoholTypeRoute = routes.DutySuspendedAlcoholTypeController.onPageLoad(NormalMode).url

  val formProvider = new DutySuspendedAlcoholTypeFormProvider()
  val form         = formProvider()

  "DutySuspendedAlcoholType Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedAlcoholTypeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DutySuspendedAlcoholTypeView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode, emptyUserAnswers.regimes)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val alcoholRegimesValues: Set[AlcoholRegime] = Set(Beer, Cider, Wine)
      val userAnswers                              = emptyUserAnswers.set(DutySuspendedAlcoholTypePage, alcoholRegimesValues).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedAlcoholTypeRoute)

        val view = application.injector.instanceOf[DutySuspendedAlcoholTypeView]

        val result = route(application, request).value

        status(result)          mustEqual OK
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

      val mockUserAnswersConnector   = mock[UserAnswersConnector]
      val mockDutySuspendedNavigator = mock[DutySuspendedNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockDutySuspendedNavigator.nextPage(eqTo(DutySuspendedAlcoholTypePage), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[DutySuspendedNavigator].toInstance(mockDutySuspendedNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedAlcoholTypeRoute)
            .withFormUrlEncodedBody(("value[0]", "Beer"), ("value[1]", "Cider"), ("value[2]", "Wine"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockDutySuspendedNavigator, times(1))
          .nextPage(eqTo(DutySuspendedAlcoholTypePage), eqTo(NormalMode), any(), eqTo(Some(true)))
      }
    }

    "must clear data for removed regimes and check if regimes were added when valid data is submitted" in {

      val mockUserAnswersConnector   = mock[UserAnswersConnector]
      val mockDutySuspendedNavigator = mock[DutySuspendedNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockDutySuspendedNavigator.nextPage(eqTo(DutySuspendedAlcoholTypePage), any(), any(), any())
      ) thenReturn onwardRoute

      val expectedCachedUserAnswers = emptyUserAnswers.copy(data =
        Json.obj(
          DeclareDutySuspenseQuestionPage.toString -> true,
          DutySuspendedAlcoholTypePage.toString    -> Json.arr("Spirits"),
          DutySuspendedQuantitiesPage.toString     -> Json.obj(),
          DutySuspendedFinalVolumesPage.toString   -> Json.obj()
        )
      )

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithDutySuspendedData))
          .overrides(
            bind[DutySuspendedNavigator].toInstance(mockDutySuspendedNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedAlcoholTypeRoute)
            .withFormUrlEncodedBody(("value[0]", "Spirits"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(eqTo(expectedCachedUserAnswers))(any())
        verify(mockDutySuspendedNavigator, times(1))
          .nextPage(
            eqTo(DutySuspendedAlcoholTypePage),
            eqTo(NormalMode),
            eqTo(expectedCachedUserAnswers),
            eqTo(Some(true))
          )
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedAlcoholTypeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[DutySuspendedAlcoholTypeView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, emptyUserAnswers.regimes)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedAlcoholTypeRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedAlcoholTypeRoute)
            .withFormUrlEncodedBody(("value[0]", "Beer"), ("value[1]", "Cider"), ("value[2]", "Wine"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
