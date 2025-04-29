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

package controllers.dutySuspended

import base.SpecBase
import connectors.UserAnswersConnector
import forms.dutySuspended.DeclareDutySuspendedDeliveriesQuestionFormProvider
import models.{NormalMode, ReturnId, UserAnswers}
import navigation.DeclareDutySuspendedDeliveriesNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.dutySuspended._
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.dutySuspended.DeclareDutySuspendedDeliveriesQuestionView

import scala.concurrent.Future
import scala.util.Success

class DeclareDutySuspendedDeliveriesQuestionControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new DeclareDutySuspendedDeliveriesQuestionFormProvider()
  val form         = formProvider()

  val pagesToDelete = List(
    DutySuspendedBeerPage,
    DutySuspendedCiderPage,
    DutySuspendedWinePage,
    DutySuspendedSpiritsPage,
    DutySuspendedOtherFermentedPage
  )

  lazy val declareDutySuspendedDeliveriesQuestionRoute =
    routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(NormalMode).url

  override def configOverrides: Map[String, Any] = Map(
    "features.duty-suspended-new-journey" -> false
  )

  "DeclareDutySuspendedDeliveriesQuestion Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, declareDutySuspendedDeliveriesQuestionRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclareDutySuspendedDeliveriesQuestionView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(DeclareDutySuspendedDeliveriesQuestionPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, declareDutySuspendedDeliveriesQuestionRoute)

        val view = application.injector.instanceOf[DeclareDutySuspendedDeliveriesQuestionView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersConnector             = mock[UserAnswersConnector]
      val mockDutySuspendedDeliveriesNavigator = mock[DeclareDutySuspendedDeliveriesNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockDutySuspendedDeliveriesNavigator.nextPage(eqTo(DeclareDutySuspendedDeliveriesQuestionPage), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[DeclareDutySuspendedDeliveriesNavigator].toInstance(mockDutySuspendedDeliveriesNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, declareDutySuspendedDeliveriesQuestionRoute)
            .withFormUrlEncodedBody(("declare-duty-suspended-deliveries-input", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockDutySuspendedDeliveriesNavigator, times(1))
          .nextPage(eqTo(DeclareDutySuspendedDeliveriesQuestionPage), eqTo(NormalMode), any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, declareDutySuspendedDeliveriesQuestionRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DeclareDutySuspendedDeliveriesQuestionView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, declareDutySuspendedDeliveriesQuestionRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, declareDutySuspendedDeliveriesQuestionRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the Task list and clear user answers when declare duty suspended deliveries question is answered as No" in {
      val taskListRoute = controllers.routes.TaskListController.onPageLoad

      val mockUserAnswersConnector             = mock[UserAnswersConnector]
      val mockUserAnswers: UserAnswers         = mock[UserAnswers]
      val mockDutySuspendedDeliveriesNavigator = mock[DeclareDutySuspendedDeliveriesNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockUserAnswers.set(eqTo(DeclareDutySuspendedDeliveriesQuestionPage), eqTo(false))(any())
      ) thenReturn Success(
        mockUserAnswers
      )
      val mockReturnId = mock[ReturnId]
      when(mockReturnId.periodKey) thenReturn "2025-01"
      when(mockUserAnswers.returnId) thenReturn mockReturnId
      when(mockUserAnswers.remove(eqTo(pagesToDelete))) thenReturn emptyUserAnswers.set(
        DeclareDutySuspendedDeliveriesQuestionPage,
        false
      )
      when(
        mockDutySuspendedDeliveriesNavigator.nextPage(eqTo(DeclareDutySuspendedDeliveriesQuestionPage), any(), any())
      ) thenReturn taskListRoute

      val application = applicationBuilder(userAnswers = Some(mockUserAnswers))
        .overrides(
          bind[DeclareDutySuspendedDeliveriesNavigator].toInstance(mockDutySuspendedDeliveriesNavigator),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, declareDutySuspendedDeliveriesQuestionRoute).withFormUrlEncodedBody(
            ("declare-duty-suspended-deliveries-input", "false")
          )
        val result  = route(application, request).value
        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual taskListRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockUserAnswers, times(1)).set(eqTo(DeclareDutySuspendedDeliveriesQuestionPage), eqTo(false))(any())
        verify(mockUserAnswers, times(1)).remove(eqTo(pagesToDelete))
        verify(mockDutySuspendedDeliveriesNavigator, times(1))
          .nextPage(eqTo(DeclareDutySuspendedDeliveriesQuestionPage), eqTo(NormalMode), any())
      }
    }
  }
}
