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

import cats.data.EitherT
import com.google.inject.{Inject, Singleton}
import connectors.{AlcoholDutyReturnsConnector, UserAnswersConnector}
import models.{AlcoholRegime, ObligationData, UserAnswers}
import models.ErrorModel
import play.api.http.Status._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BeforeStartReturnService @Inject() (
  userAnswersConnector: UserAnswersConnector,
  alcoholDutyReturnsConnector: AlcoholDutyReturnsConnector
)(implicit ec: ExecutionContext) {

  def handleExistingUserAnswers(userAnswers: UserAnswers)(implicit
    hc: HeaderCarrier
  ): Future[Either[ErrorModel, Unit]] = {
    val appaId = userAnswers.returnId.appaId
    val periodKey = userAnswers.returnId.periodKey
    val subscriptionAndObligation: EitherT[Future, String, (Set[AlcoholRegime], ObligationData)] = for {
      subscriptionRegimes <- alcoholDutyReturnsConnector.getValidSubscriptionRegimes(appaId)
      obligationData      <- alcoholDutyReturnsConnector.getOpenObligation(appaId, periodKey)
    } yield (subscriptionRegimes, obligationData)

    subscriptionAndObligation.value.flatMap {
      case Right((subscriptionRegimes, _)) =>
        if (userAnswers.regimes.regimes equals subscriptionRegimes) {
          Future.successful(Right())
        } else {
          for {
            _ <- userAnswersConnector.delete(appaId, periodKey)
            _ <- userAnswersConnector.releaseLock(userAnswers.returnId)
          } yield Left(ErrorModel(CONFLICT, "Alcohol regimes in existing user answers do not match those from API"))
        }
      case Left(err)                       =>
        Future.successful(Left(ErrorModel(INTERNAL_SERVER_ERROR, err)))
    }
  }

}
