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

package controllers.declareDuty

import base.SpecBase
import play.api.test.Helpers._
import views.html.declareDuty.DeclaringWineDutyGuidanceView

class DeclaringWineDutyGuidanceControllerSpec extends SpecBase {

  lazy val declaringWineDutyGuidanceRoute = routes.DeclaringWineDutyGuidanceController.onPageLoad().url

  "DeclaringWineDutyGuidance Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithWine)).build()

      running(application) {
        val request = FakeRequest(GET, declaringWineDutyGuidanceRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclaringWineDutyGuidanceView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(appConfig.alcoholicStrengthGuidanceUrl)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, declaringWineDutyGuidanceRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
