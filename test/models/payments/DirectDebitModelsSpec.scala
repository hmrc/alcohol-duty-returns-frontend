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

package models.payments

import base.SpecBase
import generators.ModelGenerators
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class DirectDebitModelsSpec extends SpecBase with Matchers with ModelGenerators {
  val startDirectDebitRequest =
    StartDirectDebitRequest("/return/url", "/back/url")

  val startDirectDebitRequestJson = Json.obj(
    "returnUrl" -> "/return/url",
    "backUrl"   -> "/back/url"
  )

  ".formats writes" - {

    "must generate a json representation, including a numeric 'amountInPence' value" in {
      Json.toJson(startDirectDebitRequest) mustBe startDirectDebitRequestJson
    }
  }

  ".formats reads" - {

    "must return a new PaymentStart instance" - {

      "when all fields are present & correct" in {
        startDirectDebitRequestJson.as[StartDirectDebitRequest] mustBe startDirectDebitRequest
      }
    }

  }
}
