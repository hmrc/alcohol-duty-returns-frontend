/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.checkAndSubmit

import base.SpecBase
import config.Constants.{noDetailsValue, returnCreatedDetailsKey}
import config.FrontendAppConfig
import play.api.test.Helpers._
import views.html.checkAndSubmit.ReturnSubmittedNoDetailsView

class ReturnSubmittedNoDetailsControllerSpec extends SpecBase {

  override lazy val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  "ReturnSubmittedNoDetailsController" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.checkAndSubmit.routes.ReturnSubmittedNoDetailsController.onPageLoad().url
        ).withSession(returnCreatedDetailsKey -> noDetailsValue)

        val result  = route(application, request).value

        val view = application.injector.instanceOf[ReturnSubmittedNoDetailsView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(appConfig.businessTaxAccountUrl)(
          request,
          getMessages(application)
        ).toString
      }
    }
  }

  "must redirect to the Journey Recovery screen if the return created details key in session is empty" in {
    val application = applicationBuilder().build()

    running(application) {
      val request =
        FakeRequestWithoutSession(
          GET,
          controllers.checkAndSubmit.routes.ReturnSubmittedNoDetailsController.onPageLoad().url
        )

      val result = route(application, request).value

      status(result)                 mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }
  }
}
