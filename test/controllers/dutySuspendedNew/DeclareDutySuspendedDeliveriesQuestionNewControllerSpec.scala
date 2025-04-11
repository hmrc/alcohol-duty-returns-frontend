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

package controllers.dutySuspendedNew

import base.SpecBase
import connectors.UserAnswersConnector
import forms.dutySuspendedNew.DeclareDutySuspendedDeliveriesQuestionNewFormProvider
import models.AlcoholRegime.{Beer, Cider, Wine}
import models.{AlcoholRegimes, NormalMode, ReturnId, UserAnswers}
import navigation.DeclareDutySuspendedDeliveriesNewNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.dutySuspended._
import pages.dutySuspendedNew.{DeclareDutySuspendedDeliveriesQuestionNewPage, DutySuspendedAlcoholTypePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.dutySuspendedNew.DeclareDutySuspendedDeliveriesQuestionNewView

import scala.concurrent.Future
import scala.util.Success

class DeclareDutySuspendedDeliveriesQuestionNewControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new DeclareDutySuspendedDeliveriesQuestionNewFormProvider()
  val form         = formProvider()

  val pagesToDelete = List(
    // TODO: add other pages from new journey to be removed if user submits 'No'
    DutySuspendedAlcoholTypePage
  )

  lazy val declareDutySuspendedDeliveriesQuestionRoute =
    routes.DeclareDutySuspendedDeliveriesQuestionNewController.onPageLoad(NormalMode).url

  override def configOverrides: Map[String, Any] = Map(
    "features.duty-suspended-new-journey" -> true
  )

  "DeclareDutySuspendedDeliveriesQuestion Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, declareDutySuspendedDeliveriesQuestionRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclareDutySuspendedDeliveriesQuestionNewView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(DeclareDutySuspendedDeliveriesQuestionNewPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, declareDutySuspendedDeliveriesQuestionRoute)

        val view = application.injector.instanceOf[DeclareDutySuspendedDeliveriesQuestionNewView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersConnector             = mock[UserAnswersConnector]
      val mockDutySuspendedDeliveriesNavigator = mock[DeclareDutySuspendedDeliveriesNewNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockDutySuspendedDeliveriesNavigator
          .nextPage(eqTo(DeclareDutySuspendedDeliveriesQuestionNewPage), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[DeclareDutySuspendedDeliveriesNewNavigator].toInstance(mockDutySuspendedDeliveriesNavigator),
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
          .nextPage(eqTo(DeclareDutySuspendedDeliveriesQuestionNewPage), eqTo(NormalMode), any(), eqTo(Some(false)))
      }
    }

    "must redirect to the next page when valid data is submitted and user is approved for a single regime" in {

      val userAnswers = emptyUserAnswers.copy(regimes = AlcoholRegimes(Set(Beer)))

      val mockUserAnswersConnector             = mock[UserAnswersConnector]
      val mockDutySuspendedDeliveriesNavigator = mock[DeclareDutySuspendedDeliveriesNewNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockDutySuspendedDeliveriesNavigator
          .nextPage(eqTo(DeclareDutySuspendedDeliveriesQuestionNewPage), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[DeclareDutySuspendedDeliveriesNewNavigator].toInstance(mockDutySuspendedDeliveriesNavigator),
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
          .nextPage(eqTo(DeclareDutySuspendedDeliveriesQuestionNewPage), eqTo(NormalMode), any(), eqTo(Some(false)))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, declareDutySuspendedDeliveriesQuestionRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DeclareDutySuspendedDeliveriesQuestionNewView]

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

      val mockUserAnswersConnector              = mock[UserAnswersConnector]
      val mockUserAnswers: UserAnswers          = mock[UserAnswers]
      val mockAlcoholRegimesSet: AlcoholRegimes = mock[AlcoholRegimes]
      val mockReturnId                          = mock[ReturnId]
      val mockDutySuspendedDeliveriesNavigator  = mock[DeclareDutySuspendedDeliveriesNewNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockUserAnswers.set(eqTo(DeclareDutySuspendedDeliveriesQuestionNewPage), eqTo(false))(any())
      ) thenReturn Success(
        mockUserAnswers
      )
      when(mockReturnId.periodKey) thenReturn "2025-01"
      when(mockUserAnswers.returnId) thenReturn mockReturnId
      when(mockUserAnswers.regimes) thenReturn mockAlcoholRegimesSet
      when(mockAlcoholRegimesSet.regimes) thenReturn Set(Cider, Wine)
      when(mockUserAnswers.remove(eqTo(pagesToDelete))) thenReturn emptyUserAnswers.set(
        DeclareDutySuspendedDeliveriesQuestionPage,
        false
      )
      when(
        mockDutySuspendedDeliveriesNavigator
          .nextPage(eqTo(DeclareDutySuspendedDeliveriesQuestionNewPage), any(), any(), any())
      ) thenReturn taskListRoute

      val application = applicationBuilder(userAnswers = Some(mockUserAnswers))
        .overrides(
          bind[DeclareDutySuspendedDeliveriesNewNavigator].toInstance(mockDutySuspendedDeliveriesNavigator),
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
        verify(mockUserAnswers, times(1)).set(eqTo(DeclareDutySuspendedDeliveriesQuestionNewPage), eqTo(false))(any())
        verify(mockUserAnswers, times(1)).remove(eqTo(pagesToDelete))
        verify(mockUserAnswers, times(1)).regimes
        verify(mockAlcoholRegimesSet, times(1)).regimes
        verify(mockDutySuspendedDeliveriesNavigator, times(1))
          .nextPage(eqTo(DeclareDutySuspendedDeliveriesQuestionNewPage), eqTo(NormalMode), any(), eqTo(Some(false)))
      }
    }
  }
}
