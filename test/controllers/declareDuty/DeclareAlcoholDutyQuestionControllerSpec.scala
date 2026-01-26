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
import models.AlcoholRegime.Beer
import models.{AlcoholRegimes, NormalMode}
import navigation.ReturnsNavigator
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Gen
import pages.declareDuty.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage, sectionPages}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HttpResponse
import views.html.declareDuty.DeclareAlcoholDutyQuestionView

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
        contentAsString(result) mustEqual view(form, true, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when approved regimes do not include Cider or OFP" in {
      val userAnswers = Gen.oneOf(Seq(userAnswersWithBeer, userAnswersWithWine, userAnswersWithSpirits)).sample.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, declareAlcoholDutyQuestionRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclareAlcoholDutyQuestionView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, showSparklingCider = false, NormalMode)(
          request,
          getMessages(application)
        ).toString
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
      val mockReturnsNavigator     = mock[ReturnsNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockReturnsNavigator.nextPage(eqTo(DeclareAlcoholDutyQuestionPage), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(mockReturnsNavigator),
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

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockReturnsNavigator, times(1))
          .nextPage(eqTo(DeclareAlcoholDutyQuestionPage), eqTo(NormalMode), any(), eqTo(Some(false)))
      }
    }

    "must redirect to the next page when valid data is submitted and user is approved for a single regime" in {

      val userAnswers = emptyUserAnswers.copy(regimes = AlcoholRegimes(Set(Beer)))

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockReturnsNavigator     = mock[ReturnsNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockReturnsNavigator.nextPage(eqTo(DeclareAlcoholDutyQuestionPage), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(mockReturnsNavigator),
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

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockReturnsNavigator, times(1))
          .nextPage(eqTo(DeclareAlcoholDutyQuestionPage), eqTo(NormalMode), any(), eqTo(Some(false)))
      }
    }

    "must redirect to the Task List and clear user answers when valid question is answered as No" in {
      val taskListRoute = controllers.routes.TaskListController.onPageLoad

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockReturnsNavigator     = mock[ReturnsNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockReturnsNavigator.nextPage(eqTo(DeclareAlcoholDutyQuestionPage), any(), any(), any())
      ) thenReturn taskListRoute

      val userAnswers = emptyUserAnswers.copy(data =
        Json.obj(
          DeclareAlcoholDutyQuestionPage.toString -> true,
          AlcoholDutyPage.toString                -> Json.obj(
            "Beer" -> Json.obj(
              "dutiesByTaxType" -> Json.arr(
                Json.obj(
                  "taxType"     -> "311",
                  "totalLitres" -> 100,
                  "pureAlcohol" -> 10,
                  "dutyRate"    -> 9.27,
                  "dutyDue"     -> 92.7
                ),
                Json.obj(
                  "taxType"     -> "361",
                  "totalLitres" -> 100,
                  "pureAlcohol" -> 10,
                  "dutyRate"    -> 10.01,
                  "dutyDue"     -> 100.1
                )
              ),
              "totalDuty"       -> 192.8
            )
          )
        )
      )

      val expectedCachedUserAnswers = emptyUserAnswers.set(DeclareAlcoholDutyQuestionPage, false).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(mockReturnsNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, declareAlcoholDutyQuestionRoute)
            .withFormUrlEncodedBody(("declareAlcoholDutyQuestion-yesNoValue", "false"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual taskListRoute.url

        verify(mockUserAnswersConnector, times(1)).set(eqTo(expectedCachedUserAnswers))(any())
        verify(mockReturnsNavigator, times(1))
          .nextPage(
            eqTo(DeclareAlcoholDutyQuestionPage),
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
