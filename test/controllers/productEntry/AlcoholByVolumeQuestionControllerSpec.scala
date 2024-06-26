/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.productEntry

import base.SpecBase
import connectors.{AlcoholDutyCalculatorConnector, CacheConnector}
import forms.productEntry.AlcoholByVolumeQuestionFormProvider
import models.RateType.Core
import models.productEntry.ProductEntry
import models.{AlcoholByVolume, NormalMode, RateTypeResponse}
import navigation.{FakeProductEntryNavigator, ProductEntryNavigator}
import org.mockito.ArgumentMatchers.any
import pages.productEntry.CurrentProductEntryPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.productEntry.AlcoholByVolumeQuestionView

import scala.concurrent.Future

class AlcoholByVolumeQuestionControllerSpec extends SpecBase {

  val formProvider = new AlcoholByVolumeQuestionFormProvider()
  val form         = formProvider()

  def onwardRoute = Call("GET", "/foo")

  val validAnswer = AlcoholByVolume(10.2)

  val rateType = Core

  lazy val alcoholByVolumeQuestionRoute = routes.AlcoholByVolumeQuestionController.onPageLoad(NormalMode).url

  "AlcoholByVolumeQuestion Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, alcoholByVolumeQuestionRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AlcoholByVolumeQuestionView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        emptyUserAnswers.set(CurrentProductEntryPage, ProductEntry(abv = Some(validAnswer))).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, alcoholByVolumeQuestionRoute)

        val view = application.injector.instanceOf[AlcoholByVolumeQuestionView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer.value), NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockAlcoholDutyCalculatorConnector.rateType(any(), any(), any())(any())) thenReturn Future.successful(
        RateTypeResponse(rateType)
      )

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute, hasValueChanged = true)),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, alcoholByVolumeQuestionRoute)
            .withFormUrlEncodedBody(("alcohol-by-volume-input", validAnswer.value.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when the same data is submitted" in {
      val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockAlcoholDutyCalculatorConnector.rateType(any(), any(), any())(any())) thenReturn Future.successful(
        RateTypeResponse(rateType)
      )
      val userAnswers                        =
        emptyUserAnswers
          .set(CurrentProductEntryPage, ProductEntry(abv = Some(validAnswer), rateType = Some(rateType)))
          .success
          .value

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ProductEntryNavigator].toInstance(new FakeProductEntryNavigator(onwardRoute, hasValueChanged = false)),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, alcoholByVolumeQuestionRoute)
            .withFormUrlEncodedBody(("alcohol-by-volume-input", validAnswer.value.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, alcoholByVolumeQuestionRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AlcoholByVolumeQuestionView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, alcoholByVolumeQuestionRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, alcoholByVolumeQuestionRoute)
            .withFormUrlEncodedBody(("value", validAnswer.value.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
