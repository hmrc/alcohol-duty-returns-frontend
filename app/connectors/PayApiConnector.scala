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
import models.payments.StartPaymentRequest
import play.api.Logging
import play.api.http.Status.CREATED
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReadsInstances, HttpResponse, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class StartPaymentResponse(journeyId: String, nextUrl: String)

object StartPaymentResponse {
  implicit val formats: OFormat[StartPaymentResponse] = Json.format[StartPaymentResponse]
}

class PayApiConnector @Inject() (
  config: FrontendAppConfig,
  implicit val httpClient: HttpClient
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances
    with Logging {

  def startPayment(
    paymentStart: StartPaymentRequest
  )(implicit hc: HeaderCarrier): EitherT[Future, String, StartPaymentResponse] =
    EitherT {
      httpClient
        .POST[StartPaymentRequest, Either[UpstreamErrorResponse, HttpResponse]](
          url = config.alcoholDutyStartPaymentUrl,
          paymentStart
        )
        .map {
          case Right(response) if response.status == CREATED =>
            Try(response.json.as[StartPaymentResponse]) match {
              case Success(data)      => Right[String, StartPaymentResponse](data)
              case Failure(exception) =>
                logger.warn("Invalid JSON format", exception)
                Left(s"Invalid JSON format $exception")
            }
          case Left(errorResponse: UpstreamErrorResponse)    =>
            logger.warn("Start Payment failed with error. Error response", errorResponse)
            Left(s"Start Payment failed with error. Error response: ${errorResponse.message}")
          case Right(otherStatusResponse)                    =>
            logger.warn(s"Unexpected status code: ${otherStatusResponse.status}")
            Left(s"Unexpected status code: ${otherStatusResponse.status}")
        }
    }
}
