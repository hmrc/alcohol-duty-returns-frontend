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

package controllers.adjustment

import base.SpecBase
import forms.adjustment.DeclareAdjustmentQuestionFormProvider
import models.{NormalMode, ReturnId, UserAnswers}
import navigation.{AdjustmentNavigator, FakeAdjustmentNavigator}
import org.mockito.ArgumentMatchers.any
import pages.adjustment.{AdjustmentEntryListPage, AdjustmentListPage, AdjustmentTotalPage, CurrentAdjustmentEntryPage, DeclareAdjustmentQuestionPage, OverDeclarationReasonPage, OverDeclarationTotalPage, UnderDeclarationReasonPage, UnderDeclarationTotalPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.UserAnswersConnector
import org.mockito.ArgumentMatchersSugar.eqTo
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.DeclareAdjustmentQuestionView

import scala.util.Success
import scala.concurrent.Future

class DeclareAdjustmentQuestionControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider  = new DeclareAdjustmentQuestionFormProvider()
  val form          = formProvider()
  val pagesToDelete = List(
    AdjustmentEntryListPage,
    AdjustmentListPage,
    CurrentAdjustmentEntryPage,
    AdjustmentTotalPage,
    UnderDeclarationTotalPage,
    OverDeclarationTotalPage,
    UnderDeclarationReasonPage,
    OverDeclarationReasonPage
  )

  lazy val declareAdjustmentQuestionRoute = routes.DeclareAdjustmentQuestionController.onPageLoad(NormalMode).url

  "DeclareAdjustmentQuestion Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, declareAdjustmentQuestionRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclareAdjustmentQuestionView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(DeclareAdjustmentQuestionPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, declareAdjustmentQuestionRoute)

        val view = application.injector.instanceOf[DeclareAdjustmentQuestionView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator]
              .toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = Some(true))),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, declareAdjustmentQuestionRoute)
            .withFormUrlEncodedBody(("declare-adjustment-question-value", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the Task list and clear user answers when valid question is answered as No" in {
      val mockUserAnswersConnector     = mock[UserAnswersConnector]
      val mockUserAnswers: UserAnswers = mock[UserAnswers]
      val mockReturnId                 = mock[ReturnId]
      when(mockReturnId.periodKey) thenReturn "2025-01"
      when(mockUserAnswers.returnId) thenReturn mockReturnId
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockUserAnswers.set(eqTo(DeclareAdjustmentQuestionPage), eqTo(false))(any())
      ) thenReturn Success(
        mockUserAnswers
      )
      when(mockUserAnswers.remove(eqTo(pagesToDelete))) thenReturn emptyUserAnswers.set(
        DeclareAdjustmentQuestionPage,
        false
      )

      val application =
        applicationBuilder(userAnswers = Some(mockUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, Some(true))),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, declareAdjustmentQuestionRoute)
            .withFormUrlEncodedBody(("declare-adjustment-question-value", "false"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockUserAnswers, times(1)).set(eqTo(DeclareAdjustmentQuestionPage), eqTo(false))(any())
        verify(mockUserAnswers, times(1)).remove(eqTo(pagesToDelete))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, declareAdjustmentQuestionRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DeclareAdjustmentQuestionView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, declareAdjustmentQuestionRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, declareAdjustmentQuestionRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
