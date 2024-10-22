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
import forms.returns.DoYouHaveMultipleSPRDutyRatesFormProvider
import models.{CheckMode, NormalMode}
import navigation.{FakeReturnsNavigator, ReturnsNavigator}
import org.mockito.ArgumentMatchers.any
import pages.declareDuty.DoYouHaveMultipleSPRDutyRatesPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.CacheConnector
import uk.gov.hmrc.http.HttpResponse
import views.html.declareDuty.DoYouHaveMultipleSPRDutyRatesView

import scala.concurrent.Future

class DoYouHaveMultipleSPRDutyRatesControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new DoYouHaveMultipleSPRDutyRatesFormProvider()
  val form         = formProvider()
  val regime       = regimeGen.sample.value

  lazy val doYouHaveMultipleSPRDutyRatesRoute          =
    controllers.declareDuty.routes.DoYouHaveMultipleSPRDutyRatesController.onPageLoad(NormalMode, regime).url
  lazy val doYouHaveMultipleSPRDutyRatesRouteCheckMode =
    controllers.declareDuty.routes.DoYouHaveMultipleSPRDutyRatesController.onPageLoad(CheckMode, regime).url

  "DoYouHaveMultipleSPRDutyRates Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, doYouHaveMultipleSPRDutyRatesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DoYouHaveMultipleSPRDutyRatesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, regime, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setByKey(DoYouHaveMultipleSPRDutyRatesPage, regime, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, doYouHaveMultipleSPRDutyRatesRoute)

        val view = application.injector.instanceOf[DoYouHaveMultipleSPRDutyRatesView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), regime, NormalMode)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(new FakeReturnsNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, doYouHaveMultipleSPRDutyRatesRoute)
            .withFormUrlEncodedBody(("doYouHaveMultipleSPRDutyRates-yesNoValue", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when valid data is submitted in Check Mode and the value has not changed" in {
      val userAnswers = emptyUserAnswers.setByKey(DoYouHaveMultipleSPRDutyRatesPage, regime, true).success.value

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(new FakeReturnsNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, doYouHaveMultipleSPRDutyRatesRouteCheckMode)
            .withFormUrlEncodedBody(("doYouHaveMultipleSPRDutyRates-yesNoValue", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when valid data is submitted in Check Mode and the value has changed" in {
      val userAnswers = emptyUserAnswers.setByKey(DoYouHaveMultipleSPRDutyRatesPage, regime, true).success.value

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(new FakeReturnsNavigator(onwardRoute, hasAnswerChangeValue = true)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, doYouHaveMultipleSPRDutyRatesRouteCheckMode)
            .withFormUrlEncodedBody(("doYouHaveMultipleSPRDutyRates-yesNoValue", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, doYouHaveMultipleSPRDutyRatesRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DoYouHaveMultipleSPRDutyRatesView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, regime, NormalMode)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, doYouHaveMultipleSPRDutyRatesRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, doYouHaveMultipleSPRDutyRatesRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
