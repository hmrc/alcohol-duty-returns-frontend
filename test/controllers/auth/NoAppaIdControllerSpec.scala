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

    def noAppaIdRouteUrl(fromBTA: Boolean): String = routes.NoAppaIdController.onPageLoad(fromBTA).url

    Seq(true, false).foreach { fromBTA =>
      s"must return OK and the correct view for a GET when${if (fromBTA) " not" else ""} from BTA" in {
        val application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, noAppaIdRouteUrl(fromBTA))

          val view      = application.injector.instanceOf[NoAppaIdView]
          val result    = route(application, request).value

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(fromBTA, true)(request, getMessages(application)).toString
        }
      }
    }

    Seq(true, false).foreach { signedIn =>
      s"must return OK and the correct view for a GET when${if (signedIn) " not" else ""} signed in" in {
        val application = applicationBuilder(signedIn = signedIn).build()

        running(application) {
          val request = FakeRequest(GET, noAppaIdRouteUrl(true))

          val view      = application.injector.instanceOf[NoAppaIdView]
          val result    = route(application, request).value

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(true, signedIn)(request, getMessages(application)).toString
        }
      }
    }

    Seq(true, false).foreach { fromBTA =>
      val redirect = if (fromBTA) "business tax account" else "journey recovery page"
      s"must redirect to the $redirect when the button is clicked" in {
        val application = applicationBuilder().build()

        running(application) {
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val request = FakeRequest(POST, noAppaIdRouteUrl(fromBTA))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          if (fromBTA) {
            redirectLocation(result).value mustEqual appConfig.businessTaxAccountUrl
          } else {
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url
          }
        }
      }
    }
  }
}
