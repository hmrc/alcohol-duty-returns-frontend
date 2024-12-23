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

package controllers.spiritsQuestions

import base.SpecBase
import connectors.UserAnswersConnector
import forms.spiritsQuestions.DeclareSpiritsTotalFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeQuarterlySpiritsQuestionsNavigator, QuarterlySpiritsQuestionsNavigator}
import org.mockito.ArgumentMatchers.any
import pages.spiritsQuestions.DeclareSpiritsTotalPage
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.spiritsQuestions.DeclareSpiritsTotalView

import scala.concurrent.Future

class DeclareSpiritsTotalControllerSpec extends SpecBase {
  val validAnswer = BigDecimal(10.23)

  val userAnswersPreviouslyAnswered =
    emptyUserAnswers.set(DeclareSpiritsTotalPage, validAnswer).success.value

  val spiritsGuidanceUrl: String = "https://www.gov.uk/hmrc-internal-manuals/spirits-production/spir4100"

  "DeclareSpiritsTotal Controller" - {
    "must return OK and the correct view for a GET" in new SetUp(Some(emptyUserAnswers)) {
      running(application) {
        val request = FakeRequest(GET, declareSpiritsTotalRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclareSpiritsTotalView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, spiritsGuidanceUrl)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in new SetUp(
      Some(userAnswersPreviouslyAnswered)
    ) {
      running(application) {
        val request = FakeRequest(GET, declareSpiritsTotalRoute)

        val view = application.injector.instanceOf[DeclareSpiritsTotalView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, spiritsGuidanceUrl)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in new SetUp(Some(emptyUserAnswers)) {
      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      override val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[QuarterlySpiritsQuestionsNavigator]
              .toInstance(new FakeQuarterlySpiritsQuestionsNavigator(onwardRoute, hasValueChanged = true)),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, declareSpiritsTotalRoute)
            .withFormUrlEncodedBody(("declare-spirits-total-input", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in new SetUp(Some(emptyUserAnswers)) {
      running(application) {
        val request =
          FakeRequest(POST, declareSpiritsTotalRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[DeclareSpiritsTotalView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, spiritsGuidanceUrl)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if the feature toggle is off" in new SetUp(
      Some(emptyUserAnswers),
      false
    ) {
      running(application) {
        val request = FakeRequest(GET, declareSpiritsTotalRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in new SetUp(None) {
      running(application) {
        val request = FakeRequest(GET, declareSpiritsTotalRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if the feature toggle is off" in new SetUp(
      Some(emptyUserAnswers),
      false
    ) {
      running(application) {
        val request =
          FakeRequest(POST, declareSpiritsTotalRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in new SetUp(None) {
      running(application) {
        val request =
          FakeRequest(POST, declareSpiritsTotalRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  class SetUp(maybeUserAnswers: Option[UserAnswers], spiritsAndIngredientsEnabledFeatureToggle: Boolean = true) {
    val additionalConfig         = Map("features.spirits-and-ingredients" -> spiritsAndIngredientsEnabledFeatureToggle)
    val application: Application =
      applicationBuilder(userAnswers = maybeUserAnswers).configure(additionalConfig).build()

    val formProvider = new DeclareSpiritsTotalFormProvider()
    val form         = formProvider()

    def onwardRoute = Call("GET", "/foo")

    lazy val declareSpiritsTotalRoute =
      routes.DeclareSpiritsTotalController.onPageLoad(NormalMode).url
  }
}
