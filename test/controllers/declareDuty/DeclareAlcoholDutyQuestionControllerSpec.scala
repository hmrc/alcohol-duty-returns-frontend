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
import connectors.UserAnswersConnector
import forms.declareDuty.DeclareAlcoholDutyQuestionFormProvider
import models.AlcoholRegime.{Beer, Cider, Wine}
import models.{AlcoholRegimes, NormalMode, ReturnId, UserAnswers}
import navigation.{FakeReturnsNavigator, ReturnsNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.declareDuty.{DeclareAlcoholDutyQuestionPage, sectionPages}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.declareDuty.DeclareAlcoholDutyQuestionView

import scala.util.Success
import scala.concurrent.Future

class DeclareAlcoholDutyQuestionControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider  = new DeclareAlcoholDutyQuestionFormProvider()
  val form          = formProvider()
  val pagesToDelete = sectionPages.toList

  lazy val declareAlcoholDutyQuestionRoute = routes.DeclareAlcoholDutyQuestionController.onPageLoad(NormalMode).url

  "DeclareAlcoholDutyQuestion Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, declareAlcoholDutyQuestionRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclareAlcoholDutyQuestionView]

        status(result)          mustEqual OK
        // TODO: make it testable (contains Cider flag depends on regimes)
        contentAsString(result) mustEqual view(form, true, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(DeclareAlcoholDutyQuestionPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, declareAlcoholDutyQuestionRoute)

        val view = application.injector.instanceOf[DeclareAlcoholDutyQuestionView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), true, NormalMode)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(new FakeReturnsNavigator(onwardRoute)),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, declareAlcoholDutyQuestionRoute)
            .withFormUrlEncodedBody(("declareAlcoholDutyQuestion-yesNoValue", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when valid data is submitted and user is approved for a single regime" in {

      val userAnswers              = emptyUserAnswers.copy(regimes = AlcoholRegimes(Set(Beer)))
      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(new FakeReturnsNavigator(onwardRoute)),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, declareAlcoholDutyQuestionRoute)
            .withFormUrlEncodedBody(("declareAlcoholDutyQuestion-yesNoValue", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the index page when valid question is answered as No" in {
      val mockUserAnswersConnector              = mock[UserAnswersConnector]
      val mockUserAnswers: UserAnswers          = mock[UserAnswers]
      val mockAlcoholRegimesSet: AlcoholRegimes = mock[AlcoholRegimes]
      val mockReturnId                          = mock[ReturnId]
      when(mockReturnId.periodKey) thenReturn "2025-01"
      when(mockUserAnswers.returnId) thenReturn mockReturnId
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockUserAnswers.set(eqTo(DeclareAlcoholDutyQuestionPage), eqTo(false))(any())
      ) thenReturn Success(
        mockUserAnswers
      )
      when(mockUserAnswers.remove(eqTo(pagesToDelete))) thenReturn emptyUserAnswers.set(
        DeclareAlcoholDutyQuestionPage,
        false
      )
      when(mockUserAnswers.regimes) thenReturn mockAlcoholRegimesSet
      when(mockAlcoholRegimesSet.regimes) thenReturn Set(Cider, Wine)

      val application =
        applicationBuilder(userAnswers = Some(mockUserAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(new FakeReturnsNavigator(onwardRoute)),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, declareAlcoholDutyQuestionRoute)
            .withFormUrlEncodedBody(("declareAlcoholDutyQuestion-yesNoValue", "false"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockUserAnswers, times(1)).set(eqTo(DeclareAlcoholDutyQuestionPage), eqTo(false))(any())
        verify(mockUserAnswers, times(1)).remove(eqTo(pagesToDelete))
        verify(mockUserAnswers, times(1)).regimes
        verify(mockAlcoholRegimesSet, times(1)).regimes
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, declareAlcoholDutyQuestionRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DeclareAlcoholDutyQuestionView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, true, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, declareAlcoholDutyQuestionRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, declareAlcoholDutyQuestionRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
