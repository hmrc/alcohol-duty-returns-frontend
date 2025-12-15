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

package controllers

import base.SpecBase
import connectors.UserAnswersConnector
import forms.ClearReturnAreYouSureQuestionFormProvider
import models.AlcoholRegime.Beer
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnAndUserDetails
import uk.gov.hmrc.http.{HttpResponse, UpstreamErrorResponse}
import views.html.ClearReturnAreYouSureQuestionView

import scala.concurrent.Future

class ClearReturnAreYouSureQuestionControllerSpec extends SpecBase {
  "ClearReturnAreYouSureQuestion Controller" - {

    "must return OK and the correct view for a GET" in new SetUp {
      val application = applicationBuilder(userAnswers = Some(answersBeforeClearance))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, clearReturnAreYouSureQuestionRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClearReturnAreYouSureQuestionView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form)(request, getMessages(application)).toString

        verify(mockUserAnswersConnector, never).delete(any(), any())(any())
        verify(mockUserAnswersConnector, never).createUserAnswers(any())(any())
      }
    }

    "must clear down the return, recreate and redirect to the task list page when yes is selected" in new SetUp {
      val application = applicationBuilder(userAnswers = Some(answersBeforeClearance))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      when(mockUserAnswersConnector.delete(any(), any())(any())).thenReturn(
        Future.successful(
          HttpResponse(
            OK,
            s"User answers deleted for user ${answersBeforeClearance.internalId} on return ${answersBeforeClearance.returnId.appaId}/${answersBeforeClearance.returnId.periodKey}"
          )
        )
      )
      when(
        mockUserAnswersConnector.createUserAnswers(
          eqTo(
            ReturnAndUserDetails(
              answersBeforeClearance.returnId,
              answersBeforeClearance.groupId,
              answersBeforeClearance.internalId
            )
          )
        )(any())
      ).thenReturn(Future.successful(Right(emptyUserAnswers)))

      running(application) {
        val request =
          FakeRequest(POST, clearReturnAreYouSureQuestionRoute)
            .withFormUrlEncodedBody(("clearReturnAreYouSureQuestion-yesNoValue", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url

        verify(mockUserAnswersConnector, times(1))
          .delete(eqTo(answersBeforeClearance.returnId.appaId), eqTo(answersBeforeClearance.returnId.periodKey))(any())
        verify(mockUserAnswersConnector, times(1)).createUserAnswers(
          eqTo(
            ReturnAndUserDetails(
              answersBeforeClearance.returnId,
              answersBeforeClearance.groupId,
              answersBeforeClearance.internalId
            )
          )
        )(any())
      }
    }

    "must redirect to journey recovery when yes is selected and unable to clear the return" in new SetUp {
      val application = applicationBuilder(userAnswers = Some(answersBeforeClearance))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      when(mockUserAnswersConnector.delete(any(), any())(any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "Computer says No!")))

      running(application) {
        val request =
          FakeRequest(POST, clearReturnAreYouSureQuestionRoute)
            .withFormUrlEncodedBody(("clearReturnAreYouSureQuestion-yesNoValue", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockUserAnswersConnector, times(1))
          .delete(eqTo(answersBeforeClearance.returnId.appaId), eqTo(answersBeforeClearance.returnId.periodKey))(any())
        verify(mockUserAnswersConnector, never).createUserAnswers(any())(any())
      }
    }

    "must throw an exception when yes is selected and clearing the return throws an exception" in new SetUp {
      val application = applicationBuilder(userAnswers = Some(answersBeforeClearance))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      when(mockUserAnswersConnector.delete(any(), any())(any()))
        .thenReturn(Future.failed(new RuntimeException("Failed")))

      running(application) {
        val request =
          FakeRequest(POST, clearReturnAreYouSureQuestionRoute)
            .withFormUrlEncodedBody(("clearReturnAreYouSureQuestion-yesNoValue", "true"))

        intercept[RuntimeException] {
          await(route(application, request).get)
        }

        verify(mockUserAnswersConnector, times(1))
          .delete(eqTo(answersBeforeClearance.returnId.appaId), eqTo(answersBeforeClearance.returnId.periodKey))(any())
        verify(mockUserAnswersConnector, never).createUserAnswers(any())(any())
      }
    }

    "must redirect to journey recovery when yes is selected and unable to recreate the return" in new SetUp {
      val application = applicationBuilder(userAnswers = Some(answersBeforeClearance))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      when(mockUserAnswersConnector.delete(any(), any())(any())).thenReturn(
        Future.successful(
          HttpResponse(
            OK,
            s"User answers deleted for user ${answersBeforeClearance.internalId} on return ${answersBeforeClearance.returnId.appaId}/${answersBeforeClearance.returnId.periodKey}"
          )
        )
      )
      when(
        mockUserAnswersConnector.createUserAnswers(
          eqTo(
            ReturnAndUserDetails(
              answersBeforeClearance.returnId,
              answersBeforeClearance.groupId,
              answersBeforeClearance.internalId
            )
          )
        )(any())
      ).thenReturn(Future.successful(Left(UpstreamErrorResponse("Computer says No!", INTERNAL_SERVER_ERROR))))

      running(application) {
        val request =
          FakeRequest(POST, clearReturnAreYouSureQuestionRoute)
            .withFormUrlEncodedBody(("clearReturnAreYouSureQuestion-yesNoValue", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockUserAnswersConnector, times(1))
          .delete(eqTo(answersBeforeClearance.returnId.appaId), eqTo(answersBeforeClearance.returnId.periodKey))(any())
        verify(mockUserAnswersConnector, times(1)).createUserAnswers(
          eqTo(
            ReturnAndUserDetails(
              answersBeforeClearance.returnId,
              answersBeforeClearance.groupId,
              answersBeforeClearance.internalId
            )
          )
        )(any())
      }
    }

    "must throw an exception when yes is selected and recreating the return throws an exception" in new SetUp {
      val application = applicationBuilder(userAnswers = Some(answersBeforeClearance))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      when(mockUserAnswersConnector.delete(any(), any())(any())).thenReturn(
        Future.successful(
          HttpResponse(
            OK,
            s"User answers deleted for user ${answersBeforeClearance.internalId} on return ${answersBeforeClearance.returnId.appaId}/${answersBeforeClearance.returnId.periodKey}"
          )
        )
      )
      when(
        mockUserAnswersConnector.createUserAnswers(
          eqTo(
            ReturnAndUserDetails(
              answersBeforeClearance.returnId,
              answersBeforeClearance.groupId,
              answersBeforeClearance.internalId
            )
          )
        )(any())
      ).thenReturn(Future.failed(new RuntimeException("Failed")))

      running(application) {
        val request =
          FakeRequest(POST, clearReturnAreYouSureQuestionRoute)
            .withFormUrlEncodedBody(("clearReturnAreYouSureQuestion-yesNoValue", "true"))

        intercept[RuntimeException] {
          await(route(application, request).get)
        }

        verify(mockUserAnswersConnector, times(1))
          .delete(eqTo(answersBeforeClearance.returnId.appaId), eqTo(answersBeforeClearance.returnId.periodKey))(any())
        verify(mockUserAnswersConnector, times(1)).createUserAnswers(
          eqTo(
            ReturnAndUserDetails(
              answersBeforeClearance.returnId,
              answersBeforeClearance.groupId,
              answersBeforeClearance.internalId
            )
          )
        )(any())
      }
    }

    s"must redirect to the task list page when no is selected" in new SetUp {
      val application = applicationBuilder(userAnswers = Some(answersBeforeClearance))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, clearReturnAreYouSureQuestionRoute)
            .withFormUrlEncodedBody(("clearReturnAreYouSureQuestion-yesNoValue", "false"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url

        verify(mockUserAnswersConnector, never).delete(any(), any())(any())
        verify(mockUserAnswersConnector, never).createUserAnswers(any())(any())
      }
    }
  }

  "must return a Bad Request and errors when no data is submitted" in new SetUp {
    val application = applicationBuilder(userAnswers = Some(answersBeforeClearance))
      .overrides(
        bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
      )
      .build()

    running(application) {
      val request =
        FakeRequest(POST, clearReturnAreYouSureQuestionRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[ClearReturnAreYouSureQuestionView]

      val result = route(application, request).value

      status(result)          mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(boundForm)(request, getMessages(application)).toString

      verify(mockUserAnswersConnector, never).delete(any(), any())(any())
      verify(mockUserAnswersConnector, never).createUserAnswers(any())(any())
    }
  }

  class SetUp {
    val formProvider = new ClearReturnAreYouSureQuestionFormProvider()
    val form         = formProvider()

    val clearReturnAreYouSureQuestionRoute = controllers.routes.ClearReturnAreYouSureQuestionController.onPageLoad().url

    val answersBeforeClearance =
      specifyTellUsAboutAllSingleSPRRate(whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands), Beer)

    val mockUserAnswersConnector = mock[UserAnswersConnector]
  }
}
