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
import play.api.test.Helpers._
import views.html.auth.NoAppaIdView

class NoAppaIdControllerSpec extends SpecBase {

  "NoAppaId Controller" - {

    lazy val noAppaIdRoute = routes.NoAppaIdController.onPageLoad().url

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, noAppaIdRoute)

        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val view      = application.injector.instanceOf[NoAppaIdView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(appConfig)(request, messages(application)).toString
      }
    }

    "must redirect to the business tax account when the button is clicked" in {
      val application = applicationBuilder().build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val request = FakeRequest(POST, noAppaIdRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual appConfig.businessTaxAccount
      }
    }
  }
}