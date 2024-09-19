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
import models.returns.{AdrReturnCreatedDetails, AdrReturnSubmission}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HttpClient, HttpResponse, UpstreamErrorResponse}
import play.api.http.Status.{BAD_REQUEST, CREATED, OK}
import org.scalatest.RecoverMethods.recoverToExceptionIf

import java.time.{Instant, LocalDate}
import scala.concurrent.Future

class AlcoholDutyReturnsConnectorSpec extends SpecBase with ScalaFutures {
  /*
  val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val connector                     = new AlcoholDutyReturnsConnector(config = mockConfig, httpClient = mock[HttpClient])
  val mockUrl                       = s"http://alcohol-duty-returns/obligationDetails/$appaId"

  "obligationDetails" - {
    "successfully retrieve obligation details" in {
      val obligationDataResponse = Seq(obligationDataSingleOpen)
      val jsonResponse           = Json.toJson(obligationDataResponse).toString()
      val httpResponse           = Future.successful(Right(HttpResponse(OK, jsonResponse)))

      when(mockConfig.adrGetObligationDetailsUrl(eqTo(appaId))).thenReturn(mockUrl)

      when {
        connector.httpClient
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      } thenReturn httpResponse
      whenReady(connector.obligationDetails(appaId)) { result =>
        result mustBe obligationDataResponse
        verify(connector.httpClient, atLeastOnce)
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())

      }
    }
    "fail when invalid JSON is returned" in {
      val invalidJsonResponse = Future.successful(Right(HttpResponse(OK, """{ "invalid": "json" }""")))
      when(mockConfig.adrGetObligationDetailsUrl(appaId)).thenReturn(mockUrl)
      when(
        connector.httpClient
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      )
        .thenReturn(invalidJsonResponse)
      recoverToExceptionIf[Exception] {
        connector.obligationDetails(appaId)
      } map { ex =>
        ex.getMessage must include("Invalid JSON format")
        verify(connector.httpClient, atLeastOnce)
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      }
    }

    "fail when unexpected status code returned" in {
      val invalidStatusCodeResponse = Future.successful(Right(HttpResponse(BAD_REQUEST, "")))
      when(mockConfig.adrGetObligationDetailsUrl(appaId)).thenReturn(mockUrl)
      when(
        connector.httpClient
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      )
        .thenReturn(invalidStatusCodeResponse)
      recoverToExceptionIf[Exception] {
        connector.obligationDetails(appaId)
      } map { ex =>
        ex.getMessage must include("Unexpected status code: 400")
        verify(connector.httpClient, atLeastOnce)
          .GET[Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(any(), any(), any())
      }
    }
  }

  "submitReturn" - {
    "successfully submit a return" in {
      val adrReturnSubmission     = mock[AdrReturnSubmission]
      val adrReturnCreatedDetails = AdrReturnCreatedDetails(
        processingDate = Instant.now(),
        amount = BigDecimal(1),
        chargeReference = Some("1234567890"),
        paymentDueDate = Some(LocalDate.now())
      )
      val jsonResponse            = Json.toJson(adrReturnCreatedDetails).toString()
      val httpResponse            = Future.successful(Right(HttpResponse(CREATED, jsonResponse)))

      when(mockConfig.adrSubmitReturnUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when {
        connector.httpClient
          .POST[AdrReturnSubmission, Either[UpstreamErrorResponse, HttpResponse]](
            eqTo(mockUrl),
            eqTo(adrReturnSubmission),
            any()
          )(any(), any(), any(), any())
      } thenReturn httpResponse

      whenReady(connector.submitReturn(appaId, periodKey, adrReturnSubmission).value) { result =>
        result mustBe Right(adrReturnCreatedDetails)
        verify(connector.httpClient, atLeastOnce)
          .POST[AdrReturnSubmission, Either[UpstreamErrorResponse, HttpResponse]](
            eqTo(mockUrl),
            eqTo(adrReturnSubmission),
            any()
          )(any(), any(), any(), any())
      }
    }

    "successfully submit a return and receive nil return response" in {
      val adrReturnSubmission     = mock[AdrReturnSubmission]
      val adrReturnCreatedDetails = AdrReturnCreatedDetails(
        processingDate = Instant.now(),
        amount = BigDecimal(1),
        chargeReference = None,
        paymentDueDate = None
      )
      val jsonResponse            = Json.toJson(adrReturnCreatedDetails).toString()
      val httpResponse            = Future.successful(Right(HttpResponse(CREATED, jsonResponse)))

      when(mockConfig.adrSubmitReturnUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(mockUrl)

      when {
        connector.httpClient
          .POST[AdrReturnSubmission, Either[UpstreamErrorResponse, HttpResponse]](
            eqTo(mockUrl),
            eqTo(adrReturnSubmission),
            any()
          )(any(), any(), any(), any())
      } thenReturn httpResponse

      whenReady(connector.submitReturn(appaId, periodKey, adrReturnSubmission).value) { result =>
        result mustBe Right(adrReturnCreatedDetails)
        verify(connector.httpClient, atLeastOnce)
          .POST[AdrReturnSubmission, Either[UpstreamErrorResponse, HttpResponse]](
            eqTo(mockUrl),
            eqTo(adrReturnSubmission),
            any()
          )(any(), any(), any(), any())
      }
    }

    "fail when invalid JSON is returned" in {
      val adrReturnSubmission = mock[AdrReturnSubmission]
      val invalidJsonResponse = Future.successful(Right(HttpResponse(OK, """{ "invalid": "json" }""")))
      when(mockConfig.adrSubmitReturnUrl(appaId, periodKey)).thenReturn(mockUrl)
      when(
        connector.httpClient
          .POST[AdrReturnSubmission, Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(
            any(),
            any(),
            any(),
            any()
          )
      )
        .thenReturn(invalidJsonResponse)
      recoverToExceptionIf[String] {
        connector.submitReturn(appaId, periodKey, adrReturnSubmission).value
      } map { ex =>
        ex must include("Invalid JSON format")
        verify(connector.httpClient, atLeastOnce)
          .POST[AdrReturnSubmission, Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(
            any(),
            any(),
            any(),
            any()
          )
      }
    }

    "fail when unexpected status code returned" in {
      val adrReturnSubmission       = mock[AdrReturnSubmission]
      val invalidStatusCodeResponse = Future.successful(Right(HttpResponse(BAD_REQUEST, "")))
      when(mockConfig.adrSubmitReturnUrl(appaId, periodKey)).thenReturn(mockUrl)
      when(
        connector.httpClient
          .POST[AdrReturnSubmission, Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(
            any(),
            any(),
            any(),
            any()
          )
      )
        .thenReturn(invalidStatusCodeResponse)
      recoverToExceptionIf[String] {
        connector.submitReturn(appaId, periodKey, adrReturnSubmission).value
      } map { ex =>
        ex must include("Unexpected status code: 400")
        verify(connector.httpClient, atLeastOnce)
          .POST[AdrReturnSubmission, Either[UpstreamErrorResponse, HttpResponse]](eqTo(mockUrl), any(), any())(
            any(),
            any(),
            any(),
            any()
          )
      }
    }
  }*/
}
