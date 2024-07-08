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
import models.requests.DataRequest
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Result
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

  Seq(
    ("CheckBeerRegime", new BeerHarness, userAnswersWithBeer, userAnswersWithoutBeer),
    ("CheckCiderRegime", new CiderHarness, userAnswersWithCider, userAnswersWithoutCider),
    ("CheckWineRegime", new WineHarness, userAnswersWithWine, userAnswersWithoutWine),
    ("CheckSpiritsRegime", new SpiritsHarness, userAnswersWithSpirits, userAnswersWithoutSpirits),
    (
      "CheckOtherFermentedRegime (when Cider)",
      new OtherFermentedHarness,
      userAnswersWithCider,
      userAnswersWithSpirits
    ),
    (
      "CheckOtherFermentedRegime (when Wine)",
      new OtherFermentedHarness,
      userAnswersWithWine,
      userAnswersWithSpirits
    ),
    (
      "CheckOtherFermentedRegime",
      new OtherFermentedHarness,
      userAnswersWithOtherFermentedProduct,
      userAnswersWithSpirits
    )
  ).foreach { case (description, harness, userAnswersWithRegime, userAnswersWithoutRegime) =>
    description - {
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
