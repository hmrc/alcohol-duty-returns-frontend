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

package connectors

import cats.data.EitherT
import config.FrontendAppConfig
import models.ReturnError.{ReturnLocked, ReturnNotFound, ReturnParsingError, ReturnUpstreamError}
import models.{Return, ReturnError, ReturnPeriod}
import play.api.http.Status.{LOCKED, NOT_FOUND, OK}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReadsInstances, HttpResponse, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReturnConnector @Inject() (
  config: FrontendAppConfig,
  implicit val httpClient: HttpClient
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances {

  def getReturn(returnPeriod: ReturnPeriod, appaId: String, internalId: String)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ReturnError, Return] = EitherT(
    httpClient
      .GET[Either[UpstreamErrorResponse, HttpResponse]](
        url = config.adrReturnGetUrl(),
        queryParams = Seq(
          ("appaId", appaId),
          ("internalId", internalId),
          ("periodKey", returnPeriod.periodKey)
        )
      )
      .map {
        case Right(response) if response.status == OK =>
          println("response: " + response.json)
          response.json
            .asOpt[Return]
            .fold[Either[ReturnError, Return]](Left(ReturnParsingError))(Right(_))
        case Left(errorResponse) if errorResponse.statusCode == LOCKED => Left(ReturnLocked)
        case Left(errorResponse) if errorResponse.statusCode == NOT_FOUND => Left(ReturnNotFound)
        case _                                            => Left(ReturnUpstreamError)
      }
  )
}
