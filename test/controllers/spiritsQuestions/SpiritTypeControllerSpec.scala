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
import connectors.UserAnswersConnector
import forms.spiritsQuestions.SpiritTypeFormProvider
import models.{NormalMode, SpiritType, UserAnswers}
import navigation.QuarterlySpiritsQuestionsNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.spiritsQuestions.SpiritTypePage
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.spiritsQuestions.SpiritTypeView

import scala.concurrent.Future

class SpiritTypeControllerSpec extends SpecBase {
  val userAnswersPreviouslyAnswered = emptyUserAnswers.set(SpiritTypePage, SpiritType.values.toSet).success.value

  "SpiritType Controller" - {
    "must return OK and the correct view for a GET" in new SetUp(Some(emptyUserAnswers)) {
      running(application) {
        val request = FakeRequest(GET, spiritTypeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SpiritTypeView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in new SetUp(
      Some(userAnswersPreviouslyAnswered)
    ) {
      running(application) {
        val request = FakeRequest(GET, spiritTypeRoute)

        val view = application.injector.instanceOf[SpiritTypeView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form.fill(SpiritType.values.toSet), NormalMode)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in new SetUp(Some(emptyUserAnswers)) {
      val mockUserAnswersConnector               = mock[UserAnswersConnector]
      val mockQuarterlySpiritsQuestionsNavigator = mock[QuarterlySpiritsQuestionsNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockQuarterlySpiritsQuestionsNavigator.nextPage(eqTo(SpiritTypePage), any(), any(), any())
      ) thenReturn onwardRoute

      override val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[QuarterlySpiritsQuestionsNavigator].toInstance(mockQuarterlySpiritsQuestionsNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, spiritTypeRoute)
            .withFormUrlEncodedBody(("value[0]", SpiritType.Other.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockQuarterlySpiritsQuestionsNavigator, times(1))
          .nextPage(eqTo(SpiritTypePage), eqTo(NormalMode), any(), eqTo(Some(true)))
      }
    }

    "must redirect to the next page when valid data is submitted and Other spirit type is unselected" in new SetUp(
      Some(emptyUserAnswers)
    ) {
      val mockUserAnswersConnector               = mock[UserAnswersConnector]
      val mockQuarterlySpiritsQuestionsNavigator = mock[QuarterlySpiritsQuestionsNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockQuarterlySpiritsQuestionsNavigator.nextPage(eqTo(SpiritTypePage), any(), any(), any())
      ) thenReturn onwardRoute

      override val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[QuarterlySpiritsQuestionsNavigator].toInstance(mockQuarterlySpiritsQuestionsNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, spiritTypeRoute)
            .withFormUrlEncodedBody(("value[0]", SpiritType.Grainspirits.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockQuarterlySpiritsQuestionsNavigator, times(1))
          .nextPage(eqTo(SpiritTypePage), eqTo(NormalMode), any(), eqTo(Some(false)))
      }
    }

    "must redirect to the next page when valid data is submitted and Other spirit type was previously selected and is now unselected" in new SetUp(
      Some(emptyUserAnswers)
    ) {
      val userAnswers = emptyUserAnswers
        .set(pages.spiritsQuestions.SpiritTypePage, Set[SpiritType](SpiritType.Maltspirits, SpiritType.Other))
        .success
        .value

      val mockUserAnswersConnector               = mock[UserAnswersConnector]
      val mockQuarterlySpiritsQuestionsNavigator = mock[QuarterlySpiritsQuestionsNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockQuarterlySpiritsQuestionsNavigator.nextPage(eqTo(SpiritTypePage), any(), any(), any())
      ) thenReturn onwardRoute

      override val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[QuarterlySpiritsQuestionsNavigator].toInstance(mockQuarterlySpiritsQuestionsNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, spiritTypeRoute)
            .withFormUrlEncodedBody(("value[0]", SpiritType.Maltspirits.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockQuarterlySpiritsQuestionsNavigator, times(1))
          .nextPage(eqTo(SpiritTypePage), eqTo(NormalMode), any(), eqTo(Some(false)))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in new SetUp(Some(emptyUserAnswers)) {
      running(application) {
        val request =
          FakeRequest(POST, spiritTypeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[SpiritTypeView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in new SetUp(None) {
      running(application) {
        val request = FakeRequest(GET, spiritTypeRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in new SetUp(None) {
      running(application) {
        val request =
          FakeRequest(POST, spiritTypeRoute)
            .withFormUrlEncodedBody(("value[0]", SpiritType.values.head.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  class SetUp(maybeUserAnswers: Option[UserAnswers]) {
    val application: Application = applicationBuilder(userAnswers = maybeUserAnswers).build()

    def onwardRoute = Call("GET", "/foo")

    lazy val spiritTypeRoute = routes.SpiritTypeController.onPageLoad(NormalMode).url

    val formProvider = new SpiritTypeFormProvider()
    val form         = formProvider()
  }
}
