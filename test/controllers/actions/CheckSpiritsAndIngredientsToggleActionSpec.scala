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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import controllers.routes
import models.requests.{DataRequest, IdentifierRequest}
import play.api.Application
import play.api.http.Status.SEE_OTHER
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers.LOCATION

import scala.concurrent.Future

class CheckSpiritsAndIngredientsToggleActionSpec extends SpecBase {
  "CheckSpiritsAndIngredientsToggleAction" - {
    "should redirect to Journey Recovery when the toggle is off" in new SetUp(false) {
      val result =
        harness
          .actionRefine(
            DataRequest(identifierRequest, appaId, groupId, internalId, returnPeriod, userAnswersWithAllRegimes)
          )
          .futureValue
          .left
          .getOrElse(
            fail()
          )
          .header

      result.status mustEqual SEE_OTHER
      result.headers.get(LOCATION) mustEqual Some(routes.JourneyRecoveryController.onPageLoad().url)
    }

    "should return a DataRequest when the toggle is on" in new SetUp(true) {

      val dataRequest =
        DataRequest(identifierRequest, appaId, groupId, internalId, returnPeriod, userAnswersWithAllRegimes)

      val result =
        harness
          .actionRefine(dataRequest)
          .futureValue
          .toOption
          .get

      result mustBe dataRequest
    }
  }

  class SetUp(spiritsAndIngredientsEnabledFeatureToggle: Boolean) {
    val additionalConfig             = Map("features.spirits-and-ingredients" -> spiritsAndIngredientsEnabledFeatureToggle)
    val application: Application     = applicationBuilder().configure(additionalConfig).build()
    val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

    class Harness extends CheckSpiritsAndIngredientsToggleAction(appConfig) {
      def actionRefine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
    }

    val harness = new Harness

    val identifierRequest: IdentifierRequest[AnyContentAsEmpty.type] =
      IdentifierRequest(FakeRequest(), appaId, groupId, internalId)
  }
}
