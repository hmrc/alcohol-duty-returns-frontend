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
import models.{AlcoholRegime, ObligationData}
import models.checkAndSubmit.{AdrReturnCreatedDetails, AdrReturnSubmission}
import models.returns.ReturnDetails
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsInstances, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2

class AlcoholDutyReturnsConnector @Inject() (
  config: FrontendAppConfig,
  implicit val httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances
    with Logging {

  def obligationDetails(appaId: String)(implicit hc: HeaderCarrier): Future[Seq[ObligationData]] =
    httpClient
      .get(url"${config.adrGetObligationDetailsUrl(appaId)}")
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
      .flatMap {
        case Right(response) if response.status == OK =>
          Try(response.json.as[Seq[ObligationData]]) match {
            case Success(data)      => Future.successful(data)
            case Failure(exception) => Future.failed(new Exception(s"Invalid JSON format $exception"))
          }
        case Left(errorResponse)                      => Future.failed(new Exception(s"Unexpected response: ${errorResponse.message}"))
        case Right(response)                          => Future.failed(new Exception(s"Unexpected status code: ${response.status}"))
      }

  def getOpenObligation(appaId: String, periodKey: String)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, String, ObligationData] =
    EitherT {
      httpClient
        .get(url"${config.adrGetOpenObligationUrl(appaId, periodKey)}")
        .execute[Either[UpstreamErrorResponse, HttpResponse]]
        .map {
          case Right(response) if response.status == OK                     =>
            Try(response.json.as[ObligationData]) match {
              case Success(data)      => Right[String, ObligationData](data)
              case Failure(exception) =>
                logger.warn(s"Invalid JSON format", exception)
                Left(s"Invalid JSON format $exception")
            }
          case Left(errorResponse) if errorResponse.statusCode == NOT_FOUND =>
            logger.warn(
              s"No open obligation found. Status: ${errorResponse.statusCode}, Message: ${errorResponse.message}"
            )
            Left("No open obligation found.")
          case Left(errorResponse)                                          =>
            logger.warn(s"Unexpected response. Status: ${errorResponse.statusCode}, Message: ${errorResponse.message}")
            Left(s"Unexpected response. Status: ${errorResponse.statusCode}")
          case Right(response)                                              =>
            logger.warn(s"Unexpected status code: ${response.status}")
            Left(s"Unexpected status code: ${response.status}")
        }
    }

  def getValidSubscriptionRegimes(
    appaId: String
  )(implicit hc: HeaderCarrier): EitherT[Future, String, Set[AlcoholRegime]] =
    EitherT {
      httpClient
        .get(url"${config.adrGetValidSubscriptionRegimes(appaId)}")
        .execute[Either[UpstreamErrorResponse, HttpResponse]]
        .map {
          case Right(response) if response.status == OK                     =>
            Try(response.json.as[Set[AlcoholRegime]]) match {
              case Success(data)      => Right[String, Set[AlcoholRegime]](data)
              case Failure(exception) =>
                logger.warn(s"Invalid JSON format", exception)
                Left(s"Invalid JSON format $exception")
            }
          case Left(errorResponse) if errorResponse.statusCode == FORBIDDEN =>
            logger.warn(
              s"Forbidden: Subscription status is not Approved or Insolvent. Status: ${errorResponse.statusCode}, Message: ${errorResponse.message}"
            )
            Left("Forbidden: Subscription status is not Approved or Insolvent.")
          case Left(errorResponse)                                          =>
            logger.warn(s"Unexpected response. Status: ${errorResponse.statusCode}, Message: ${errorResponse.message}")
            Left(s"Unexpected response. Status: ${errorResponse.statusCode}")
          case Right(response)                                              =>
            logger.warn(s"Unexpected status code: ${response.status}")
            Left(s"Unexpected status code: ${response.status}")
        }
    }

  def getReturn(appaId: String, periodKey: String)(implicit hc: HeaderCarrier): Future[ReturnDetails] =
    httpClient
      .get(url"${config.adrGetReturnsUrl(appaId, periodKey)}")
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
      .flatMap {
        case Right(response) if response.status == OK =>
          Try(response.json.as[ReturnDetails]) match {
            case Success(data)      => Future.successful(data)
            case Failure(exception) => Future.failed(new Exception(s"Invalid JSON format $exception"))
          }
        case Left(errorResponse)                      => Future.failed(new Exception(s"Unexpected response: ${errorResponse.message}"))
        case Right(response)                          => Future.failed(new Exception(s"Unexpected status code: ${response.status}"))
      }

  def submitReturn(appaId: String, periodKey: String, returnSubmission: AdrReturnSubmission)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, String, AdrReturnCreatedDetails] =
    EitherT {
      httpClient
        .post(url"${config.adrSubmitReturnUrl(appaId, periodKey)}")
        .withBody(Json.toJson(returnSubmission))
        .execute[Either[UpstreamErrorResponse, HttpResponse]]
        .map {
          case Right(response) if response.status == CREATED =>
            Try(response.json.as[AdrReturnCreatedDetails]) match {
              case Success(data)      => Right[String, AdrReturnCreatedDetails](data)
              case Failure(exception) =>
                logger.warn(s"Invalid JSON format", exception)
                Left(s"Invalid JSON format $exception")
            }
          case Left(errorResponse)                           =>
            logger.warn(s"Unable to submit return. Unexpected response: ${errorResponse.message}")
            Left(s"Unable to submit return. Unexpected response: ${errorResponse.message}")
          case Right(response)                               =>
            logger.warn(s"Unable to submit return. Unexpected status code: ${response.status}")
            Left(s"Unable to submit return. Unexpected status code: ${response.status}")
        }
    }
}
