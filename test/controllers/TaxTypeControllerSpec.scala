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

package controllers

import base.SpecBase
import forms.TaxTypeFormProvider
import models.{NormalMode, TaxType, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{AlcoholByVolumeQuestionPage, DraughtReliefQuestionPage, SmallProducerReliefQuestionPage, TaxTypePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import viewmodels.TaxTypePageViewModel
import views.html.TaxTypeView

import scala.concurrent.Future

class TaxTypeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val taxTypeRoute = routes.TaxTypeController.onPageLoad(NormalMode).url

  val formProvider = new TaxTypeFormProvider()
  val form         = formProvider()

  val fullUserAnswers = UserAnswers(userAnswersId)
    .set(AlcoholByVolumeQuestionPage, BigDecimal(3.5))
    .success
    .value
    .set(DraughtReliefQuestionPage, true)
    .success
    .value
    .set(SmallProducerReliefQuestionPage, false)
    .success
    .value

  "TaxType Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(fullUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, taxTypeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaxTypeView]

        val viewModel = TaxTypePageViewModel(fullUserAnswers)(messages(application)).get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, viewModel)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = fullUserAnswers.set(TaxTypePage, TaxType.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, taxTypeRoute)

        val view = application.injector.instanceOf[TaxTypeView]

        val result = route(application, request).value

        val viewModel = TaxTypePageViewModel(userAnswers)(messages(application)).get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(TaxType.values.head), NormalMode, viewModel)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, taxTypeRoute)
            .withFormUrlEncodedBody(("value", TaxType.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(fullUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, taxTypeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[TaxTypeView]

        val result = route(application, request).value

        val viewModel = TaxTypePageViewModel(fullUserAnswers)(messages(application)).get

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, viewModel)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET" - {
      "if no existing data is found" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, taxTypeRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "if one of the necessary userAnswer data are missing" in {
        val userAnswers = fullUserAnswers.remove(AlcoholByVolumeQuestionPage).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, taxTypeRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "redirect to Journey Recovery for a POST" - {
      "if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, taxTypeRoute)
              .withFormUrlEncodedBody(("value", TaxType.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
      "if one of the necessary userAnswer data are missing" in {

        val userAnswers = fullUserAnswers.remove(DraughtReliefQuestionPage).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, taxTypeRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
