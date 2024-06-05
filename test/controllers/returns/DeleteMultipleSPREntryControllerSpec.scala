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

package controllers.returns

import base.SpecBase
import forms.returns.DeleteMultipleSPREntryFormProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.returns.{DeleteMultipleSPREntryPage, MultipleSPRListPage}
import play.api.inject.bind
import play.api.test.Helpers._
import connectors.CacheConnector
import models.NormalMode
import uk.gov.hmrc.http.HttpResponse
import views.html.returns.DeleteMultipleSPREntryView

import scala.concurrent.Future

class DeleteMultipleSPREntryControllerSpec extends SpecBase with MockitoSugar {

  val regime = regimeGen.sample.value

  val index = 0

  val formProvider = new DeleteMultipleSPREntryFormProvider()
  val form         = formProvider()

  lazy val deleteMultipleSPREntryRoute =
    controllers.returns.routes.DeleteMultipleSPREntryController.onPageLoad(regime, index).url

  "DeleteMultipleSPREntry Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, deleteMultipleSPREntryRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeleteMultipleSPREntryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, regime, index)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(DeleteMultipleSPREntryPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, deleteMultipleSPREntryRoute)

        val view = application.injector.instanceOf[DeleteMultipleSPREntryView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), regime, index)(request, messages(application)).toString
      }
    }

    "must redirect to the Multiple SPR list page when valid data is submitted and the list is not empty" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val rateBandList    = arbitraryRateBandList(regime).arbitrary.suchThat(_.nonEmpty).sample.value
      val dutiesByTaxType = rateBandList.map(rateBand => genDutyTaxTypesFromRateBand(rateBand).arbitrary.sample.value)

      val userAnswers = emptyUserAnswers
        .setByKey(MultipleSPRListPage, regime, dutiesByTaxType)
        .success
        .value

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
        redirectLocation(result).value mustEqual controllers.returns.routes.MultipleSPRListController
          .onPageLoad(regime)
          .url
      }
    }

    "must redirect to the DoYouHaveMultipleSPRDutyRates page when valid data is submitted and the list is empty" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
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
        redirectLocation(result).value mustEqual controllers.returns.routes.DoYouHaveMultipleSPRDutyRatesController
          .onPageLoad(NormalMode, regime)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, deleteMultipleSPREntryRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DeleteMultipleSPREntryView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, regime, index)(request, messages(application)).toString
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
