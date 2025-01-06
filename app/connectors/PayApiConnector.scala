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
import models.payments.{StartPaymentRequest, StartPaymentResponse}
import play.api.Logging
import play.api.http.Status.CREATED
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsInstances, HttpResponse, StringContextOps, UpstreamErrorResponse}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class PayApiConnector @Inject() (
  config: FrontendAppConfig,
  implicit val httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances
    with Logging {

  def startPayment(
    startPaymentRequest: StartPaymentRequest
  )(implicit hc: HeaderCarrier): EitherT[Future, String, StartPaymentResponse] =
    EitherT {
      httpClient
        .post(url"${config.startPaymentUrl}")
        .withBody(Json.toJson(startPaymentRequest))
        .execute[Either[UpstreamErrorResponse, HttpResponse]]
        .map {
          case Right(response) if response.status == CREATED =>
            Try(response.json.as[StartPaymentResponse]) match {
              case Success(data)      => Right[String, StartPaymentResponse](data)
              case Failure(exception) =>
                logger.warn("Invalid JSON format when starting payment", exception)
                Left(s"Invalid JSON format when starting payment. Exception: $exception")
            }
          case Left(errorResponse: UpstreamErrorResponse)    =>
            logger.warn("Start Payment failed with error. Error response", errorResponse)
            Left(s"Start Payment failed with error. Error response: ${errorResponse.message}")
          case Right(otherStatusResponse)                    =>
            logger.warn(s"Unexpected status code when starting payment: ${otherStatusResponse.status}")
            Left(s"Unexpected status code when starting payment: ${otherStatusResponse.status}")
        }
    }
}
