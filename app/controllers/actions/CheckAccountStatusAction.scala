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

import connectors.AlcoholDutyReturnsConnector
import models.requests.OptionalDataRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckAccountStatusActionImpl @Inject()(alcoholDutyReturnsConnector: AlcoholDutyReturnsConnector)
                                        (implicit val executionContext: ExecutionContext) extends CheckAccountStatusAction {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, OptionalDataRequest[A]]]  = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    alcoholDutyReturnsConnector.checkSubscriptionStatus(request.appaId) map {
      case Right(true) =>
        Right(OptionalDataRequest(request.request, request.appaId, request.groupId, request.userId, request.returnPeriod, request.userAnswers))
      case _ =>
        Left(Redirect(controllers.routes.SubscriptionStatusController.onPageLoad()))
    }
  }
}

trait CheckAccountStatusAction extends ActionRefiner[OptionalDataRequest, OptionalDataRequest]

