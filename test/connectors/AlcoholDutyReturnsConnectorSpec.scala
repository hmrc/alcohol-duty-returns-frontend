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

package connectors

import base.SpecBase
import config.FrontendAppConfig
import models.AlcoholRegime.{Beer, Cider}
import models.checkAndSubmit.AdrReturnCreatedDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HttpResponse, StringContextOps, UpstreamErrorResponse}
import play.api.http.Status._
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import java.time.{Instant, LocalDate}
import scala.concurrent.Future

class AlcoholDutyReturnsConnectorSpec extends SpecBase with ScalaFutures {
  "obligationDetails" - {
    val mockUrl = s"http://alcohol-duty-returns/obligationDetails/$appaId"

    "successfully retrieve obligation details" in new SetUp {
      val obligationDataResponse = Seq(obligationDataSingleOpen)
      val jsonResponse           = Json.toJson(obligationDataResponse).toString()
      val httpResponse           = HttpResponse(OK, jsonResponse)

      when(mockConfig.adrGetObligationDetailsUrl(eqTo(appaId))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(httpResponse)))

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.obligationDetails(appaId)) { result =>
        result mustBe obligationDataResponse
        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when invalid JSON is returned" in new SetUp {
      val invalidJsonResponse = HttpResponse(OK, """{ "invalid": "json" }""")

      when(mockConfig.adrGetObligationDetailsUrl(eqTo(appaId))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(invalidJsonResponse)))

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.obligationDetails(appaId).failed) { e =>
        e.getMessage must include("Invalid JSON format")

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when an unexpected response is returned" in new SetUp {
      val upstreamErrorResponse = Future.successful(
        Left[UpstreamErrorResponse, HttpResponse](UpstreamErrorResponse("", BAD_GATEWAY, BAD_GATEWAY, Map.empty))
      )

      when(mockConfig.adrGetObligationDetailsUrl(eqTo(appaId))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(upstreamErrorResponse)

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.obligationDetails(appaId).failed) { e =>
        e.getMessage must include("Unexpected response")

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when unexpected status code returned" in new SetUp {
      val invalidStatusCodeResponse = HttpResponse(CREATED, "")

      when(mockConfig.adrGetObligationDetailsUrl(eqTo(appaId))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(invalidStatusCodeResponse)))

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.obligationDetails(appaId).failed) { e =>
        e.getMessage mustBe "Unexpected status code: 201"

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }
  }

  "getOpenObligation" - {
    val mockUrl = s"http://alcohol-duty-returns/openObligation/$appaId/$periodKey"

    "successfully retrieve an open obligation" in new SetUp {
      val jsonResponse = Json.toJson(obligationDataSingleOpen).toString()
      val httpResponse = HttpResponse(OK, jsonResponse)

      when(mockConfig.adrGetOpenObligationUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(httpResponse)))

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.getOpenObligation(appaId, periodKey).value) { result =>
        result mustBe Right(obligationDataSingleOpen)
        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when invalid JSON is returned" in new SetUp {
      val invalidJsonResponse = HttpResponse(OK, """{ "invalid": "json" }""")

      when(mockConfig.adrGetOpenObligationUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(invalidJsonResponse)))

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.getOpenObligation(appaId, periodKey).value) { result =>
        result match {
          case Left(err) => err must include("Invalid JSON format")
          case Right(_)  => fail("Expected a Left")
        }

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when no open obligation is found" in new SetUp {
      val notFoundResponse = Future.successful(
        Left[UpstreamErrorResponse, HttpResponse](UpstreamErrorResponse("", NOT_FOUND, NOT_FOUND, Map.empty))
      )

      when(mockConfig.adrGetOpenObligationUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(notFoundResponse)

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.getOpenObligation(appaId, periodKey).value) { result =>
        result mustBe Left("No open obligation found.")

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when another unexpected response is returned" in new SetUp {
      val upstreamErrorResponse = Future.successful(
        Left[UpstreamErrorResponse, HttpResponse](UpstreamErrorResponse("", BAD_GATEWAY, BAD_GATEWAY, Map.empty))
      )

      when(mockConfig.adrGetOpenObligationUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(upstreamErrorResponse)

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.getOpenObligation(appaId, periodKey).value) { result =>
        result mustBe Left("Unexpected response. Status: 502")

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when unexpected status code is returned" in new SetUp {
      val invalidStatusCodeResponse = HttpResponse(CREATED, "")

      when(mockConfig.adrGetOpenObligationUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(invalidStatusCodeResponse)))

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.getOpenObligation(appaId, periodKey).value) { result =>
        result mustBe Left("Unexpected status code: 201")

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }
  }

  "checkSubscriptionStatus" - {
    val mockUrl = s"http://alcohol-duty-returns/subscriptionSummary/$appaId"

    "successfully retrieve subscription and valid status" in new SetUp {
      val alcoholRegimes = Set(Beer, Cider)
      val jsonResponse   = Json.toJson(alcoholRegimes).toString()
      val httpResponse   = HttpResponse(OK, jsonResponse)

      when(mockConfig.adrGetValidSubscriptionRegimes(eqTo(appaId))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(httpResponse)))

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.checkSubscriptionStatus(appaId)) { result =>
        result mustBe Right(true)
        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when subscription approval status is invalid" in new SetUp {
      val forbiddenResponse = Future.successful(
        Left[UpstreamErrorResponse, HttpResponse](UpstreamErrorResponse("", FORBIDDEN, FORBIDDEN, Map.empty))
      )

      when(mockConfig.adrGetValidSubscriptionRegimes(eqTo(appaId))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(forbiddenResponse)

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.getValidSubscriptionRegimes(appaId).value) { result =>
        result mustBe Left("Forbidden: Subscription status is not Approved or Insolvent.")

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when another unexpected response is returned" in new SetUp {
      val upstreamErrorResponse = Future.successful(
        Left[UpstreamErrorResponse, HttpResponse](UpstreamErrorResponse("", BAD_GATEWAY, BAD_GATEWAY, Map.empty))
      )

      when(mockConfig.adrGetValidSubscriptionRegimes(eqTo(appaId))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(upstreamErrorResponse)

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.getValidSubscriptionRegimes(appaId).value) { result =>
        result mustBe Left("Unexpected response. Status: 502")

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }
  }

  "getValidSubscriptionRegimes" - {
    val mockUrl = s"http://alcohol-duty-returns/subscriptionSummary/$appaId"

    "successfully retrieve subscription regimes" in new SetUp {
      val alcoholRegimes = Set(Beer, Cider)
      val jsonResponse   = Json.toJson(alcoholRegimes).toString()
      val httpResponse   = HttpResponse(OK, jsonResponse)

      when(mockConfig.adrGetValidSubscriptionRegimes(eqTo(appaId))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(httpResponse)))

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.getValidSubscriptionRegimes(appaId).value) { result =>
        result mustBe Right(alcoholRegimes)
        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when invalid JSON is returned" in new SetUp {
      val invalidJsonResponse = HttpResponse(OK, """{ "invalid": "json" }""")

      when(mockConfig.adrGetValidSubscriptionRegimes(eqTo(appaId))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(invalidJsonResponse)))

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.getValidSubscriptionRegimes(appaId).value) { result =>
        result match {
          case Left(err) => err must include("Invalid JSON format")
          case Right(_)  => fail("Expected a Left")
        }

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when subscription approval status is invalid" in new SetUp {
      val forbiddenResponse = Future.successful(
        Left[UpstreamErrorResponse, HttpResponse](UpstreamErrorResponse("", FORBIDDEN, FORBIDDEN, Map.empty))
      )

      when(mockConfig.adrGetValidSubscriptionRegimes(eqTo(appaId))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(forbiddenResponse)

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.getValidSubscriptionRegimes(appaId).value) { result =>
        result mustBe Left("Forbidden: Subscription status is not Approved or Insolvent.")

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when another unexpected response is returned" in new SetUp {
      val upstreamErrorResponse = Future.successful(
        Left[UpstreamErrorResponse, HttpResponse](UpstreamErrorResponse("", BAD_GATEWAY, BAD_GATEWAY, Map.empty))
      )

      when(mockConfig.adrGetValidSubscriptionRegimes(eqTo(appaId))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(upstreamErrorResponse)

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.getValidSubscriptionRegimes(appaId).value) { result =>
        result mustBe Left("Unexpected response. Status: 502")

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when unexpected status code is returned" in new SetUp {
      val invalidStatusCodeResponse = HttpResponse(CREATED, "")

      when(mockConfig.adrGetValidSubscriptionRegimes(eqTo(appaId))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(invalidStatusCodeResponse)))

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.getValidSubscriptionRegimes(appaId).value) { result =>
        result mustBe Left("Unexpected status code: 201")

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }
  }

  "submitReturn" - {
    val mockUrl = s"http://alcohol-duty-returns/producers/$appaId/returns/$periodKey"

    "successfully submit a return" in new SetUp {
      val adrReturnCreatedDetails = AdrReturnCreatedDetails(
        processingDate = Instant.now(clock),
        amount = BigDecimal(1),
        chargeReference = Some("1234567890"),
        paymentDueDate = Some(LocalDate.now(clock))
      )
      val jsonResponse            = Json.toJson(adrReturnCreatedDetails).toString()
      val httpResponse            = HttpResponse(CREATED, jsonResponse)

      when(mockConfig.adrSubmitReturnUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(httpResponse)))

      when(
        requestBuilder.withBody(
          eqTo(Json.toJson(fullReturn))
        )(any(), any(), any())
      )
        .thenReturn(requestBuilder)

      when(connector.httpClient.post(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.submitReturn(appaId, periodKey, fullReturn).value) { result =>
        result mustBe Right(adrReturnCreatedDetails)
        verify(connector.httpClient, times(1))
          .post(eqTo(url"$mockUrl"))(any())
      }
    }

    "successfully submit a return and receive nil return response" in new SetUp {
      val adrReturnCreatedDetails = AdrReturnCreatedDetails(
        processingDate = Instant.now(clock),
        amount = BigDecimal(1),
        chargeReference = None,
        paymentDueDate = None
      )
      val jsonResponse            = Json.toJson(adrReturnCreatedDetails).toString()
      val httpResponse            = HttpResponse(CREATED, jsonResponse)

      when(mockConfig.adrSubmitReturnUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(httpResponse)))

      when(
        requestBuilder.withBody(
          eqTo(Json.toJson(nilReturn))
        )(any(), any(), any())
      )
        .thenReturn(requestBuilder)

      when(connector.httpClient.post(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.submitReturn(appaId, periodKey, nilReturn).value) { result =>
        result mustBe Right(adrReturnCreatedDetails)
        verify(connector.httpClient, atLeastOnce)
          .post(eqTo(url"$mockUrl"))(any())
      }
    }

    "fail when invalid JSON is returned" in new SetUp {
      val invalidJsonResponse = HttpResponse(CREATED, """{ "invalid": "json" }""")

      when(mockConfig.adrSubmitReturnUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(invalidJsonResponse)))

      when(
        requestBuilder.withBody(
          eqTo(Json.toJson(nilReturn))
        )(any(), any(), any())
      )
        .thenReturn(requestBuilder)

      when(connector.httpClient.post(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.submitReturn(appaId, periodKey, nilReturn).value) { result =>
        result.swap.toOption.get.status mustBe INTERNAL_SERVER_ERROR
        result.swap.toOption.get.message  must include("Invalid JSON format")

        verify(connector.httpClient, times(1))
          .post(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "return an ErrorModel when a duplicate submission response is returned" in new SetUp {
      val duplicateSubmissionResponse = Future.successful(
        Left[UpstreamErrorResponse, HttpResponse](
          UpstreamErrorResponse("Return already submitted", UNPROCESSABLE_ENTITY, UNPROCESSABLE_ENTITY, Map.empty)
        )
      )

      when(mockConfig.adrSubmitReturnUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(duplicateSubmissionResponse)

      when(
        requestBuilder.withBody(
          eqTo(Json.toJson(nilReturn))
        )(any(), any(), any())
      )
        .thenReturn(requestBuilder)

      when(connector.httpClient.post(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.submitReturn(appaId, periodKey, nilReturn).value) { result =>
        result.swap.toOption.get.status  mustBe UNPROCESSABLE_ENTITY
        result.swap.toOption.get.message mustBe "Return already submitted"

        verify(connector.httpClient, times(1))
          .post(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when an unexpected response is returned" in new SetUp {
      val upstreamErrorResponse = Future.successful(
        Left[UpstreamErrorResponse, HttpResponse](UpstreamErrorResponse("", BAD_GATEWAY, BAD_GATEWAY, Map.empty))
      )

      when(mockConfig.adrSubmitReturnUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(upstreamErrorResponse)

      when(
        requestBuilder.withBody(
          eqTo(Json.toJson(nilReturn))
        )(any(), any(), any())
      )
        .thenReturn(requestBuilder)

      when(connector.httpClient.post(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.submitReturn(appaId, periodKey, nilReturn).value) { result =>
        result.swap.toOption.get.status mustBe INTERNAL_SERVER_ERROR
        result.swap.toOption.get.message  must include("Unexpected response")

        verify(connector.httpClient, times(1))
          .post(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when unexpected status code returned" in new SetUp {
      val invalidStatusCodeResponse = HttpResponse(OK, "")

      when(mockConfig.adrSubmitReturnUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(invalidStatusCodeResponse)))

      when(
        requestBuilder.withBody(
          eqTo(Json.toJson(nilReturn))
        )(any(), any(), any())
      )
        .thenReturn(requestBuilder)

      when(connector.httpClient.post(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.submitReturn(appaId, periodKey, nilReturn).value) { result =>
        result.swap.toOption.get.status mustBe INTERNAL_SERVER_ERROR
        result.swap.toOption.get.message  must include("Unexpected status code: 200")

        verify(connector.httpClient, times(1))
          .post(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }
  }

  "getReturn" - {
    val mockUrl = s"http://alcohol-duty-returns/producers/$appaId/returns/$periodKey"

    "successfully get a return" in new SetUp {
      val adrReturnDetails = exampleReturnDetails(periodKey, Instant.now(clock))
      val jsonResponse     = Json.toJson(adrReturnDetails).toString()
      val httpResponse     = HttpResponse(OK, jsonResponse)

      when(mockConfig.adrGetReturnsUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(httpResponse)))

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.getReturn(appaId, periodKey)) { result =>
        result mustBe adrReturnDetails

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, atLeastOnce)
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when invalid JSON is returned" in new SetUp {
      val invalidJsonResponse = HttpResponse(OK, """{ "invalid": "json" }""")

      when(mockConfig.adrGetReturnsUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(invalidJsonResponse)))

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.getReturn(appaId, periodKey).failed) { e =>
        e.getMessage must include("Invalid JSON format")

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when an unexpected response is returned" in new SetUp {
      val upstreamErrorResponse = Future.successful(
        Left[UpstreamErrorResponse, HttpResponse](UpstreamErrorResponse("", BAD_GATEWAY, BAD_GATEWAY, Map.empty))
      )

      when(mockConfig.adrGetReturnsUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(upstreamErrorResponse)

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.getReturn(appaId, periodKey).failed) { e =>
        e.getMessage must include("Unexpected response")

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when unexpected status code returned" in new SetUp {
      val invalidStatusCodeResponse = HttpResponse(CREATED, "")

      when(mockConfig.adrGetReturnsUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(invalidStatusCodeResponse)))

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.getReturn(appaId, periodKey).failed) { e =>
        e.getMessage must include("Unexpected status code: 201")

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }
  }

  class SetUp {
    val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
    val connector                     = new AlcoholDutyReturnsConnector(config = mockConfig, httpClient = mock[HttpClientV2])

    val requestBuilder: RequestBuilder = mock[RequestBuilder]
  }
}
