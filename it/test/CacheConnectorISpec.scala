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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, delete, equalToJson, get, post, put, urlMatching}
import connectors.CacheConnector
import models.AlcoholRegime.Beer
import models.{AlcoholRegimes, ReturnId, UserAnswers}
import play.api.Application
import play.api.http.Status.{CREATED, OK}
import play.api.libs.json.Json

import java.time.Instant

class CacheConnectorISpec extends ISpecBase with WireMockHelper {
  override def fakeApplication(): Application = applicationBuilder(None).configure("microservice.services.alcohol-duty-returns.port" -> server.port()).build()

"CacheConnector" - {

  "get" - {
    "successfully fetch cache" in new SetUp{
      val userAnswers = UserAnswers(ReturnId(appaId, periodKey),"abc","xyz",  AlcoholRegimes(Set(Beer)), Json.obj(), Instant.now(clock))
      val jsonResponse = Json.toJson(userAnswers).toString()
      val mockUrl = s"/alcohol-duty-returns/cache/get/$appaId/$periodKey"
      server.stubFor(
        get(urlMatching(mockUrl))
          .willReturn(aResponse().withStatus(OK).withBody(jsonResponse))
      )

      whenReady(connector.get(appaId, periodKey)) { result =>
        result mustBe Right(userAnswers)
      }
    }
  }

 "createUserAnswers" - {
    "successfully write cache" in new SetUp{
      val postUrl = "/alcohol-duty-returns/cache/user-answers"
      server.stubFor(
        post(urlMatching(postUrl))
          .withRequestBody(equalToJson(Json.stringify(Json.toJson(returnAndUserDetails))))
          .willReturn(aResponse().withStatus(CREATED))
      )

      whenReady(connector.createUserAnswers(returnAndUserDetails)) {
        result =>
          result.status mustBe CREATED
      }
    }
  }

   "set" - {
     "successfully write cache" in new SetUp{
       val putUrl = "/alcohol-duty-returns/cache/set"
       server.stubFor(
         put(urlMatching(putUrl))
           .withRequestBody(equalToJson(Json.stringify(Json.toJson(emptyUserAnswers))))
           .willReturn(aResponse().withStatus(OK))
       )
       whenReady(connector.set(emptyUserAnswers)) {
         result =>
           result.status mustBe OK
       }
     }
   }

   "releaseLock" - {
     "should call the release lock endpoint" in new SetUp{
       val releaseLockUrl = s"/alcohol-duty-returns/cache/lock/$appaId/$periodKey"
       server.stubFor(
         delete(urlMatching(releaseLockUrl))
           .willReturn(aResponse().withStatus(OK))
       )

       whenReady(connector.releaseLock(returnId)) {
         result =>
           result mustBe ()
       }
     }
   }

  "keepAlive" - {
    "should call the keep alive endpoint" in new SetUp{
         val keepAliveUrl = s"/alcohol-duty-returns/cache/lock/${returnId.appaId}/${returnId.periodKey}/ttl"
         server.stubFor(
           put(urlMatching(keepAliveUrl))
             .willReturn(aResponse().withStatus(OK))
         )

         whenReady(connector.keepAlive(returnId)) { response =>
           response.status mustBe OK
         }
       }
     }
  }

  class SetUp {
    val connector = app.injector.instanceOf[CacheConnector]
  }
}