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
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.requests.{DataRequest, IdentifierRequest}
import play.api.http.Status.SEE_OTHER
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION

import scala.concurrent.Future

class CheckRegimeActionSpec extends SpecBase {
  trait TestHarness {
    def actionRefine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]]
  }

  class BeerHarness extends CheckBeerRegimeAction with TestHarness {
    def actionRefine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  class CiderHarness extends CheckCiderRegimeAction with TestHarness {
    def actionRefine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  class WineHarness extends CheckWineRegimeAction with TestHarness {
    def actionRefine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  class SpiritsHarness extends CheckSpiritsRegimeAction with TestHarness {
    def actionRefine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  class OtherFermentedHarness extends CheckOtherFermentedRegimeAction with TestHarness {
    def actionRefine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type]             = FakeRequest()
  val identifierRequest: IdentifierRequest[AnyContentAsEmpty.type] =
    IdentifierRequest(fakeRequest, appaId, groupId, internalId)

  val returnPeriod                        = returnPeriodGen.sample.get
  val userAnswers                         = emptyUserAnswers
  val userAnswersWithBeerRegime           = emptyUserAnswers.copy(data = JsObject(Seq("alcoholRegime" -> Json.toJson(Set(Beer)))))
  val userAnswersWithCiderRegime          =
    emptyUserAnswers.copy(data = JsObject(Seq("alcoholRegime" -> Json.toJson(Set(Cider)))))
  val userAnswersWithWineRegime           = emptyUserAnswers.copy(data = JsObject(Seq("alcoholRegime" -> Json.toJson(Set(Wine)))))
  val userAnswersWithSpiritsRegime        =
    emptyUserAnswers.copy(data = JsObject(Seq("alcoholRegime" -> Json.toJson(Set(Spirits)))))
  val userAnswersWithOtherFermentedRegime =
    emptyUserAnswers.copy(data = JsObject(Seq("alcoholRegime" -> Json.toJson(Set(OtherFermentedProduct)))))

  Seq(
    ("CheckBeerRegime", new BeerHarness, userAnswersWithBeerRegime, userAnswersWithSpiritsRegime),
    ("CheckCiderRegime", new CiderHarness, userAnswersWithCiderRegime, userAnswersWithSpiritsRegime),
    ("CheckWineRegime", new WineHarness, userAnswersWithWineRegime, userAnswersWithSpiritsRegime),
    ("CheckSpiritsRegime", new SpiritsHarness, userAnswersWithSpiritsRegime, userAnswersWithBeerRegime),
    (
      "CheckOtherFermentedRegime (when Cider)",
      new OtherFermentedHarness,
      userAnswersWithCiderRegime,
      userAnswersWithSpiritsRegime
    ),
    (
      "CheckOtherFermentedRegime (when Wine)",
      new OtherFermentedHarness,
      userAnswersWithWineRegime,
      userAnswersWithSpiritsRegime
    ),
    (
      "CheckOtherFermentedRegime",
      new OtherFermentedHarness,
      userAnswersWithOtherFermentedRegime,
      userAnswersWithSpiritsRegime
    )
  ).foreach { case (description, harness, userAnswersWithRegime, userAnswersWithoutRegime) =>
    description - {
      "should redirect to Journey Recovery when no regimes could be obtained from User Answers" in {
        val result =
          harness
            .actionRefine(DataRequest(identifierRequest, appaId, groupId, internalId, returnPeriod, emptyUserAnswers))
            .futureValue
            .left
            .getOrElse(
              fail()
            )
            .header

        result.status mustEqual SEE_OTHER
        result.headers.get(LOCATION) mustEqual Some(routes.JourneyRecoveryController.onPageLoad().url)
      }

      "should redirect to Unauthorised when the required regime is not in userAnswers" in {
        val result =
          harness
            .actionRefine(
              DataRequest(identifierRequest, appaId, groupId, internalId, returnPeriod, userAnswersWithoutRegime)
            )
            .futureValue
            .left
            .getOrElse(
              fail()
            )
            .header

        result.status mustEqual SEE_OTHER
        result.headers.get(LOCATION) mustEqual Some(routes.UnauthorisedController.onPageLoad.url)
      }

      "should return a DataRequest if the required regime is in UserAnswers" in {
        val dataRequest =
          DataRequest(identifierRequest, appaId, groupId, internalId, returnPeriod, userAnswersWithRegime)

        val result =
          harness
            .actionRefine(dataRequest)
            .futureValue
            .toOption
            .get

        result mustBe dataRequest
      }
    }
  }
}
