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

package connectors

import config.FrontendAppConfig
import models.{ReturnId, UserAnswers}
import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnAndUserDetails
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsInstances, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CacheConnector @Inject() (
  config: FrontendAppConfig,
  implicit val httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances {

  def get(appaId: String, periodKey: String)(implicit
    hc: HeaderCarrier
  ): Future[Either[UpstreamErrorResponse, UserAnswers]] =
    httpClient.get(url"${config.adrCacheGetUrl(appaId, periodKey)}").execute[Either[UpstreamErrorResponse, UserAnswers]]

  def set(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .put(url"${config.adrCacheSetUrl()}")
      .setHeader("Csrf-Token" -> "nocheck")
      .withBody(Json.toJson(userAnswers))
      .execute[HttpResponse]

  def createUserAnswers(
    returnAndUserDetails: ReturnAndUserDetails
  )(implicit hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, UserAnswers]] =
    httpClient
      .post(url"${config.adrCacheCreateUserAnswersUrl()}")
      .withBody(Json.toJson(returnAndUserDetails))
      .setHeader("Csrf-Token" -> "nocheck")
      .execute[Either[UpstreamErrorResponse, UserAnswers]]

  def releaseLock(returnId: ReturnId)(implicit hc: HeaderCarrier): Future[Unit] =
    httpClient
      .delete(url"${config.adrReleaseCacheLockUrl(returnId.appaId, returnId.periodKey)}")
      .setHeader("Csrf-Token" -> "nocheck")
      .execute[HttpResponse]
      .map(_ => ())

  def keepAlive(returnId: ReturnId)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .put(url"${config.adrCacheKeepAliveUrl(returnId.appaId, returnId.periodKey)}")
      .setHeader("Csrf-Token" -> "nocheck")
      .execute[HttpResponse]
}
