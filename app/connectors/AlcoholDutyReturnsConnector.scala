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

import config.FrontendAppConfig
import models.ObligationData
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReadsInstances, HttpResponse, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import play.api.http.Status.OK

class AlcoholDutyReturnsConnector @Inject() (
  config: FrontendAppConfig,
  implicit val httpClient: HttpClient
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances {

  def obligationDetails(appaId: String)(implicit hc: HeaderCarrier): Future[Seq[ObligationData]] =
    httpClient
      .GET[Either[UpstreamErrorResponse, HttpResponse]](url = config.adrGetObligationDetailsUrl(appaId))
      .flatMap {
        case Right(response) if response.status == OK =>
          Try(response.json.as[Seq[ObligationData]]) match {
            case Success(data)      => Future.successful(data)
            case Failure(exception) => Future.failed(new Exception("Invalid JSON format " + exception))
          }
        case Left(errorResponse)                      => Future.failed(new Exception(s"Unexpected response: ${errorResponse.message}"))
        case Right(response)                          => Future.failed(new Exception(s"Unexpected status code: ${response.status}"))
      }
}
