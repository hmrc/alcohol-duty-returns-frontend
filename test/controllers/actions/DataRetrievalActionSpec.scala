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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import connectors.CacheConnector
import models.requests.{IdentifierRequest, OptionalDataRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar {
  val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
  when(mockConfig.periodKeySessionKey).thenReturn("period-key")

  class Harness(cacheConnector: CacheConnector) extends DataRetrievalActionImpl(mockConfig, cacheConnector) {
    def callTransform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  "Data Retrieval Action" - {

    "when there is no data in the cache" - {

      "must set userAnswers to 'None' in the request" in {

        val cacheConnector = mock[CacheConnector]
        when(cacheConnector.get(eqTo(appaId), eqTo(periodKey))(any())) thenReturn Future(None)
        val action         = new Harness(cacheConnector)

        val result = action.callTransform(IdentifierRequest(FakeRequest(), appaId, groupId, userAnswersId)).futureValue

        result.userAnswers must not be defined
      }
    }

    "when there is data in the cache" - {

      "must build a userAnswers object and add it to the request" in {

        val cacheConnector = mock[CacheConnector]
        when(cacheConnector.get(eqTo(appaId), eqTo(periodKey))(any)) thenReturn Future(Some(emptyUserAnswers))
        val action         = new Harness(cacheConnector)

        val result =
          action
            .callTransform(
              new IdentifierRequest(
                FakeRequest().withSession((mockConfig.periodKeySessionKey, periodKey)),
                appaId,
                groupId,
                userAnswersId
              )
            )
            .futureValue

        result.userAnswers mustBe defined
      }

      "must set userAnswers to 'None' if the session does not contain the period Key" in {

        val cacheConnector = mock[CacheConnector]
        when(cacheConnector.get(eqTo(appaId), eqTo(periodKey))(any)) thenReturn Future(Some(emptyUserAnswers))
        val action         = new Harness(cacheConnector)

        val result =
          action
            .callTransform(
              IdentifierRequest(
                play.api.test.FakeRequest(),
                appaId,
                groupId,
                userAnswersId
              )
            )
            .futureValue

        result.userAnswers must not be defined
      }
    }
  }
}
