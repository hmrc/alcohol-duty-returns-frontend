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
import models.NormalMode
import navigation.DutySuspendedNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.dutySuspended._
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import views.html.dutySuspended.DisplayCalculationView

class DisplayCalculationControllerSpec extends SpecBase {
  def onwardRoute = Call("GET", "/foo")

  val regime = regimeGen.sample.value

  lazy val displayCalculationRoute = routes.DisplayCalculationController.onPageLoad(regime).url

  val userAnswersWithDataForRegime = userAnswersAllDSDRegimesSelected
    .setByKey(DutySuspendedQuantitiesPage, regime, dutySuspendedQuantities)
    .success
    .value
    .setByKey(DutySuspendedFinalVolumesPage, regime, dutySuspendedFinalVolumes)
    .success
    .value

  "DisplayCalculation Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithDataForRegime)).build()

      running(application) {
        val request = FakeRequest(GET, displayCalculationRoute)

        val view = application.injector.instanceOf[DisplayCalculationView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(regime, dutySuspendedQuantities, dutySuspendedFinalVolumes)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to the next page for a POST" in {
      val mockDutySuspendedNavigator = mock[DutySuspendedNavigator]

      when(
        mockDutySuspendedNavigator.nextPageWithRegime(eqTo(DisplayCalculationPage), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithDataForRegime))
          .overrides(
            bind[DutySuspendedNavigator].toInstance(mockDutySuspendedNavigator)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, displayCalculationRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockDutySuspendedNavigator, times(1))
          .nextPageWithRegime(
            eqTo(DisplayCalculationPage),
            eqTo(NormalMode),
            eqTo(userAnswersWithDataForRegime),
            eqTo(regime)
          )
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, displayCalculationRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if declared quantities or calculated volumes are missing" in {
      val userAnswersNoDeclaredQuantities = userAnswersAllDSDRegimesSelected
        .setByKey(DutySuspendedFinalVolumesPage, regime, dutySuspendedFinalVolumes)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswersNoDeclaredQuantities)).build()

      running(application) {
        val request = FakeRequest(GET, displayCalculationRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, displayCalculationRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST for a GET if declared quantities or calculated volumes are missing" in {
      val userAnswersNoFinalVolumes = userAnswersAllDSDRegimesSelected
        .setByKey(DutySuspendedQuantitiesPage, regime, dutySuspendedQuantities)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswersNoFinalVolumes)).build()

      running(application) {
        val request = FakeRequest(POST, displayCalculationRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
