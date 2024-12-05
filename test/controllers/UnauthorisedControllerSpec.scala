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

package controllers

import base.SpecBase
import controllers.actions.CheckAffinityGroupIsOrganisationActionImpl
import models.requests.IsOrganisationRequest
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import views.html.UnauthorisedView

import scala.concurrent.Future

class UnauthorisedControllerSpec extends SpecBase {

  val mockCheckAffinityGroupIsOrganisationActionImpl = mock[CheckAffinityGroupIsOrganisationActionImpl]

  def successfulFutureCodeBlock: (Request[_], IsOrganisationRequest[_]) => Future[Result] = (_, _) => {
    Future.successful(Ok("Test message"))
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCheckAffinityGroupIsOrganisationActionImpl)
  }

  "Unauthorised Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.UnauthorisedController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnauthorisedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(isOrganisation = true)(request, getMessages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when user is not an organisation" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isOrganisation = false)
        .build()

      running(application) {

        val request = FakeRequest(GET, routes.UnauthorisedController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnauthorisedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(isOrganisation = false)(request, getMessages(application)).toString
      }
    }
  }
}
