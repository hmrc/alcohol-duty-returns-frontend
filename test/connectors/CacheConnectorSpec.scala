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
import play.api.libs.json.JsObject
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HttpClient, HttpResponse, UpstreamErrorResponse}

import java.time.LocalDateTime
import scala.concurrent.Future

class CacheConnectorSpec extends SpecBase {
  /*
  val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val httpClient: HttpClient        = mock[HttpClientV2]
  val connector                     = new CacheConnector(config = mockConfig, httpClient = httpClient)
  val dateVal: LocalDateTime        = LocalDateTime.now

  "GET" - {
    "successfully fetch cache" in {

      when {
        connector.httpClient
          .GET[Either[UpstreamErrorResponse, UserAnswers]](any(), any(), any())(any(), any(), any())
      } thenReturn Future.successful(Right(emptyUserAnswers))

      whenReady(connector.get("someref", "somePeriodKey")) {
        _ mustBe Right(emptyUserAnswers)
      }
    }
  }

  "POST" - {
    "successfully write cache" in {
      Mockito.reset(connector.httpClient)

      val postUrl = "/cache/user-answers"

      when(mockConfig.adrCacheCreateUserAnswersUrl()).thenReturn(postUrl)

      connector.createUserAnswers(returnAndUserDetails)
      verify(connector.httpClient, atLeastOnce)
        .POST(eqTo(postUrl), eqTo(returnAndUserDetails), any())(any(), any(), any(), any())
    }
  }

  "PUT" - {
    "successfully write cache" in {
      Mockito.reset(connector.httpClient)

      val putUrl = "/cache/set"

      when(mockConfig.adrCacheSetUrl()).thenReturn(putUrl)

      connector.set(emptyUserAnswers)
      verify(connector.httpClient, atLeastOnce)
        .PUT(eqTo(putUrl), eqTo(emptyUserAnswers), any())(any(), any(), any(), any())
    }
  }

  "releaseLock" - {
    "should call the release lock endpoint" in {
      Mockito.reset(httpClient)

      val releaseLockUrl = "/cache/release-lock"

      when(mockConfig.adrReleaseCacheLockUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(releaseLockUrl)
      when(httpClient.DELETE[HttpResponse](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(mock[HttpResponse]))

      whenReady(connector.releaseLock(returnId)) {
        _ mustBe ()
      }
    }
  }

  "keepAlive" - {
    "should call the keep alive endpoint" in {
      Mockito.reset(httpClient)

      val keepAliveUrl = s"/cache/keep-alive/$appaId/$periodKey"
      when(mockConfig.adrCacheKeepAliveUrl(eqTo(appaId), eqTo(periodKey))).thenReturn(keepAliveUrl)

      when(httpClient.PUT[JsObject, HttpResponse](any(), any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(mock[HttpResponse]))

      whenReady(connector.keepAlive(returnId)) { response =>
        response mustBe a[HttpResponse]
      }
    }
  }*/
}
