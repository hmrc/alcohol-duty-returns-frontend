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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, get, post, urlMatching}
import connectors.AlcoholDutyReturnsConnector
import models.ErrorModel
import models.checkAndSubmit.AdrReturnCreatedDetails
import org.scalatest.RecoverMethods.recoverToExceptionIf
import play.api.Application
import play.api.http.Status._
import play.api.libs.json.Json

import java.time.{Instant, LocalDate}

class AlcoholDutyReturnsConnectorISpec extends ISpecBase with WireMockHelper {
  override def fakeApplication(): Application =
    applicationBuilder(None).configure("microservice.services.alcohol-duty-returns.port" -> server.port()).build()

  "AlcoholDutyReturnsConnector" - {
    "obligationDetails" - {
      "must successfully retrieve obligation details" in new SetUp {
        val obligationDataResponse = Seq(obligationDataSingleOpen)
        val jsonResponse           = Json.toJson(obligationDataResponse).toString()

        server.stubFor(
          get(urlMatching(obligationDeatilsUrl))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withBody(jsonResponse)
            )
        )

        whenReady(connector.obligationDetails(appaId)) { result =>
          result mustBe obligationDataResponse
        }
      }

      "must fail when invalid JSON is returned" in new SetUp {
        val invalidJsonResponse = """{ "invalid": "json" }"""
        server.stubFor(
          get(urlMatching(obligationDeatilsUrl))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withBody(invalidJsonResponse)
            )
        )

        whenReady(connector.obligationDetails(appaId).failed) { e =>
          e.getMessage must include("Invalid JSON format")
        }
      }

      "must fail when an unexpected response is returned" in new SetUp {
        server.stubFor(
          get(urlMatching(obligationDeatilsUrl))
            .willReturn(
              aResponse()
                .withStatus(BAD_GATEWAY)
            )
        )

        whenReady(connector.obligationDetails(appaId).failed) { e =>
          e.getMessage must include("Unexpected response")
        }
      }

