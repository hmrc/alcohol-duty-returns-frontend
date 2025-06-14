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

package controllers.dutySuspended

import base.SpecBase
import connectors.{AlcoholDutyCalculatorConnector, UserAnswersConnector}
import forms.dutySuspended.DutySuspendedQuantitiesFormProvider
import models.NormalMode
import navigation.DutySuspendedNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.dutySuspended.{DutySuspendedFinalVolumesPage, DutySuspendedQuantitiesPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.dutySuspended.DutySuspendedQuantitiesView

import scala.concurrent.Future

class DutySuspendedQuantitiesControllerSpec extends SpecBase {
  def onwardRoute = Call("GET", "/foo")

  val regime = regimeGen.sample.value

  val formProvider = new DutySuspendedQuantitiesFormProvider()
  val form         = formProvider(regime)

  lazy val dutySuspendedQuantitiesRoute = routes.DutySuspendedQuantitiesController.onPageLoad(NormalMode, regime).url

  "DutySuspendedQuantities Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersAllDSDRegimesSelected)).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedQuantitiesRoute)

        val view = application.injector.instanceOf[DutySuspendedQuantitiesView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, regime, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = userAnswersAllDSDRegimesSelected
        .setByKey(DutySuspendedQuantitiesPage, regime, dutySuspendedQuantities)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedQuantitiesRoute)

        val view = application.injector.instanceOf[DutySuspendedQuantitiesView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form.fill(dutySuspendedQuantities), regime, NormalMode)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must save user answers, calculate volumes and redirect to the next page when valid data is submitted" in {
      val mockCalculatorConnector    = mock[AlcoholDutyCalculatorConnector]
      val mockUserAnswersConnector   = mock[UserAnswersConnector]
      val mockDutySuspendedNavigator = mock[DutySuspendedNavigator]

      when(
        mockCalculatorConnector.calculateDutySuspendedVolumes(eqTo(dutySuspendedQuantities))(any())
      ) thenReturn Future.successful(dutySuspendedFinalVolumes)
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockDutySuspendedNavigator.nextPageWithRegime(eqTo(DutySuspendedQuantitiesPage), any(), any(), any())
      ) thenReturn onwardRoute

      val expectedCachedUserAnswers = userAnswersAllDSDRegimesSelected
        .setByKey(DutySuspendedQuantitiesPage, regime, dutySuspendedQuantities)
        .success
        .value
        .setByKey(DutySuspendedFinalVolumesPage, regime, dutySuspendedFinalVolumes)
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAllDSDRegimesSelected))
          .overrides(
            bind[DutySuspendedNavigator].toInstance(mockDutySuspendedNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockCalculatorConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedQuantitiesRoute)
            .withFormUrlEncodedBody(
              ("totalLitresDeliveredInsideUK", validTotalLitresDeliveredInsideUK.toString),
              ("pureAlcoholDeliveredInsideUK", validPureAlcoholDeliveredInsideUK.toString),
              ("totalLitresDeliveredOutsideUK", validTotalLitresDeliveredOutsideUK.toString),
              ("pureAlcoholDeliveredOutsideUK", validPureAlcoholDeliveredOutsideUK.toString),
              ("totalLitresReceived", validTotalLitresReceived.toString),
              ("pureAlcoholReceived", validPureAlcoholReceived.toString)
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockCalculatorConnector, times(1)).calculateDutySuspendedVolumes(eqTo(dutySuspendedQuantities))(any())
        verify(mockUserAnswersConnector, times(1)).set(eqTo(expectedCachedUserAnswers))(any())
        verify(mockDutySuspendedNavigator, times(1))
          .nextPageWithRegime(
            eqTo(DutySuspendedQuantitiesPage),
            eqTo(NormalMode),
            eqTo(expectedCachedUserAnswers),
            eqTo(regime)
          )
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersAllDSDRegimesSelected)).build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedQuantitiesRoute)
            .withFormUrlEncodedBody(("totalLitresDeliveredInsideUK", "invalid value"))

        val boundForm = form.bind(Map("totalLitresDeliveredInsideUK" -> "invalid value"))

        val view = application.injector.instanceOf[DutySuspendedQuantitiesView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, regime, NormalMode)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to journey recovery when the calculator call fails" in {
      val mockCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
      when(
        mockCalculatorConnector.calculateDutySuspendedVolumes(eqTo(dutySuspendedQuantities))(any())
      ) thenReturn Future.failed(new Exception("Calculation failed"))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAllDSDRegimesSelected))
          .overrides(
            bind[AlcoholDutyCalculatorConnector].toInstance(mockCalculatorConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedQuantitiesRoute)
            .withFormUrlEncodedBody(
              ("totalLitresDeliveredInsideUK", validTotalLitresDeliveredInsideUK.toString),
              ("pureAlcoholDeliveredInsideUK", validPureAlcoholDeliveredInsideUK.toString),
              ("totalLitresDeliveredOutsideUK", validTotalLitresDeliveredOutsideUK.toString),
              ("pureAlcoholDeliveredOutsideUK", validPureAlcoholDeliveredOutsideUK.toString),
              ("totalLitresReceived", validTotalLitresReceived.toString),
              ("pureAlcoholReceived", validPureAlcoholReceived.toString)
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockCalculatorConnector, times(1)).calculateDutySuspendedVolumes(eqTo(dutySuspendedQuantities))(any())
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedQuantitiesRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery if regime is missing for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, dutySuspendedQuantitiesRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedQuantitiesRoute)
            .withFormUrlEncodedBody(
              ("totalLitresDeliveredInsideUK", validTotalLitresDeliveredInsideUK.toString),
              ("pureAlcoholDeliveredInsideUK", validPureAlcoholDeliveredInsideUK.toString),
              ("totalLitresDeliveredOutsideUK", validTotalLitresDeliveredOutsideUK.toString),
              ("pureAlcoholDeliveredOutsideUK", validPureAlcoholDeliveredOutsideUK.toString),
              ("totalLitresReceived", validTotalLitresReceived.toString),
              ("pureAlcoholReceived", validPureAlcoholReceived.toString)
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery if regime is missing for a POST" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, dutySuspendedQuantitiesRoute)
            .withFormUrlEncodedBody(
              ("totalLitresDeliveredInsideUK", validTotalLitresDeliveredInsideUK.toString),
              ("pureAlcoholDeliveredInsideUK", validPureAlcoholDeliveredInsideUK.toString),
              ("totalLitresDeliveredOutsideUK", validTotalLitresDeliveredOutsideUK.toString),
              ("pureAlcoholDeliveredOutsideUK", validPureAlcoholDeliveredOutsideUK.toString),
              ("totalLitresReceived", validTotalLitresReceived.toString),
              ("pureAlcoholReceived", validPureAlcoholReceived.toString)
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
