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
import forms.spiritsQuestions.OtherMaltedGrainsFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import pages.spiritsQuestions.OtherMaltedGrainsPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.UserAnswersConnector
import models.spiritsQuestions.OtherMaltedGrains
import navigation.{FakeQuarterlySpiritsQuestionsNavigator, QuarterlySpiritsQuestionsNavigator}
import play.api.Application
import uk.gov.hmrc.http.HttpResponse
import views.html.spiritsQuestions.OtherMaltedGrainsView

import scala.concurrent.Future

class OtherMaltedGrainsControllerSpec extends SpecBase {
  val otherMaltedGrainsTypes    = "Coco Pops"
  val otherMaltedGrainsQuantity = BigDecimal(100000)

  val userAnswers = emptyUserAnswers.copy(data =
    Json.obj(
      OtherMaltedGrainsPage.toString -> Json.obj(
        "otherMaltedGrainsTypes"    -> otherMaltedGrainsTypes,
        "otherMaltedGrainsQuantity" -> otherMaltedGrainsQuantity
      )
    )
  )

  "OtherMaltedGrains Controller" - {
    "must return OK and the correct view for a GET" in new SetUp(Some(emptyUserAnswers)) {
      running(application) {
        val request = FakeRequest(GET, otherMaltedGrainsRoute)

        val view = application.injector.instanceOf[OtherMaltedGrainsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in new SetUp(
      Some(userAnswers)
    ) {
      running(application) {
        val request = FakeRequest(GET, otherMaltedGrainsRoute)

        val view = application.injector.instanceOf[OtherMaltedGrainsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(OtherMaltedGrains(otherMaltedGrainsTypes, otherMaltedGrainsQuantity)),
          NormalMode
        )(request, getMessages(application)).toString
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
          FakeRequest(POST, otherMaltedGrainsRoute)
            .withFormUrlEncodedBody(
              ("otherMaltedGrainsTypes", otherMaltedGrainsTypes),
              ("otherMaltedGrainsQuantity", otherMaltedGrainsQuantity.toString())
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in new SetUp(Some(emptyUserAnswers)) {
      running(application) {
        val request =
          FakeRequest(POST, otherMaltedGrainsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[OtherMaltedGrainsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if the feature toggle is off" in new SetUp(
      Some(emptyUserAnswers),
      false
    ) {
      running(application) {
        val request = FakeRequest(GET, otherMaltedGrainsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in new SetUp(None) {
      running(application) {
        val request = FakeRequest(GET, otherMaltedGrainsRoute)

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
          FakeRequest(POST, otherMaltedGrainsRoute)
            .withFormUrlEncodedBody(
              ("otherMaltedGrainsTypes", otherMaltedGrainsTypes),
              ("otherMaltedGrainsQuantity", otherMaltedGrainsQuantity.toString())
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in new SetUp(None) {
      running(application) {
        val request =
          FakeRequest(POST, otherMaltedGrainsRoute)
            .withFormUrlEncodedBody(
              ("otherMaltedGrainsTypes", otherMaltedGrainsTypes),
              ("otherMaltedGrainsQuantity", otherMaltedGrainsQuantity.toString())
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

    val formProvider = new OtherMaltedGrainsFormProvider()
    val form         = formProvider()

    lazy val otherMaltedGrainsRoute = routes.OtherMaltedGrainsController.onPageLoad(NormalMode).url
  }
}
