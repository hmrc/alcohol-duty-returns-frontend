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
import connectors.PayApiConnector
import models.payments.{StartPaymentRequest, StartPaymentResponse}
import play.api.Application
import play.api.http.Status.{BAD_GATEWAY, BAD_REQUEST, CREATED, OK}
import play.api.libs.json.Json
import org.scalatest.RecoverMethods.recoverToExceptionIf


class PayApiConnectorISpec extends ISpecBase with WireMockHelper {
  override def fakeApplication(): Application = applicationBuilder(None).configure("microservice.services.pay-api.port" -> server.port()).build()

  "startPayment" - {

    "successfully retrieve a start payment response" in new SetUp{
      val startPaymentResponse = StartPaymentResponse("journey-id", "/next-url")
      val jsonResponse = Json.toJson(startPaymentResponse).toString()
      server.stubFor(
        post(urlMatching(url))
          .withRequestBody(equalToJson(Json.stringify(Json.toJson(startPaymentRequest))))
          .willReturn(aResponse().withBody(jsonResponse).withStatus(CREATED))
      )

      whenReady(connector.startPayment(startPaymentRequest).value) { result =>
        result mustBe Right(startPaymentResponse)
      }
    }

    "fail when an invalid JSON format is returned" in new SetUp {
      val invalidJsonResponse = """{ "invalid": "json" }"""
      server.stubFor(
        post(urlMatching(url))
          .withRequestBody(equalToJson(Json.stringify(Json.toJson(startPaymentRequest))))
          .willReturn(aResponse().withBody(invalidJsonResponse).withStatus(OK))
      )
      recoverToExceptionIf[Exception] {
        connector.startPayment(startPaymentRequest).value
      } map { ex =>
        ex.getMessage must include("Invalid JSON format")
      }
    }

    "fail when Start Payment returns an error" in new SetUp {
      server.stubFor(
        post(urlMatching(url))
          .withRequestBody(equalToJson(Json.stringify(Json.toJson(startPaymentRequest))))
          .willReturn(aResponse().withBody("upstreamErrorResponse").withStatus(BAD_GATEWAY))
      )

      recoverToExceptionIf[Exception] {
        connector.startPayment(startPaymentRequest).value
      } map { ex =>
        ex.getMessage must include("Start Payment failed")
      }
    }

    "fail when an unexpected status code is returned" in new SetUp {
      server.stubFor(
          post(urlMatching(url))
            .withRequestBody(equalToJson(Json.stringify(Json.toJson(startPaymentRequest))))
            .willReturn(aResponse().withBody("invalidStatusCodeResponse").withStatus(BAD_REQUEST))
        )
      recoverToExceptionIf[Exception] {
        connector.startPayment(startPaymentRequest).value
      } map { ex =>
        ex.getMessage must include("Unexpected status code: 400")
      }
    }

  }
  class SetUp {
    val url = "/pay-api/alcohol-duty/journey/start"
    val connector = app.injector.instanceOf[PayApiConnector]
    val startPaymentRequest =
      StartPaymentRequest("referenceNumber", BigInt(1000), "chargeReferenceNumber", "/return/url", "/back/url")
  }
}
