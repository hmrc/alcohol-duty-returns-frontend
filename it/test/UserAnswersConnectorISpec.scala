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
import connectors.UserAnswersConnector
import models.AlcoholRegime.Beer
import models.{AlcoholRegimes, ReturnId, UserAnswers}
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, CREATED, INTERNAL_SERVER_ERROR, OK, SERVICE_UNAVAILABLE}
import play.api.libs.json.Json

import java.time.Instant

class UserAnswersConnectorISpec extends ISpecBase with WireMockHelper {
  override def fakeApplication(): Application =
    applicationBuilder(None).configure("microservice.services.alcohol-duty-returns.port" -> server.port()).build()

  "UserAnswersConnector" - {

    "get" - {
      "successfully fetch user answers" in new SetUp {
        val jsonResponse = Json.toJson(userAnswers).toString()
        server.stubFor(
          get(urlMatching(userAnswersGetUrl))
            .willReturn(aResponse().withStatus(OK).withBody(jsonResponse))
        )

        whenReady(connector.get(appaId, periodKey)) { result =>
          result mustBe Right(userAnswers)
        }
      }
      "return an error when the upstream service returns an error" in new SetUp {
        server.stubFor(
          get(urlMatching(userAnswersGetUrl))
            .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
        )

        whenReady(connector.get(appaId, periodKey)) { result =>
          result.isLeft                       mustBe true
          result.swap.toOption.get.statusCode mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "createUserAnswers" - {
      "successfully write user answers" in new SetUp {
        server.stubFor(
          post(urlMatching(userAnswersUrl))
            .withRequestBody(equalToJson(Json.stringify(Json.toJson(returnAndUserDetails))))
            .willReturn(
              aResponse()
                .withStatus(CREATED)
                .withBody(Json.stringify(Json.toJson(userAnswers)))
            )
        )

        whenReady(connector.createUserAnswers(returnAndUserDetails)) {
          case Right(userAnswersResponse) =>
            userAnswersResponse mustBe userAnswers
          case Left(_)                    =>
            fail("Expected Right(UserAnswers), but got Left(UpstreamErrorResponse)")
        }
      }

      "fail to write user answers when the service returns an error" in new SetUp {
        server.stubFor(
          post(urlMatching(userAnswersUrl))
            .withRequestBody(equalToJson(Json.stringify(Json.toJson(returnAndUserDetails))))
            .willReturn(aResponse().withStatus(BAD_REQUEST))
        )

        whenReady(connector.createUserAnswers(returnAndUserDetails)) { result =>
          result.isLeft                       mustBe true
          result.swap.toOption.get.statusCode mustBe BAD_REQUEST
        }
      }
    }

    "set" - {
      "successfully write user answers" in new SetUp {
        server.stubFor(
          put(urlMatching(userAnswersUrl))
            .withRequestBody(equalToJson(Json.stringify(Json.toJson(emptyUserAnswers))))
            .willReturn(aResponse().withStatus(OK))
        )

        whenReady(connector.set(emptyUserAnswers)) { result =>
          result.status mustBe OK
        }
      }

      "fail to write user answers when the service returns an error" in new SetUp {
        server.stubFor(
          put(urlMatching(userAnswersUrl))
            .withRequestBody(equalToJson(Json.stringify(Json.toJson(emptyUserAnswers))))
            .willReturn(aResponse().withStatus(SERVICE_UNAVAILABLE))
        )

        whenReady(connector.set(emptyUserAnswers)) { result =>
          result.status mustBe SERVICE_UNAVAILABLE
        }
      }
    }

    "releaseLock" - {
      "must call the release lock endpoint" in new SetUp {
        server.stubFor(
          delete(urlMatching(releaseLockUrl))
            .willReturn(aResponse().withStatus(OK))
        )

        whenReady(connector.releaseLock(returnId)) { result =>
          result.status mustBe OK
        }
      }

      "return an error when the upstream service returns an error" in new SetUp {
        server.stubFor(
          delete(urlMatching(releaseLockUrl))
            .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
        )

        whenReady(connector.releaseLock(returnId)) { result =>
          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "keepAlive" - {
      "must call the keep alive endpoint" in new SetUp {
        server.stubFor(
          put(urlMatching(keepAliveUrl))
            .willReturn(aResponse().withStatus(OK))
        )

        whenReady(connector.keepAlive(returnId)) { response =>
          response.status mustBe OK
        }
      }

      "return an error when the upstream service returns an error" in new SetUp {
        server.stubFor(
          put(urlMatching(keepAliveUrl))
            .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
        )
        whenReady(connector.keepAlive(returnId)) { response =>
          response.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  class SetUp {
    val connector         = app.injector.instanceOf[UserAnswersConnector]
    val userAnswers       = UserAnswers(
      ReturnId(appaId, periodKey),
      "abc",
      "xyz",
      AlcoholRegimes(Set(Beer)),
      Json.obj(),
      Instant.now(clock),
      Instant.now(clock)
    )
    val userAnswersGetUrl = s"/alcohol-duty-returns/user-answers/$appaId/$periodKey"
    val userAnswersUrl    = "/alcohol-duty-returns/user-answers"
    val releaseLockUrl    = s"/alcohol-duty-returns/user-answers/lock/$appaId/$periodKey"
    val keepAliveUrl      = s"/alcohol-duty-returns/user-answers/lock/${returnId.appaId}/${returnId.periodKey}/ttl"
  }
}
