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
import forms.declareDuty.DeleteMultipleSPREntryFormProvider
import org.mockito.ArgumentMatchers.any
import pages.declareDuty.{MultipleSPRListPage, WhatDoYouNeedToDeclarePage}
import play.api.inject.bind
import play.api.test.Helpers._
import connectors.CacheConnector
import models.NormalMode
import uk.gov.hmrc.http.HttpResponse
import views.html.declareDuty.DeleteMultipleSPREntryView

import scala.concurrent.Future

class DeleteMultipleSPREntryControllerSpec extends SpecBase {

  val regime = regimeGen.sample.value

  val index = 0

  val rateBands               = genListOfRateBandForRegimeWithSPR(regime).sample.value.toSet
  val volumeAndRateByTaxTypes = rateBands.map(genVolumeAndRateByTaxTypeRateBand(_).arbitrary.sample.value).toSeq

  val userAnswersWithRegime = emptyUserAnswers
    .setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands)
    .success
    .value

  val userAnswers = userAnswersWithRegime
    .setByKey(MultipleSPRListPage, regime, volumeAndRateByTaxTypes)
    .success
    .value

  val userAnswersWithSingleSPREntry = userAnswersWithRegime
    .setByKey(MultipleSPRListPage, regime, Seq(volumeAndRateByTaxTypes.head))
    .success
    .value

  val formProvider = new DeleteMultipleSPREntryFormProvider()
  val form         = formProvider()

  lazy val deleteMultipleSPREntryRoute             =
    controllers.declareDuty.routes.DeleteMultipleSPREntryController.onPageLoad(regime, Some(index)).url
  lazy val deleteMultipleSPREntryRouteWithoutIndex =
    controllers.declareDuty.routes.DeleteMultipleSPREntryController.onPageLoad(regime, None).url

  "DeleteMultipleSPREntry Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, deleteMultipleSPREntryRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeleteMultipleSPREntryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, regime, index)(request, getMessages(application)).toString
      }
    }

    "must redirect to the journey recovery page if no index specified for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, deleteMultipleSPREntryRouteWithoutIndex)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the Multiple SPR list page when valid data is submitted and the list is not empty" in {
      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, deleteMultipleSPREntryRoute)
            .withFormUrlEncodedBody(("deleteMultipleSPREntry-yesNoValue", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.declareDuty.routes.MultipleSPRListController
          .onPageLoad(regime)
          .url
      }
    }

    "must redirect to the DoYouHaveMultipleSPRDutyRates page when 'yes' is submitted and the list is empty" in {
      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithSingleSPREntry))
          .overrides(
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deleteMultipleSPREntryRoute)
            .withFormUrlEncodedBody(("deleteMultipleSPREntry-yesNoValue", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.declareDuty.routes.DoYouHaveMultipleSPRDutyRatesController
          .onPageLoad(NormalMode, regime)
          .url
      }
    }

    "must redirect to the Multiple SPR List page when 'No' is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithSingleSPREntry)).build()

      running(application) {
        val request =
          FakeRequest(POST, deleteMultipleSPREntryRoute)
            .withFormUrlEncodedBody(("deleteMultipleSPREntry-yesNoValue", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.declareDuty.routes.MultipleSPRListController
          .onPageLoad(regime)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, deleteMultipleSPREntryRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DeleteMultipleSPREntryView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, regime, index)(request, getMessages(application)).toString
      }
    }

    "must redirect to the journey recovery page if no index specified for a POST" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, deleteMultipleSPREntryRouteWithoutIndex)
          .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, deleteMultipleSPREntryRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no index is provided" in {
      val application = applicationBuilder(userAnswers = None).build()

      val deleteMultipleSPREntryRoute =
        controllers.declareDuty.routes.DeleteMultipleSPREntryController.onPageLoad(regime, None).url

      running(application) {
        val request = FakeRequest(GET, deleteMultipleSPREntryRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, deleteMultipleSPREntryRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
