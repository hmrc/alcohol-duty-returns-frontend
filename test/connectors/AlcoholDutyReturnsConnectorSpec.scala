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
import models.returns.AdrReturnCreatedDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HttpResponse, StringContextOps, UpstreamErrorResponse}
import play.api.http.Status.{BAD_REQUEST, CREATED, OK}
import org.scalatest.RecoverMethods.recoverToExceptionIf
import uk.gov.hmrc.http.client.HttpClientV2

import java.time.{Instant, LocalDate}
import scala.concurrent.Future

class AlcoholDutyReturnsConnectorSpec extends SpecBase with ScalaFutures {

  val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val connector                     = new AlcoholDutyReturnsConnector(config = mockConfig, httpClient = mock[HttpClientV2])
  val mockUrl                       = s"http://alcohol-duty-returns/obligationDetails/$appaId"

  "obligationDetails" - {
    when(mockConfig.adrGetObligationDetailsUrl(eqTo(appaId))).thenReturn(mockUrl)
    "successfully retrieve obligation details" in {
      val obligationDataResponse = Seq(obligationDataSingleOpen)
      val jsonResponse           = Json.toJson(obligationDataResponse).toString()
      val httpResponse           = HttpResponse(OK, jsonResponse)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(httpResponse)))

      when {
        connector.httpClient
          .get(any())(any())
      } thenReturn requestBuilder

      whenReady(connector.obligationDetails(appaId)) { result =>
        result mustBe obligationDataResponse
        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when invalid JSON is returned" in {
      val invalidJsonResponse = HttpResponse(OK, """{ "invalid": "json" }""")

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(invalidJsonResponse)))

      when {
        connector.httpClient
          .get(any())(any())
      } thenReturn requestBuilder

      recoverToExceptionIf[Exception] {
        connector.obligationDetails(appaId)
      } map { ex =>
        ex.getMessage must include("Invalid JSON format")
        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "fail when unexpected status code returned" in {
      val invalidStatusCodeResponse = HttpResponse(BAD_REQUEST, "")

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(invalidStatusCodeResponse)))

      when {
        connector.httpClient
          .get(any())(any())
      } thenReturn requestBuilder

      recoverToExceptionIf[Exception] {
        connector.obligationDetails(appaId)
      } map { ex =>
        ex.getMessage must include("Unexpected status code: 400")

        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl"))(any())

        verify(requestBuilder, times(1))
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }
  }

  "submitReturn" - {
    val mockUrl = s"http://alcohol-duty-returns/producers/$appaId/returns/$periodKey"
    when(mockConfig.adrSubmitReturnUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)
    "successfully submit a return" in {
      val adrReturnCreatedDetails = AdrReturnCreatedDetails(
        processingDate = Instant.now(),
        amount = BigDecimal(1),
        chargeReference = Some("1234567890"),
        paymentDueDate = Some(LocalDate.now())
      )
      val jsonResponse            = Json.toJson(adrReturnCreatedDetails).toString()
      val httpResponse            = HttpResponse(CREATED, jsonResponse)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(httpResponse)))

      when(
        requestBuilder.withBody(
          eqTo(Json.toJson(fullReturn))
        )(any(), any(), any())
      )
        .thenReturn(requestBuilder)

      when {
        connector.httpClient
          .post(eqTo(url"$mockUrl"))(any())
      } thenReturn requestBuilder

      whenReady(connector.submitReturn(appaId, periodKey, fullReturn).value) { result =>
        result mustBe Right(adrReturnCreatedDetails)
        verify(connector.httpClient, times(1))
          .post(eqTo(url"$mockUrl"))(any())
      }
    }

    "successfully submit a return and receive nil return response" in {

      val adrReturnCreatedDetails = AdrReturnCreatedDetails(
        processingDate = Instant.now(),
        amount = BigDecimal(1),
        chargeReference = None,
        paymentDueDate = None
      )
      val jsonResponse            = Json.toJson(adrReturnCreatedDetails).toString()
      val httpResponse            = HttpResponse(CREATED, jsonResponse)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(httpResponse)))

      when(
        requestBuilder.withBody(
          eqTo(Json.toJson(nilReturn))
        )(any(), any(), any())
      )
        .thenReturn(requestBuilder)

      when {
        connector.httpClient
          .post(any())(any())
      } thenReturn requestBuilder

      whenReady(connector.submitReturn(appaId, periodKey, nilReturn).value) { result =>
        result mustBe Right(adrReturnCreatedDetails)
        verify(connector.httpClient, atLeastOnce)
          .post(eqTo(url"$mockUrl"))(any())
      }

    }

    "fail when invalid JSON is returned" in {
      val invalidJsonResponse = HttpResponse(OK, """{ "invalid": "json" }""")

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(invalidJsonResponse)))

      when(
        requestBuilder.withBody(
          eqTo(Json.toJson(nilReturn))
        )(any(), any(), any())
      )
        .thenReturn(requestBuilder)

      when {
        connector.httpClient
          .post(eqTo(url"$mockUrl"))(any())
      } thenReturn requestBuilder

      recoverToExceptionIf[String] {
        connector.submitReturn(appaId, periodKey, nilReturn).value
      } map { ex =>
        ex must include("Invalid JSON format")
        verify(connector.httpClient, times(1))
          .post(eqTo(url"$mockUrl"))(any())
      }
    }
  }
  "fail when unexpected status code returned" in {

    val invalidStatusCodeResponse = HttpResponse(BAD_REQUEST, "")

    when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
      .thenReturn(Future.successful(Right(invalidStatusCodeResponse)))

    when(
      requestBuilder.withBody(
        eqTo(Json.toJson(nilReturn))
      )(any(), any(), any())
    )
      .thenReturn(requestBuilder)

    recoverToExceptionIf[String] {
      connector.submitReturn(appaId, periodKey, nilReturn).value
    } map { ex =>
      ex must include("Unexpected status code: 400")
      verify(connector.httpClient, times(1))
        .post(eqTo(url"$mockUrl"))(any())
    }
  }
  class SetUp {}
}
