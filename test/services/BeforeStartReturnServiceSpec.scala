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

package services

import base.SpecBase
import cats.data.EitherT
import connectors.{AlcoholDutyReturnsConnector, UserAnswersConnector}
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.{AlcoholRegime, AlcoholRegimes, ErrorModel, UserAnswers}
import org.mockito.ArgumentMatchers.any
import play.api.http.Status._
import uk.gov.hmrc.http.HttpResponse

import java.time.Instant
import scala.concurrent.Future

class BeforeStartReturnServiceSpec extends SpecBase {

  "BeforeStartReturnService" - {

    "handleExistingUserAnswers" - {

      "must return a Right if subscription and obligation status are valid and regimes match the API" in new SetUp {
        when(mockAlcoholDutyReturnsConnector.getValidSubscriptionRegimes(any())(any())) thenReturn EitherT.rightT(
          allRegimes
        )
        when(mockAlcoholDutyReturnsConnector.getOpenObligation(any(), any())(any())) thenReturn EitherT.rightT(
          obligationDataSingleOpen
        )

        whenReady(beforeStartReturnService.handleExistingUserAnswers(emptyUserAnswers)) { result =>
          result mustBe Right()
        }
      }

      "must return a Left with CONFLICT if subscription and obligation status are valid but regimes do not match the API" in new SetUp {
        when(mockAlcoholDutyReturnsConnector.getValidSubscriptionRegimes(any())(any())) thenReturn EitherT.rightT(
          Set(Wine)
        )
        when(mockAlcoholDutyReturnsConnector.getOpenObligation(any(), any())(any())) thenReturn EitherT.rightT(
          obligationDataSingleOpen
        )

        when(mockUserAnswersConnector.delete(any(), any())(any())) thenReturn Future.successful(mockHttpResponse)
        when(mockUserAnswersConnector.releaseLock(any())(any())) thenReturn Future.successful(mockHttpResponse)

        whenReady(beforeStartReturnService.handleExistingUserAnswers(emptyUserAnswers)) { result =>
          result mustBe Left(
            ErrorModel(CONFLICT, "Alcohol regimes in existing user answers do not match those from API")
          )
        }
      }

      "must return a Left with INTERNAL_SERVER_ERROR if subscription approval status is not Approved or Insolvent" in new SetUp {
        when(mockAlcoholDutyReturnsConnector.getValidSubscriptionRegimes(any())(any())) thenReturn EitherT.leftT(
          "Forbidden: Subscription status is not Approved or Insolvent."
        )

        whenReady(beforeStartReturnService.handleExistingUserAnswers(emptyUserAnswers)) { result =>
          result mustBe Left(
            ErrorModel(INTERNAL_SERVER_ERROR, "Forbidden: Subscription status is not Approved or Insolvent.")
          )
        }
      }

      "must return a Left with INTERNAL_SERVER_ERROR if obligation is no longer open" in new SetUp {
        when(mockAlcoholDutyReturnsConnector.getValidSubscriptionRegimes(any())(any())) thenReturn EitherT.rightT(
          allRegimes
        )
        when(mockAlcoholDutyReturnsConnector.getOpenObligation(any(), any())(any())) thenReturn EitherT.leftT(
          "No open obligation found."
        )

        whenReady(beforeStartReturnService.handleExistingUserAnswers(emptyUserAnswers)) { result =>
          result mustBe Left(ErrorModel(INTERNAL_SERVER_ERROR, "No open obligation found."))
        }
      }

      "must return a Left with INTERNAL_SERVER_ERROR if another error occurred" in new SetUp {
        when(mockAlcoholDutyReturnsConnector.getValidSubscriptionRegimes(any())(any())) thenReturn EitherT.leftT(
          "Unexpected status code: 201"
        )

        whenReady(beforeStartReturnService.handleExistingUserAnswers(emptyUserAnswers)) { result =>
          result mustBe Left(ErrorModel(INTERNAL_SERVER_ERROR, "Unexpected status code: 201"))
        }
      }
    }
  }

  class SetUp {
    val mockUserAnswersConnector        = mock[UserAnswersConnector]
    val mockAlcoholDutyReturnsConnector = mock[AlcoholDutyReturnsConnector]
    val mockHttpResponse: HttpResponse  = mock[HttpResponse]

    val beforeStartReturnService =
      new BeforeStartReturnService(mockUserAnswersConnector, mockAlcoholDutyReturnsConnector)

    val allRegimes: Set[AlcoholRegime] = Set(Beer, Cider, Wine, Spirits, OtherFermentedProduct)
    val emptyUserAnswers: UserAnswers  = UserAnswers(
      returnId,
      groupId,
      internalId,
      regimes = AlcoholRegimes(allRegimes),
      startedTime = Instant.now(clock),
      lastUpdated = Instant.now(clock),
      validUntil = Some(Instant.now(clock))
    )
  }
}
