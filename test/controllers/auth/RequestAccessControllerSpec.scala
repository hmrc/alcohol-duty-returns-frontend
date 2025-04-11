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
import controllers.actions.{FakeIdentifyWithoutEnrolmentFailAction, IdentifyWithoutEnrolmentAction}
import play.api.test.Helpers._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

class RequestAccessControllerSpec extends SpecBase {

  "RequestAccessController Controller" - {

    "must return OK and requestAccessUrl for a GET when logged in" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, controllers.auth.routes.RequestAccessController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual appConfig.requestAccessUrl
      }
    }

    "must return OK and unauthorised for a GET when not logged in" in {
      val application =
        new GuiceApplicationBuilder()
          .overrides(
            bind[IdentifyWithoutEnrolmentAction].to[FakeIdentifyWithoutEnrolmentFailAction]
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.auth.routes.RequestAccessController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.auth.routes.UnauthorisedController.onPageLoad.url
      }
    }
  }
}
