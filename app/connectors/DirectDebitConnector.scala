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
import models.payments.{StartDirectDebitRequest, StartDirectDebitResponse}
import play.api.Logging
import play.api.http.Status.CREATED
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReadsInstances, HttpResponse, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class DirectDebitConnector @Inject() (
  config: FrontendAppConfig,
  implicit val httpClient: HttpClient
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances
    with Logging {

  def startDirectDebit(
    startDirectDebitRequest: StartDirectDebitRequest
  )(implicit hc: HeaderCarrier): EitherT[Future, String, StartDirectDebitResponse] =
    EitherT {
      httpClient
        .POST[StartDirectDebitRequest, Either[UpstreamErrorResponse, HttpResponse]](
          url = config.startDirectDebitUrl,
          startDirectDebitRequest
        )
        .map {
          case Right(response) if response.status == CREATED =>
            Try(response.json.as[StartDirectDebitResponse]) match {
              case Success(data)      => Right[String, StartDirectDebitResponse](data)
              case Failure(exception) =>
                logger.warn("Invalid JSON format when starting direct debit", exception)
                Left(s"Invalid JSON format when starting direct debit. Exception: $exception")
            }
          case Left(errorResponse: UpstreamErrorResponse)    =>
            logger.warn("Start Direct Debit failed with error. Error response", errorResponse)
            Left(s"Start Direct Debit failed with error. Error response: ${errorResponse.message}")
          case Right(otherStatusResponse)                    =>
            logger.warn(s"Unexpected status code received when starting direct debit: ${otherStatusResponse.status}")
            Left(s"Unexpected status code received when starting direct debit: ${otherStatusResponse.status}")
        }
    }
}
