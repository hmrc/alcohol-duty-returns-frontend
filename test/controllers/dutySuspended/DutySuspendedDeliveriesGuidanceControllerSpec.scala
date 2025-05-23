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

package controllers.dutySuspended

import base.SpecBase
import play.api.test.Helpers._
import views.html.dutySuspended.DutySuspendedDeliveriesGuidanceView

class DutySuspendedDeliveriesGuidanceControllerSpec extends SpecBase {

  override def configOverrides: Map[String, Any] = Map(
    "features.duty-suspended-new-journey" -> false
  )

  "DutySuspendedDeliveriesGuidance Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.DutySuspendedDeliveriesGuidanceController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DutySuspendedDeliveriesGuidanceView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view()(request, getMessages(application)).toString
      }
    }

    "must return SEE_OTHER for a POST" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.DutySuspendedDeliveriesGuidanceController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }
  }
}
