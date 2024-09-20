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
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HttpClient, HttpResponse, StringContextOps, UpstreamErrorResponse}

import java.time.LocalDateTime
import scala.concurrent.Future

class CacheConnectorSpec extends SpecBase {
  "GET" - {
    "successfully fetch cache" in new SetUp {
      val mockUrl = s"http://alcohol-duty-account/cache/$appaId/$periodKey"
      when(mockConfig.adrCacheGetUrl(any(), any())).thenReturn(mockUrl)

      when(requestBuilder.execute[Either[UpstreamErrorResponse, UserAnswers]](any(), any()))
        .thenReturn(Future.successful(Right(emptyUserAnswers)))

      when {
        connector.httpClient
          .get(any())(any())
      } thenReturn requestBuilder

      whenReady(connector.get("someref", "somePeriodKey")) {
        _ mustBe Right(emptyUserAnswers)
      }
    }
  }

  "POST" - {
    "successfully write cache" in new SetUp {
      val postUrl = "http://cache/user-answers"

      when(mockConfig.adrCacheCreateUserAnswersUrl()).thenReturn(postUrl)

      when {
        connector.httpClient
          .post(any())(any())
      } thenReturn requestBuilder

      when(requestBuilder.withBody(eqTo(Json.toJson(returnAndUserDetails)))(any(), any(), any()))
        .thenReturn(requestBuilder)

      when(requestBuilder.setHeader("Csrf-Token" -> "nocheck"))
        .thenReturn(requestBuilder)

      when(requestBuilder.execute[HttpResponse](any(), any()))
        .thenReturn(Future.successful(mockHttpResponse))

      connector.createUserAnswers(returnAndUserDetails)
      verify(connector.httpClient, atLeastOnce)
        .post(eqTo(url"$postUrl"))(any())
    }
  }

  "PUT" - {
    "successfully write cache" in new SetUp {
      val putUrl = "http://cache/set"

      when(mockConfig.adrCacheSetUrl()).thenReturn(putUrl)

      when {
        connector.httpClient
          .put(any())(any())
      } thenReturn requestBuilder

      when(requestBuilder.withBody(eqTo(Json.toJson(emptyUserAnswers)))(any(), any(), any()))
        .thenReturn(requestBuilder)

      when(requestBuilder.setHeader("Csrf-Token" -> "nocheck"))
        .thenReturn(requestBuilder)

      when(requestBuilder.execute[HttpResponse](any(), any()))
        .thenReturn(Future.successful(mockHttpResponse))

      connector.set(emptyUserAnswers)

      verify(connector.httpClient, atLeastOnce)
        .put(eqTo(url"$putUrl"))(any())
    }
  }

  "releaseLock" - {
    "should call the release lock endpoint" in new SetUp {
      val releaseLockUrl = "http://cache/release-lock"

      when(mockConfig.adrReleaseCacheLockUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(releaseLockUrl)

      when {
        connector.httpClient
          .delete(any())(any())
      } thenReturn requestBuilder

      when(requestBuilder.setHeader("Csrf-Token" -> "nocheck"))
        .thenReturn(requestBuilder)

      when(requestBuilder.execute[HttpResponse](any(), any()))
        .thenReturn(Future.successful(mockHttpResponse))

      whenReady(connector.releaseLock(returnId)) {
        _ mustBe ()
      }

      verify(connector.httpClient, atLeastOnce)
        .delete(eqTo(url"$releaseLockUrl"))(any())
    }
  }

  "keepAlive" - {
    "should call the keep alive endpoint" in new SetUp {
      val keepAliveUrl = s"http://cache/keep-alive/$appaId/$periodKey"

      when(mockConfig.adrCacheKeepAliveUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(keepAliveUrl)

      when {
        connector.httpClient
          .put(any())(any())
      } thenReturn requestBuilder

      when(requestBuilder.withBody(eqTo(Json.toJson(emptyUserAnswers)))(any(), any(), any()))
        .thenReturn(requestBuilder)

      when(requestBuilder.setHeader("Csrf-Token" -> "nocheck"))
        .thenReturn(requestBuilder)

      when(requestBuilder.execute[HttpResponse](any(), any()))
        .thenReturn(Future.successful(mockHttpResponse))

      whenReady(connector.keepAlive(returnId)) { response =>
        response mustBe mockHttpResponse
      }

      verify(connector.httpClient, atLeastOnce)
        .put(eqTo(url"$keepAliveUrl"))(any())
    }
  }

  class SetUp {
    val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
    val httpClient: HttpClientV2      = mock[HttpClientV2]
    val connector                     = new CacheConnector(config = mockConfig, httpClient = httpClient)
    val dateVal: LocalDateTime        = LocalDateTime.now
    val mockHttpResponse: HttpResponse = mock[HttpResponse]
  }
}
