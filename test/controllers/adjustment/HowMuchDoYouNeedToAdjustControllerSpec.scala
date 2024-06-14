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
import forms.adjustment.HowMuchDoYouNeedToAdjustFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{AdjustmentNavigator, FakeAdjustmentNavigator}
import org.mockito.ArgumentMatchers.any
import pages.adjustment.HowMuchDoYouNeedToAdjustPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.CacheConnector
import models.adjustment.AdjustmentType.Spoilt
import models.adjustment.HowMuchDoYouNeedToAdjust
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.HowMuchDoYouNeedToAdjustView

import scala.concurrent.Future

class HowMuchDoYouNeedToAdjustControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new HowMuchDoYouNeedToAdjustFormProvider()
  val form         = formProvider()

  lazy val howMuchDoYouNeedToAdjustRoute = routes.HowMuchDoYouNeedToAdjustController.onPageLoad(NormalMode).url

  val spoilt = Spoilt.toString

  val userAnswers = UserAnswers(
    returnId,
    groupId,
    internalId,
    Json.obj(
      HowMuchDoYouNeedToAdjustPage.toString -> Json.obj(
        "totalLitersVolume" -> "value 1",
        "pureAlcoholVolume" -> "value 2"
      )
    )
  )

  "HowMuchDoYouNeedToAdjust Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, howMuchDoYouNeedToAdjustRoute)

        val view = application.injector.instanceOf[HowMuchDoYouNeedToAdjustView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, spoilt)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, howMuchDoYouNeedToAdjustRoute)

        val view = application.injector.instanceOf[HowMuchDoYouNeedToAdjustView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(HowMuchDoYouNeedToAdjust(123, 12, Some(123))),
          NormalMode,
          spoilt
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = true)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, howMuchDoYouNeedToAdjustRoute)
            .withFormUrlEncodedBody(("totalLitersVolume", "value 1"), ("pureAlcoholVolume", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, howMuchDoYouNeedToAdjustRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[HowMuchDoYouNeedToAdjustView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, spoilt)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, howMuchDoYouNeedToAdjustRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, howMuchDoYouNeedToAdjustRoute)
            .withFormUrlEncodedBody(("totalLitersVolume", "value 1"), ("pureAlcoholVolume", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
