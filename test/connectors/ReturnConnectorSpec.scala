/*
 * Copyright 2023 HM Revenue & Customs
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
import models.ReturnError.{ReturnLocked, ReturnNotFound}
import models.{Return, ReturnPeriod}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.{atLeastOnce, mock, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.{INTERNAL_SERVER_ERROR, LOCKED, NOT_FOUND, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}

class ReturnConnectorSpec extends SpecBase with ScalaFutures {

  protected implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  protected implicit val hc: HeaderCarrier    = HeaderCarrier()
  val mockConfig: FrontendAppConfig           = mock[FrontendAppConfig]
  val connector                               = new ReturnConnector(config = mockConfig, httpClient = mock[HttpClient])
  val periodKey = "24AE"
  val appaId = "XMADP0000000200"
  val internalId = "12345678"
  val returnPeriod = ReturnPeriod(periodKey, 2024, 5)
  val userReturn = Return(returnPeriod, appaId)
  val expectedQueryParams = Seq(
    ("appaId", appaId),
    ("internalId", internalId),
    ("periodKey", periodKey)
  )

  "return connector should" - {
    "successfully get a return" in {
      when {
        connector.httpClient.GET[Either[UpstreamErrorResponse, HttpResponse]](any(), any(), any())(any(), any(), any())
      } thenReturn Future.successful(Right(HttpResponse(OK, Json.toJson[Return](userReturn).toString())))

      whenReady(connector.getReturn(returnPeriod, appaId, internalId).value) {
        result =>
          result mustBe Right(userReturn)
          verify(connector.httpClient, atLeastOnce)
            .GET[Either[UpstreamErrorResponse, HttpResponse]](any(), eqTo(expectedQueryParams), any())(any(), any(), any())

      }
    }

    "return ReturnLocked if LOCKED is returned" in {
      when {
        connector.httpClient.GET[Either[UpstreamErrorResponse, HttpResponse]](any(), any(), any())(any(), any(), any())
      } thenReturn Future.successful(Left(UpstreamErrorResponse("", LOCKED)))

      whenReady(connector.getReturn(returnPeriod, appaId, internalId).value) {
        result =>
          result mustBe Left(ReturnLocked)
          verify(connector.httpClient, atLeastOnce)
            .GET[Either[UpstreamErrorResponse, HttpResponse]](any(), eqTo(expectedQueryParams), any())(any(), any(), any())

      }
    }

    "return ReturnNotFound if NOT_FOUND is returned" in {
      when {
        connector.httpClient.GET[Either[UpstreamErrorResponse, HttpResponse]](any(), any(), any())(any(), any(), any())
      } thenReturn Future.successful(Left(UpstreamErrorResponse("", NOT_FOUND)))

      whenReady(connector.getReturn(returnPeriod, appaId, internalId).value) {
        result =>
          result mustBe Left(ReturnNotFound)
          verify(connector.httpClient, atLeastOnce)
            .GET[Either[UpstreamErrorResponse, HttpResponse]](any(), eqTo(expectedQueryParams), any())(any(), any(), any())

      }
    }

    "return error details if anything else is returned" in {
      when {
        connector.httpClient.GET[Either[UpstreamErrorResponse, HttpResponse]](any(), any(), any())(any(), any(), any())
      } thenReturn Future.successful(Right(HttpResponse(INTERNAL_SERVER_ERROR, "")))

      whenReady(connector.getReturn(returnPeriod, appaId, internalId).value) {
        result =>
          result.isLeft mustBe true
          verify(connector.httpClient, atLeastOnce)
            .GET[Either[UpstreamErrorResponse, HttpResponse]](any(), eqTo(expectedQueryParams), any())(any(), any(), any())

      }
    }
  }
}
