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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlMatching}
import connectors.AlcoholDutyAccountConnector
import play.api.Application
import play.api.http.Status.{BAD_GATEWAY, CREATED, OK}
import play.api.libs.json.Json

class AlcoholDutyAccountConnectorISpec extends ISpecBase with WireMockHelper{
  override def fakeApplication(): Application = applicationBuilder(None).configure("microservice.services.alcohol-duty-account.port" -> server.port()).build()

  "AlcoholDutyAccountConnector" - {
    "Open Payments" - {
      val url = s"/alcohol-duty-account/producers/$appaId/payments/open"

      "should successfully retrieve open payment details" in new SetUp {
        val jsonResponse         = Json.toJson(openPaymentsData).toString()

        server.stubFor(get(urlMatching(url))
          .willReturn(aResponse()
            .withStatus(OK)
            .withBody(jsonResponse)))

        whenReady(connector.outstandingPayments(appaId)) { result =>
          result mustBe openPaymentsData
        }
      }

      "should fail when invalid JSON is returned" in new SetUp {
        val invalidJsonResponse = """{ "invalid": "json" }"""
        server.stubFor(get(urlMatching(url))
          .willReturn(aResponse()
            .withStatus(OK)
            .withBody(invalidJsonResponse)))

        whenReady(connector.outstandingPayments(appaId).failed) { e =>
          e.getMessage must include("Invalid JSON format")
        }
      }

      "should fail when an unexpected response is returned" in new SetUp {
        server.stubFor(get(urlMatching(url))
          .willReturn(aResponse()
            .withStatus(BAD_GATEWAY)))

        whenReady(connector.outstandingPayments(appaId).failed) { e =>
          e.getMessage must include("Unexpected response")
        }
      }

      "should fail when an unexpected  status code is returned" in new SetUp {
        server.stubFor(get(urlMatching(url))
          .willReturn(aResponse()
            .withStatus(CREATED)))

        whenReady(connector.outstandingPayments(appaId).failed) { e =>
          e.getMessage must include("Unexpected status code: 201")
        }
      }
    }

    "historicPayments" - {
      val year    = 2024
      val url = s"/alcohol-duty-account/producers/$appaId/payments/historic/$year"

      "should successfully retrieve historic payment details" in new SetUp {
        val jsonResponse = Json.toJson(historicPayments).toString()

        server.stubFor(get(urlMatching(url))
          .willReturn(aResponse()
            .withStatus(OK)
            .withBody(jsonResponse)))

        whenReady(connector.historicPayments(appaId, year)) { result =>
          result mustBe historicPayments
        }
      }

      "should fail when invalid JSON is returned" in new SetUp {
        val invalidJsonResponse = """{ "invalid": "json" }"""
        server.stubFor(get(urlMatching(url))
          .willReturn(aResponse()
            .withStatus(OK)
            .withBody(invalidJsonResponse)))

        whenReady(connector.historicPayments(appaId, year).failed) { e =>
          e.getMessage must include("Invalid JSON format")
        }
      }

      "should fail when an unexpected response is returned" in new SetUp {
        server.stubFor(get(urlMatching(url))
          .willReturn(aResponse()
            .withStatus(BAD_GATEWAY)))

        whenReady(connector.historicPayments(appaId, year).failed) { e =>
          e.getMessage must include("Unexpected response")
        }
      }

      "should fail when an unexpected  status code is returned" in new SetUp {
        server.stubFor(get(urlMatching(url))
          .willReturn(aResponse()
            .withStatus(CREATED)))

        whenReady(connector.historicPayments(appaId, year).failed) { e =>
          e.getMessage must include("Unexpected status code: 201")
        }
      }
    }
  }

  class SetUp {
    val connector = app.injector.instanceOf[AlcoholDutyAccountConnector]
  }
}
