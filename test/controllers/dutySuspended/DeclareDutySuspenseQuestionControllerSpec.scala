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
import connectors.UserAnswersConnector
import forms.dutySuspended.DeclareDutySuspenseQuestionFormProvider
import models.AlcoholRegime.Beer
import models.{AlcoholRegimes, NormalMode}
import navigation.DutySuspendedNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.dutySuspended.DeclareDutySuspenseQuestionPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.dutySuspended.DeclareDutySuspenseQuestionView

import scala.concurrent.Future

class DeclareDutySuspenseQuestionControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new DeclareDutySuspenseQuestionFormProvider()
  val form         = formProvider()

  lazy val declareDutySuspenseQuestionRoute = routes.DeclareDutySuspenseQuestionController.onPageLoad(NormalMode).url

  "DeclareDutySuspenseQuestion Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, declareDutySuspenseQuestionRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclareDutySuspenseQuestionView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(DeclareDutySuspenseQuestionPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, declareDutySuspenseQuestionRoute)

        val view = application.injector.instanceOf[DeclareDutySuspenseQuestionView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersConnector   = mock[UserAnswersConnector]
      val mockDutySuspendedNavigator = mock[DutySuspendedNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockDutySuspendedNavigator.nextPage(eqTo(DeclareDutySuspenseQuestionPage), any(), any(), any())
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
          FakeRequest(POST, declareDutySuspenseQuestionRoute)
            .withFormUrlEncodedBody(("declare-duty-suspended-deliveries-input", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockDutySuspendedNavigator, times(1))
          .nextPage(eqTo(DeclareDutySuspenseQuestionPage), eqTo(NormalMode), any(), eqTo(Some(false)))
      }
    }

    "must redirect to the next page when valid data is submitted and user is approved for a single regime" in {

      val userAnswers = emptyUserAnswers.copy(regimes = AlcoholRegimes(Set(Beer)))

      val mockUserAnswersConnector   = mock[UserAnswersConnector]
      val mockDutySuspendedNavigator = mock[DutySuspendedNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockDutySuspendedNavigator.nextPage(eqTo(DeclareDutySuspenseQuestionPage), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[DutySuspendedNavigator].toInstance(mockDutySuspendedNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, declareDutySuspenseQuestionRoute)
            .withFormUrlEncodedBody(("declare-duty-suspended-deliveries-input", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockDutySuspendedNavigator, times(1))
          .nextPage(eqTo(DeclareDutySuspenseQuestionPage), eqTo(NormalMode), any(), eqTo(Some(false)))
      }
    }

    "must redirect to the Task list and clear subsequent pages from user answers when declare duty suspense question is answered as No" in {
      val taskListRoute = controllers.routes.TaskListController.onPageLoad

      val mockUserAnswersConnector   = mock[UserAnswersConnector]
      val mockDutySuspendedNavigator = mock[DutySuspendedNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockDutySuspendedNavigator.nextPage(eqTo(DeclareDutySuspenseQuestionPage), any(), any(), any())
      ) thenReturn taskListRoute

      val expectedCachedUserAnswers = emptyUserAnswers.set(DeclareDutySuspenseQuestionPage, false).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithDutySuspendedData))
        .overrides(
          bind[DutySuspendedNavigator].toInstance(mockDutySuspendedNavigator),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, declareDutySuspenseQuestionRoute).withFormUrlEncodedBody(
            ("declare-duty-suspended-deliveries-input", "false")
          )
        val result  = route(application, request).value
        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual taskListRoute.url

        verify(mockUserAnswersConnector, times(1)).set(eqTo(expectedCachedUserAnswers))(any())
        verify(mockDutySuspendedNavigator, times(1))
          .nextPage(
            eqTo(DeclareDutySuspenseQuestionPage),
            eqTo(NormalMode),
            eqTo(expectedCachedUserAnswers),
            eqTo(Some(false))
          )
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, declareDutySuspenseQuestionRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DeclareDutySuspenseQuestionView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, declareDutySuspenseQuestionRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, declareDutySuspenseQuestionRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
