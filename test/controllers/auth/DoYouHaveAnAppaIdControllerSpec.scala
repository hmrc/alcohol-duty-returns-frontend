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

package controllers.auth

import base.SpecBase
import config.FrontendAppConfig
import forms.auth.DoYouHaveAnAppaIdFormProvider
import play.api.test.Helpers._
import views.html.auth.DoYouHaveAnAppaIdView

class DoYouHaveAnAppaIdControllerSpec extends SpecBase {
  val formProvider = new DoYouHaveAnAppaIdFormProvider()
  val form         = formProvider()

  lazy val doYouHaveAnAppaIdRoute = controllers.auth.routes.DoYouHaveAnAppaIdController.onPageLoad().url

  "DoYouHaveAnAppaId Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, doYouHaveAnAppaIdRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DoYouHaveAnAppaIdView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must redirect to the request access page when yes is selected" in {
      val application = applicationBuilder().build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   =
          FakeRequest(POST, doYouHaveAnAppaIdRoute)
            .withFormUrlEncodedBody(("doYouHaveAnAppaId-yesNoValue", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual appConfig.requestAccessUrl
      }
    }

    "must redirect to the no AppaId page when no is selected" in {
      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, doYouHaveAnAppaIdRoute)
            .withFormUrlEncodedBody(("doYouHaveAnAppaId-yesNoValue", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.auth.routes.NoAppaIdController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when no data is submitted" in {
      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, doYouHaveAnAppaIdRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DoYouHaveAnAppaIdView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}
