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
import config.Constants.periodKeySessionKey
import config.FrontendAppConfig
import connectors.UserAnswersConnector
import models.requests.{IdentifierRequest, OptionalDataRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.http.Status.{LOCKED, SEE_OTHER}
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.http.UpstreamErrorResponse

import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase {
  val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]

  class Harness(userAnswersConnector: UserAnswersConnector)
      extends DataRetrievalActionImpl(mockConfig, userAnswersConnector) {
    def actionRefine[A](request: IdentifierRequest[A]): Future[Either[Result, OptionalDataRequest[A]]] = refine(request)
  }

  "Data Retrieval Action" - {

    "when there is no data in the user answers" - {

      "must set userAnswers to 'None' in the request" in {
        val mockUpstreamErrorResponse = mock[UpstreamErrorResponse]
        when(mockUpstreamErrorResponse.statusCode).thenReturn(NOT_FOUND)

        val userAnswersConnector = mock[UserAnswersConnector]
        when(userAnswersConnector.get(eqTo(appaId), eqTo(periodKey))(any())) thenReturn Future(
          Left(mockUpstreamErrorResponse)
        )
        val action               = new Harness(userAnswersConnector)

        val result = action.actionRefine(IdentifierRequest(FakeRequest(), appaId, groupId, internalId)).futureValue

        result.isRight mustBe true
        result.map { dataRetrievalRequest =>
          dataRetrievalRequest.userAnswers must not be defined
        }
      }
    }

    "when there is data in the user answers" - {

      "must build a userAnswers object and add it to the request" in {

        val userAnswersConnector = mock[UserAnswersConnector]
        when(userAnswersConnector.get(eqTo(appaId), eqTo(periodKey))(any)) thenReturn Future(Right(emptyUserAnswers))
        val action               = new Harness(userAnswersConnector)

        val result =
          action
            .actionRefine(
              IdentifierRequest(
                FakeRequest().withSession((periodKeySessionKey, periodKey)),
                appaId,
                groupId,
                internalId
              )
            )
            .futureValue

        result.isRight mustBe true
        result.map { dataRetrievalRequest =>
          dataRetrievalRequest.userAnswers mustBe defined
        }
      }

      "must set userAnswers to 'None' if the session does not contain the period Key" in {

        val userAnswersConnector = mock[UserAnswersConnector]
        when(userAnswersConnector.get(eqTo(appaId), eqTo(periodKey))(any)) thenReturn Future(Right(emptyUserAnswers))
        val action               = new Harness(userAnswersConnector)

        val result =
          action
            .actionRefine(
              IdentifierRequest(
                play.api.test.FakeRequest(),
                appaId,
                groupId,
                internalId
              )
            )
            .futureValue

        result.isRight mustBe true
        result.map { dataRetrievalRequest =>
          dataRetrievalRequest.userAnswers must not be defined
        }
      }
    }

    "when the User Answer Connector return an error" - {

      "must redirect to the Return Locked controller if the error status is Locked" in {

        val mockUpstreamErrorResponse = mock[UpstreamErrorResponse]
        when(mockUpstreamErrorResponse.statusCode).thenReturn(LOCKED)

        val userAnswersConnector = mock[UserAnswersConnector]
        when(userAnswersConnector.get(eqTo(appaId), eqTo(periodKey))(any())) thenReturn Future(
          Left(mockUpstreamErrorResponse)
        )
        val action               = new Harness(userAnswersConnector)

        val result = action.actionRefine(IdentifierRequest(FakeRequest(), appaId, groupId, internalId))

        val redirectResult = result.map {
          case Left(res) => res
          case _         => fail()
        }
        status(redirectResult) mustBe SEE_OTHER
        redirectLocation(redirectResult).value mustEqual controllers.routes.ReturnLockedController.onPageLoad().url
      }

      "must redirect to the Journey Recovery controller if the error status is not Bad Request" in {

        val mockUpstreamErrorResponse = mock[UpstreamErrorResponse]
        when(mockUpstreamErrorResponse.statusCode).thenReturn(BAD_REQUEST)

        val userAnswersConnector = mock[UserAnswersConnector]
        when(userAnswersConnector.get(eqTo(appaId), eqTo(periodKey))(any())) thenReturn Future(
          Left(mockUpstreamErrorResponse)
        )
        val action               = new Harness(userAnswersConnector)

        val result = action.actionRefine(IdentifierRequest(FakeRequest(), appaId, groupId, internalId))

        val redirectResult = result.map {
          case Left(res) => res
          case _         => fail()
        }
        status(redirectResult) mustBe SEE_OTHER
        redirectLocation(redirectResult).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
