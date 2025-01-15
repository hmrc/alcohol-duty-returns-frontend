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

import models.{ReturnPeriod, UserAnswers}
import models.requests.{IdentifierRequest, OptionalDataRequest}
import play.api.mvc.Result

import java.time.YearMonth
import scala.concurrent.{ExecutionContext, Future}

class FakeDataRetrievalAction(maybeUserAnswers: Option[UserAnswers]) extends DataRetrievalAction {
  private val defaultReturnPeriod = ReturnPeriod(YearMonth.of(2025, 1))

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, OptionalDataRequest[A]]] =
    Future(
      Right(
        OptionalDataRequest(
          request.request,
          request.appaId,
          request.groupId,
          request.userId,
          maybeUserAnswers
            .flatMap(userAnswers => ReturnPeriod.fromPeriodKey(userAnswers.returnId.periodKey))
            .orElse(Some(defaultReturnPeriod)),
          maybeUserAnswers
        )
      )
    )

  override protected implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}
