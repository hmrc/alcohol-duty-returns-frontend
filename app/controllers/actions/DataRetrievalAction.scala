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

import config.Constants.periodKeySessionKey
import connectors.CacheConnector
import controllers.routes
import models.ReturnPeriod

import javax.inject.Inject
import models.requests.{IdentifierRequest, OptionalDataRequest}
import play.api.Logging
import play.api.http.Status.{LOCKED, NOT_FOUND}
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject() (
  val cacheConnector: CacheConnector
)(implicit val executionContext: ExecutionContext)
    extends DataRetrievalAction
    with Logging {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, OptionalDataRequest[A]]] = {

    val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    request.session.get(periodKeySessionKey) match {
      case None            =>
        Future.successful(
          Right(OptionalDataRequest(request.request, request.appaId, request.groupId, request.userId, None, None))
        )
      case Some(periodKey) =>
        cacheConnector
          .get(request.appaId, periodKey)(headerCarrier)
          .map {
            case Right(ua)                              =>
              Right(
                OptionalDataRequest(
                  request.request,
                  request.appaId,
                  request.groupId,
                  request.userId,
                  ReturnPeriod.fromPeriodKey(periodKey),
                  Some(ua)
                )
              )
            case Left(ex) if ex.statusCode == NOT_FOUND =>
              logger.warn(s"Return ${request.appaId}/$periodKey not found")
              Right(
                OptionalDataRequest(
                  request.request,
                  request.appaId,
                  request.groupId,
                  request.userId,
                  ReturnPeriod.fromPeriodKey(periodKey),
                  None
                )
              )
            case Left(ex) if ex.statusCode == LOCKED    =>
              logger.warn(s"Return ${request.appaId}/$periodKey locked for the user")
              Left(Redirect(routes.ReturnLockedController.onPageLoad()))
            case Left(ex)                               =>
              logger.warn("Data retrieval failed with exception: ", ex)
              Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))
          }
    }
  }
}

trait DataRetrievalAction extends ActionRefiner[IdentifierRequest, OptionalDataRequest]
