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
import forms.dutySuspended.DutySuspendedSpiritsFormProvider
import models.NormalMode
import models.dutySuspended.DutySuspendedSpirits
import navigation.DeclareDutySuspendedDeliveriesNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.dutySuspended.DutySuspendedSpiritsPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.dutySuspended.DutySuspendedSpiritsView

import scala.concurrent.Future

class DutySuspendedSpiritsControllerSpec extends SpecBase {
  def onwardRoute = Call("GET", "/foo")

  val formProvider              = new DutySuspendedSpiritsFormProvider()
  val form                      = formProvider()
  val validTotalSpirits         = 45.67
  val validPureAlcoholInSpirits = 23.45

  lazy val dutySuspendedSpiritsRoute = routes.DutySuspendedSpiritsController.onPageLoad(NormalMode).url

  override def configOverrides: Map[String, Any] = Map(
    "features.duty-suspended-new-journey" -> false
  )

  val userAnswers = userAnswersWithSpirits.copy(data =
    Json.obj(
      DutySuspendedSpiritsPage.toString -> Json.obj(
        "totalSpirits"         -> validTotalSpirits,
        "pureAlcoholInSpirits" -> validPureAlcoholInSpirits
      )
    )
  )

  "DutySuspendedSpirits Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithSpirits)).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedSpiritsRoute)

        val view = application.injector.instanceOf[DutySuspendedSpiritsView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedSpiritsRoute)

        val view = application.injector.instanceOf[DutySuspendedSpiritsView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(DutySuspendedSpirits(validTotalSpirits, validPureAlcoholInSpirits)),
          NormalMode
        )(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockUserAnswersConnector             = mock[UserAnswersConnector]
      val mockDutySuspendedDeliveriesNavigator = mock[DeclareDutySuspendedDeliveriesNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockDutySuspendedDeliveriesNavigator.nextPage(eqTo(DutySuspendedSpiritsPage), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithSpirits))
          .overrides(
            bind[DeclareDutySuspendedDeliveriesNavigator].toInstance(mockDutySuspendedDeliveriesNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedSpiritsRoute)
            .withFormUrlEncodedBody(
              ("totalSpirits", validTotalSpirits.toString),
              ("pureAlcoholInSpirits", validPureAlcoholInSpirits.toString)
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockDutySuspendedDeliveriesNavigator, times(1))
          .nextPage(eqTo(DutySuspendedSpiritsPage), eqTo(NormalMode), any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithSpirits)).build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedSpiritsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[DutySuspendedSpiritsView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedSpiritsRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery if regime is missing for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutSpirits)).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedSpiritsRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedSpiritsRoute)
            .withFormUrlEncodedBody(("totalSpirits", "value 1"), ("pureAlcoholInSpirits", "value 2"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery if regime is missing for a POST" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutSpirits)).build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedSpiritsRoute)
            .withFormUrlEncodedBody(
              ("totalSpirits", validTotalSpirits.toString),
              ("pureAlcoholInSpirits", validPureAlcoholInSpirits.toString)
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
