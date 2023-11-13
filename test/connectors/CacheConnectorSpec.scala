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
import org.mockito.MockitoSugar.{atLeastOnce, mock, verify, when}
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class CacheConnectorSpec extends SpecBase with ScalaFutures {

  protected implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  protected implicit val hc: HeaderCarrier = HeaderCarrier()
  val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val connector = new CacheConnector(config = mockConfig, httpClient = mock[HttpClient])
  val dateVal: LocalDateTime = LocalDateTime.now
  val answers: UserAnswers = UserAnswers("id")

  "GET" - {
    "successfully fetch cache" in {

      when {
        connector.httpClient.GET[Option[UserAnswers]](any(), any(), any())(any(), any(), any())
      } thenReturn Future.successful(Some(answers))

      whenReady(connector.get("someref")) {
        _ mustBe Some(answers)
      }
    }
  }

  "POST" - {
    "successfully write cache" in {
      Mockito.reset(connector.httpClient)

      val putUrl = s"/cache/set/someref"

      when(mockConfig.adrCacheSetUrl(any())).thenReturn("/cache/set/someref")

      connector.set(answers)
      verify(connector.httpClient, atLeastOnce).POST(eqTo(putUrl), eqTo(answers), any())(any(), any(), any(), any())
    }
  }
}