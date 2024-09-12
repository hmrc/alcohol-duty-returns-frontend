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
import connectors.CacheConnector
import forms.spiritsQuestions.AlcoholUsedFormProvider
import models.{NormalMode, UserAnswers}
import models.spiritsQuestions.AlcoholUsed
import navigation.{FakeQuarterlySpiritsQuestionsNavigator, QuarterlySpiritsQuestionsNavigator}
import org.mockito.ArgumentMatchers.any
import pages.spiritsQuestions.AlcoholUsedPage
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.spiritsQuestions.AlcoholUsedView

import scala.concurrent.Future

class AlcoholUsedControllerSpec extends SpecBase {
  val validBeer         = 55.6
  val validWine         = 47.5
  val validCiderOrPerry = 55.6
  val validMadeWine     = 47.5

  val userAnswers = emptyUserAnswers.copy(data =
    Json.obj(
      AlcoholUsedPage.toString -> Json.obj(
        "beer"         -> validBeer,
        "wine"         -> validWine,
        "madeWine"     -> validMadeWine,
        "ciderOrPerry" -> validCiderOrPerry
      )
    )
  )

  "AlcoholUsed Controller" - {
    "must return OK and the correct view for a GET" in new SetUp(Some(emptyUserAnswers)) {
      running(application) {
        val request = FakeRequest(GET, alcoholUsedRoute)

        val view = application.injector.instanceOf[AlcoholUsedView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in new SetUp(
      Some(userAnswers)
    ) {
      running(application) {
        val request = FakeRequest(GET, alcoholUsedRoute)

        val view = application.injector.instanceOf[AlcoholUsedView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(AlcoholUsed(validBeer, validWine, validMadeWine, validCiderOrPerry)),
          NormalMode
        )(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in new SetUp(Some(emptyUserAnswers)) {
      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      override val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .configure(additionalConfig)
          .overrides(
            bind[QuarterlySpiritsQuestionsNavigator]
              .toInstance(new FakeQuarterlySpiritsQuestionsNavigator(onwardRoute, hasValueChanged = true)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, alcoholUsedRoute)
            .withFormUrlEncodedBody(
              ("beer", validBeer.toString),
              ("wine", validWine.toString),
              ("madeWine", validMadeWine.toString),
              ("ciderOrPerry", validCiderOrPerry.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in new SetUp(Some(emptyUserAnswers)) {
      running(application) {
        val request =
          FakeRequest(POST, alcoholUsedRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AlcoholUsedView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if the feature toggle is off" in new SetUp(Some(userAnswers), false) {
      running(application) {
        val request = FakeRequest(GET, alcoholUsedRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in new SetUp(None) {
      running(application) {
        val request = FakeRequest(GET, alcoholUsedRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if the feature toggle is off" in new SetUp(Some(userAnswers), false) {
      running(application) {
        val request =
          FakeRequest(POST, alcoholUsedRoute)
            .withFormUrlEncodedBody(
              ("beer", validBeer.toString),
              ("wine", validWine.toString),
              ("madeWine", validMadeWine.toString),
              ("ciderOrPerry", validCiderOrPerry.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in new SetUp(None) {
      running(application) {
        val request =
          FakeRequest(POST, alcoholUsedRoute)
            .withFormUrlEncodedBody(
              ("beer", validBeer.toString),
              ("wine", validWine.toString),
              ("madeWine", validMadeWine.toString),
              ("ciderOrPerry", validCiderOrPerry.toString)
            )

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

    def onwardRoute = Call("GET", "/foo")

    val formProvider = new AlcoholUsedFormProvider()
    val form         = formProvider()

    lazy val alcoholUsedRoute = routes.AlcoholUsedController.onPageLoad(NormalMode).url
  }
}
