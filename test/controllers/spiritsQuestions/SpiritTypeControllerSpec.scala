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

package controllers.spiritsQuestions

import base.SpecBase
import forms.spiritsQuestions.SpiritTypeFormProvider
import models.{NormalMode, SpiritType}
import navigation.{FakeQuarterlySpiritsQuestionsNavigator, QuarterlySpiritsQuestionsNavigator}
import org.mockito.ArgumentMatchers.any
import pages.spiritsQuestions.SpiritTypePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.CacheConnector
import views.html.spiritsQuestions.SpiritTypeView
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class SpiritTypeControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  lazy val spiritTypeRoute = routes.SpiritTypeController.onPageLoad(NormalMode).url

  val formProvider = new SpiritTypeFormProvider()
  val form         = formProvider()

  "SpiritType Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, spiritTypeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SpiritTypeView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(SpiritTypePage, SpiritType.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, spiritTypeRoute)

        val view = application.injector.instanceOf[SpiritTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(SpiritType.values.toSet), NormalMode)(
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
            bind[QuarterlySpiritsQuestionsNavigator]
              .toInstance(new FakeQuarterlySpiritsQuestionsNavigator(onwardRoute, hasValueChanged = true)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, spiritTypeRoute)
            .withFormUrlEncodedBody(("value[0]", SpiritType.Other.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when valid data is submitted and Other spirit type is unselected" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[QuarterlySpiritsQuestionsNavigator]
              .toInstance(new FakeQuarterlySpiritsQuestionsNavigator(onwardRoute, hasValueChanged = false)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, spiritTypeRoute)
            .withFormUrlEncodedBody(("value[0]", SpiritType.Grainspirits.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when valid data is submitted and Other spirit type was previously selected and is now unselected" in {

      val userAnswers = emptyUserAnswers
        .set(pages.spiritsQuestions.SpiritTypePage, Set[SpiritType](SpiritType.Maltspirits, SpiritType.Other))
        .success
        .value

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[QuarterlySpiritsQuestionsNavigator]
              .toInstance(new FakeQuarterlySpiritsQuestionsNavigator(onwardRoute, hasValueChanged = false)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, spiritTypeRoute)
            .withFormUrlEncodedBody(("value[0]", SpiritType.Maltspirits.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, spiritTypeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[SpiritTypeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, spiritTypeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, spiritTypeRoute)
            .withFormUrlEncodedBody(("value[0]", SpiritType.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
