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
import controllers.routes
import models.requests.{DataRequest, OptionalDataRequest}
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Result
import play.api.test.Helpers.LOCATION

import scala.concurrent.Future

class DataRequiredActionSpec extends SpecBase {

  class Harness extends DataRequiredActionImpl {
    def actionRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  val userAnswers = emptyUserAnswers

  "Data Required Action" - {
    "should redirect to the Journey Recovery when User Answers or Return Period are None" in {

      val harness = new Harness

      val result =
        harness
          .actionRefine(OptionalDataRequest(identifierRequest, appaId, groupId, internalId, None, None))
          .futureValue
          .left
          .getOrElse(
            fail()
          )
          .header

      result.status mustEqual SEE_OTHER
      result.headers.get(LOCATION) mustEqual Some(routes.JourneyRecoveryController.onPageLoad().url)
    }

    "should redirect to the Journey Recovery when User Answers is None" in {

      val harness = new Harness

      val result =
        harness
          .actionRefine(
            OptionalDataRequest(identifierRequest, appaId, groupId, internalId, Some(returnPeriod), None)
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

    "redirect to the Journey Recovery when the Return Period is None" in {

      val harness = new Harness

      val result =
        harness
          .actionRefine(OptionalDataRequest(identifierRequest, appaId, groupId, internalId, None, Some(userAnswers)))
          .futureValue
          .left
          .getOrElse(
            fail()
          )
          .header

      result.status mustEqual SEE_OTHER
      result.headers.get(LOCATION) mustEqual Some(routes.JourneyRecoveryController.onPageLoad().url)
    }

    "should return a DataRequest with the correct values" in {

      val harness = new Harness

      val optionalDataRequest =
        OptionalDataRequest(identifierRequest, appaId, groupId, internalId, Some(returnPeriod), Some(userAnswers))

      val result =
        harness
          .actionRefine(optionalDataRequest)
          .futureValue

      result.isRight mustBe true
      result.map { dataRequest =>
        dataRequest.request mustEqual identifierRequest
        dataRequest.userAnswers mustEqual userAnswers
        dataRequest.appaId mustEqual appaId
        dataRequest.groupId mustEqual groupId
        dataRequest.userId mustEqual internalId
        dataRequest.returnPeriod mustEqual returnPeriod
      }
    }
  }

}
