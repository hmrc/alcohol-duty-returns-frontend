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
import forms.spiritsQuestions.DeclareQuarterlySpiritsFormProvider
import models.{AlcoholRegimes, NormalMode, ReturnId, UserAnswers}
import navigation.{FakeQuarterlySpiritsQuestionsNavigator, QuarterlySpiritsQuestionsNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.spiritsQuestions.{DeclareQuarterlySpiritsPage, DeclareSpiritsTotalPage, OtherSpiritsProducedPage, SpiritTypePage, WhiskyPage}
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.spiritsQuestions.DeclareQuarterlySpiritsView

import scala.concurrent.Future
import scala.util.Success

class DeclareQuarterlySpiritsControllerSpec extends SpecBase {
  val userAnswersPreviouslyAnswered = emptyUserAnswers.set(DeclareQuarterlySpiritsPage, true).success.value
  val mockUserAnswers: UserAnswers  = mock[UserAnswers]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswers)
  }

  "DeclareQuarterlySpirits Controller" - {
    "must return OK and the correct view for a GET" in new SetUp(Some(emptyUserAnswers), true) {
      running(application) {
        val request = FakeRequest(GET, declareQuarterlySpiritsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclareQuarterlySpiritsView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in new SetUp(
      Some(userAnswersPreviouslyAnswered),
      true
    ) {
      running(application) {
        val request = FakeRequest(GET, declareQuarterlySpiritsRoute)

        val view = application.injector.instanceOf[DeclareQuarterlySpiritsView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in new SetUp(Some(emptyUserAnswers), true) {
      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      override val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[QuarterlySpiritsQuestionsNavigator]
              .toInstance(new FakeQuarterlySpiritsQuestionsNavigator(onwardRoute, hasValueChanged = Some(true))),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, declareQuarterlySpiritsRoute)
            .withFormUrlEncodedBody(("declareQuarterlySpirits-yesNoValue", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the task list page and clear user answers when 'No' is submitted" in new SetUp(
      Some(mockUserAnswers),
      true
    ) {
      val pagesToDelete = List(DeclareSpiritsTotalPage, SpiritTypePage, OtherSpiritsProducedPage, WhiskyPage)

      val mockUserAnswersConnector: UserAnswersConnector = mock[UserAnswersConnector]
      val mockAlcoholRegimesSet: AlcoholRegimes          = mock[AlcoholRegimes]
      val mockReturnId                                   = mock[ReturnId]
      when(mockReturnId.periodKey) thenReturn "2025-01"
      when(mockUserAnswers.returnId) thenReturn mockReturnId
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(mockUserAnswers.set(eqTo(DeclareQuarterlySpiritsPage), eqTo(false))(any())) thenReturn Success(
        mockUserAnswers
      )
      when(mockUserAnswers.remove(eqTo(pagesToDelete))) thenReturn emptyUserAnswers.set(
        DeclareQuarterlySpiritsPage,
        false
      )
      when(mockUserAnswers.regimes) thenReturn mockAlcoholRegimesSet
      when(mockAlcoholRegimesSet.hasRegime(any())) thenReturn true

      override val application =
        applicationBuilder(userAnswers = Some(mockUserAnswers))
          .overrides(
            bind[QuarterlySpiritsQuestionsNavigator]
              .toInstance(
                new FakeQuarterlySpiritsQuestionsNavigator(
                  controllers.routes.TaskListController.onPageLoad,
                  hasValueChanged = Some(true)
                )
              ),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, declareQuarterlySpiritsRoute)
            .withFormUrlEncodedBody(("declareQuarterlySpirits-yesNoValue", "false"))

        val result = route(application, request).value

        status(result)           mustEqual SEE_OTHER
        redirectLocation(result) mustEqual Some(controllers.routes.TaskListController.onPageLoad.toString)

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockUserAnswers, times(1)).set(eqTo(DeclareQuarterlySpiritsPage), eqTo(false))(any())
        verify(mockUserAnswers, times(1)).remove(eqTo(pagesToDelete))
        verify(mockUserAnswers, times(1)).regimes
        verify(mockAlcoholRegimesSet, times(1)).hasRegime(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in new SetUp(Some(emptyUserAnswers), true) {
      running(application) {
        val request =
          FakeRequest(POST, declareQuarterlySpiritsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DeclareQuarterlySpiritsView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if the feature toggle is off" in new SetUp(
      Some(emptyUserAnswers),
      false
    ) {
      running(application) {
        val request = FakeRequest(GET, declareQuarterlySpiritsRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in new SetUp(None, true) {
      running(application) {
        val request = FakeRequest(GET, declareQuarterlySpiritsRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if the feature toggle is off" in new SetUp(
      Some(emptyUserAnswers),
      false
    ) {
      running(application) {
        val request =
          FakeRequest(POST, declareQuarterlySpiritsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in new SetUp(None, true) {
      running(application) {
        val request =
          FakeRequest(POST, declareQuarterlySpiritsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  class SetUp(maybeUserAnswers: Option[UserAnswers], spiritsAndIngredientsEnabledFeatureToggle: Boolean = true) {
    val additionalConfig         = Map("features.spirits-and-ingredients" -> spiritsAndIngredientsEnabledFeatureToggle)
    val application: Application =
      applicationBuilder(userAnswers = maybeUserAnswers).configure(additionalConfig).build()

    def onwardRoute = Call("GET", "/foo")

    val formProvider = new DeclareQuarterlySpiritsFormProvider()
    val form         = formProvider()

    lazy val declareQuarterlySpiritsRoute =
      controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(NormalMode).url
  }
}
