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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, post, urlMatching}
import connectors.DirectDebitConnector
import models.payments.{StartDirectDebitRequest, StartDirectDebitResponse}
import org.scalatest.RecoverMethods.recoverToExceptionIf
import play.api.Application
import play.api.http.Status.{BAD_GATEWAY, BAD_REQUEST, CREATED, OK}
import play.api.libs.json.Json

class DirectDebitConnectorISpec extends ISpecBase with WireMockHelper {
  override def fakeApplication(): Application =
    applicationBuilder(None).configure("microservice.services.direct-debit.port" -> server.port()).build()

  "DirectDebitConnector" - {
    "successfully retrieve a start direct debit response" in new SetUp {
      val startDirectDebitResponse = StartDirectDebitResponse("/next-url")
      val jsonResponse             = Json.toJson(startDirectDebitResponse).toString()

      server.stubFor(
        post(urlMatching(url))
          .withRequestBody(equalToJson(Json.stringify(Json.toJson(startDirectDebitRequest))))
          .willReturn(aResponse().withBody(jsonResponse).withStatus(CREATED))
      )

      whenReady(connector.startDirectDebit(startDirectDebitRequest).value) { result =>
        result mustBe Right(startDirectDebitResponse)
      }
    }

    "return an error when the HttpResponse cannot be parsed as a StartPaymentResponse" in new SetUp {

      val jsonResponse = """{ "test": "not start payment response" }"""

      server.stubFor(
        post(urlMatching(url))
          .withRequestBody(equalToJson(Json.stringify(Json.toJson(startDirectDebitRequest))))
          .willReturn(aResponse().withBody(jsonResponse).withStatus(CREATED))
      )

      whenReady(connector.startDirectDebit(startDirectDebitRequest).value) { result =>
        val resultErrorString = result.swap.toOption.get
        resultErrorString.contains("Invalid JSON format when starting direct debit") mustBe true
      }
    }

    "fail when an invalid JSON format is returned" in new SetUp {
      val invalidJsonResponse = """{ "invalid": "json" }"""
      server.stubFor(
        post(urlMatching(url))
          .withRequestBody(equalToJson(Json.stringify(Json.toJson(startDirectDebitRequest))))
          .willReturn(aResponse().withBody(invalidJsonResponse).withStatus(OK))
      )

      recoverToExceptionIf[Exception] {
        connector.startDirectDebit(startDirectDebitRequest).value
      } map { ex =>
        ex.getMessage must include("Invalid JSON format")
      }
    }

    "fail when Start Direct Debit returns an error" in new SetUp {

      server.stubFor(
        post(urlMatching(url))
          .withRequestBody(equalToJson(Json.stringify(Json.toJson(startDirectDebitRequest))))
          .willReturn(aResponse().withBody("upstreamErrorResponse").withStatus(BAD_GATEWAY))
      )

      recoverToExceptionIf[Exception] {
        connector.startDirectDebit(startDirectDebitRequest).value
      } map { ex =>
        ex.getMessage must include("Start Direct Debit failed")
      }
    }

    "fail when an unexpected status code is returned" in new SetUp {
      server.stubFor(
        post(urlMatching(url))
          .withRequestBody(equalToJson(Json.stringify(Json.toJson(startDirectDebitRequest))))
          .willReturn(aResponse().withBody("invalidStatusCodeResponse").withStatus(BAD_REQUEST))
      )

      recoverToExceptionIf[Exception] {
        connector.startDirectDebit(startDirectDebitRequest).value
      } map { ex =>
        ex.getMessage mustBe "Unexpected status code received when starting direct debit: 400"
      }
    }
  }

  class SetUp {
    val url                     = "/direct-debit-backend/ad-confirmation/ad/journey/start"
    val connector               = app.injector.instanceOf[DirectDebitConnector]
    val startDirectDebitRequest = StartDirectDebitRequest("/return/url", "/back/url")
  }
}
