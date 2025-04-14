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
import play.api.inject.bind
import play.api.test.Helpers._
import views.html.NotOrganisationView
import controllers.actions.{CheckSignedInAction, FakeCheckSignedInAction, FakeIsSignedIn}
class NotOrganisationControllerSpec extends SpecBase {

  lazy val notOrganisationRoute: String = controllers.auth.routes.NotOrganisationController.onPageLoad.url

  val testCreateOrganisationAccountUrl: String = "https://www.gov.uk/log-in-register-hmrc-online-services/register"
  val testContinueUrl: String                  = "/manage-alcohol-duty/account/sign-out-log-in"

  val fakeIsSignedInTrue: FakeIsSignedIn  = FakeIsSignedIn(true)
  val fakeIsSignedInFalse: FakeIsSignedIn = FakeIsSignedIn(false)

  "DoYouHaveAnAppaId Controller" - {
    "must return OK and the correct view for a GET if signed in" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, notOrganisationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NotOrganisationView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(testCreateOrganisationAccountUrl, testContinueUrl, signedIn = true)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if not signed in" in {
      val application = applicationBuilder(signedIn = false).build()

      running(application) {
        val request = FakeRequest(GET, notOrganisationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NotOrganisationView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(testCreateOrganisationAccountUrl, testContinueUrl, signedIn = false)(
          request,
          getMessages(application)
        ).toString
      }
    }
  }
}