      "must fail when an unexpected status code is returned" in new SetUp {
        server.stubFor(
          get(urlMatching(obligationDeatilsUrl))
            .willReturn(
              aResponse()
                .withStatus(CREATED)
            )
        )

        whenReady(connector.obligationDetails(appaId).failed) { e =>
          e.getMessage must include("Unexpected status code: 201")
        }
      }
    }

    "submitReturn" - {
      "must successfully submit a return" in new SetUp {
        val adrReturnCreatedDetails = AdrReturnCreatedDetails(
          processingDate = Instant.now(clock),
          amount = BigDecimal(1),
          chargeReference = Some("1234567890"),
          paymentDueDate = Some(LocalDate.now(clock))
        )
        val jsonResponse            = Json.toJson(adrReturnCreatedDetails).toString()

        server.stubFor(
          post(urlMatching(returnUrl))
            .willReturn(
              aResponse()
                .withStatus(CREATED)
                .withBody(jsonResponse)
            )
        )

        whenReady(connector.submitReturn(fullUserAnswers, fullReturn).value) {
          case Right(details) =>
            details.processingDate  mustBe adrReturnCreatedDetails.processingDate
            details.amount          mustBe adrReturnCreatedDetails.amount
            details.chargeReference mustBe adrReturnCreatedDetails.chargeReference
            details.paymentDueDate  mustBe adrReturnCreatedDetails.paymentDueDate
          case _              => fail("Test failed: result did not match expected value")
        }
      }

      "submission must fail when JSON is parsed successfully, but an unexpected status code was returned" in new SetUp {
        val adrReturnCreatedDetails = AdrReturnCreatedDetails(
          processingDate = Instant.now(clock),
          amount = BigDecimal(1),
          chargeReference = Some("1234567890"),
          paymentDueDate = Some(LocalDate.now(clock))
        )
        val jsonResponse            = Json.toJson(adrReturnCreatedDetails).toString()

        val expectedErrorModel =
          ErrorModel(INTERNAL_SERVER_ERROR, s"Unable to submit return. Unexpected status code: $OK")

        server.stubFor(
          post(urlMatching(returnUrl))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withBody(jsonResponse)
            )
        )

        whenReady(connector.submitReturn(fullUserAnswers, fullReturn).value) {
          case Left(errorModel) =>
            errorModel mustBe expectedErrorModel
          case _                => fail("Test failed: result did not match expected value")
        }
      }

      "must fail when invalid JSON is returned" in new SetUp {
        val invalidJsonResponse = """{ "invalid": "json" }"""
        server.stubFor(
          post(urlMatching(returnUrl))
            .willReturn(
              aResponse()
                .withStatus(CREATED)
                .withBody(invalidJsonResponse)
            )
        )

        whenReady(connector.submitReturn(fullUserAnswers, nilReturn).value) { result =>
          result.swap.toOption.get.status mustBe INTERNAL_SERVER_ERROR
          result.swap.toOption.get.message  must include("Invalid JSON format")
        }
      }

      "must return an ErrorModel when a duplicate submission response is returned" in new SetUp {
        server.stubFor(
          post(urlMatching(returnUrl))
            .willReturn(
              aResponse()
                .withStatus(UNPROCESSABLE_ENTITY)
                .withBody("Return already submitted")
            )
        )

        whenReady(connector.submitReturn(fullUserAnswers, nilReturn).value) { result =>
          result.swap.toOption.get.status mustBe UNPROCESSABLE_ENTITY
          result.swap.toOption.get.message mustBe "Return already submitted"
        }
      }

      "must fail when submit return returns an error" in new SetUp {
        server.stubFor(
          post(urlMatching(returnUrl))
            .withRequestBody(equalToJson(Json.stringify(Json.toJson(nilReturn))))
            .willReturn(aResponse().withBody("upstreamErrorResponse").withStatus(BAD_GATEWAY))
        )

        recoverToExceptionIf[Exception] {
          connector.submitReturn(fullUserAnswers, nilReturn).value
        } map { ex =>
          ex.getMessage must include("Unexpected response")
        }
      }

      "must fail when an unexpected status code is returned" in new SetUp {
        server.stubFor(
          post(urlMatching(returnUrl))
            .withRequestBody(equalToJson(Json.stringify(Json.toJson(nilReturn))))
            .willReturn(aResponse().withBody("invalidStatusCodeResponse").withStatus(BAD_REQUEST))
        )
        recoverToExceptionIf[Exception] {
          connector.submitReturn(fullUserAnswers, nilReturn).value
        } map { ex =>
          ex.getMessage must include("Unexpected status code: 201")
        }
      }
    }

    "getReturn" - {
      "must successfully get a return" in new SetUp {
        val adrReturnDetails = exampleReturnDetails(fullUserAnswers.returnId.periodKey, Instant.now(clock))
        val jsonResponse     = Json.toJson(adrReturnDetails).toString()

        server.stubFor(
          get(urlMatching(returnUrl))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withBody(jsonResponse)
            )
        )

        whenReady(connector.getReturn(fullUserAnswers.returnId.appaId, fullUserAnswers.returnId.periodKey)) { result =>
          result mustBe adrReturnDetails
        }
      }

      "must fail when invalid JSON is returned" in new SetUp {
        server.stubFor(
          get(urlMatching(returnUrl))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withBody("""{ "invalid": "json" }""")
            )
        )

        whenReady(connector.getReturn(fullUserAnswers.returnId.appaId, fullUserAnswers.returnId.periodKey).failed) { e =>
          e.getMessage must include("Invalid JSON format")
        }
      }

      "must fail when an unexpected response is returned" in new SetUp {
        server.stubFor(
          get(urlMatching(returnUrl))
            .willReturn(
              aResponse()
                .withStatus(BAD_GATEWAY)
            )
        )

        whenReady(connector.getReturn(appaId, periodKey).failed) { e =>
          e.getMessage must include("Unexpected response")
        }
      }

      "must fail when an unexpected  status code is returned" in new SetUp {
        server.stubFor(
          get(urlMatching(returnUrl))
            .willReturn(
              aResponse()
                .withStatus(CREATED)
            )
        )

        whenReady(connector.getReturn(fullUserAnswers.returnId.appaId, fullUserAnswers.returnId.periodKey).failed) { e =>
          e.getMessage must include("Unexpected status code: 201")
        }
      }
    }
  }

  class SetUp {
    val connector            = app.injector.instanceOf[AlcoholDutyReturnsConnector]
    val obligationDeatilsUrl = s"/alcohol-duty-returns/obligationDetails/$appaId"
    val returnUrl      = s"/alcohol-duty-returns/producers/${fullUserAnswers.returnId.appaId}/returns/${fullUserAnswers.returnId.periodKey}"
  }
}
