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
import connectors.UserAnswersConnector
import forms.adjustment.DeclareAdjustmentQuestionFormProvider
import models.NormalMode
import navigation.AdjustmentNavigator
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import pages.adjustment.*
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.DeclareAdjustmentQuestionView

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
      val mockAdjustmentNavigator  = mock[AdjustmentNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockAdjustmentNavigator.nextPage(eqTo(DeclareAdjustmentQuestionPage), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
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

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockAdjustmentNavigator, times(1))
          .nextPage(eqTo(DeclareAdjustmentQuestionPage), eqTo(NormalMode), any(), eqTo(Some(true)))
      }
    }

    "must redirect to the Task list and clear user answers when valid question is answered as No" in {
      val taskListRoute = controllers.routes.TaskListController.onPageLoad

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockAdjustmentNavigator  = mock[AdjustmentNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockAdjustmentNavigator.nextPage(eqTo(DeclareAdjustmentQuestionPage), any(), any(), any())
      ) thenReturn taskListRoute

      val userAnswers = emptyUserAnswers.copy(data =
        Json.obj(
          DeclareAdjustmentQuestionPage.toString -> true,
          AdjustmentTotalPage.toString           -> -515.67,
          UnderDeclarationTotalPage.toString     -> 1019.7,
          UnderDeclarationReasonPage.toString    -> "Reason for under declaration",
          OverDeclarationTotalPage.toString      -> -1854,
          OverDeclarationReasonPage.toString     -> "Reason for over declaration"
        )
      )

      val expectedCachedUserAnswers = emptyUserAnswers.set(DeclareAdjustmentQuestionPage, false).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, declareAdjustmentQuestionRoute)
            .withFormUrlEncodedBody(("declare-adjustment-question-value", "false"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual taskListRoute.url

        verify(mockUserAnswersConnector, times(1)).set(eqTo(expectedCachedUserAnswers))(any())
        verify(mockAdjustmentNavigator, times(1))
          .nextPage(
            eqTo(DeclareAdjustmentQuestionPage),
            eqTo(NormalMode),
            eqTo(expectedCachedUserAnswers),
            eqTo(Some(true))
          )
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
