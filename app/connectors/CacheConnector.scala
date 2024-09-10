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
import play.api.libs.json.Writes
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnAndUserDetails
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpReadsInstances, HttpResponse, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CacheConnector @Inject() (
  config: FrontendAppConfig,
  implicit val httpClient: HttpClient
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances {

  def get(appaId: String, periodKey: String)(implicit
    hc: HeaderCarrier
  ): Future[Either[UpstreamErrorResponse, UserAnswers]] =
    httpClient.GET[Either[UpstreamErrorResponse, UserAnswers]](config.adrCacheGetUrl(appaId, periodKey))

  def set(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.PUT(config.adrCacheSetUrl(), userAnswers)(
      implicitly[Writes[UserAnswers]],
      implicitly[HttpReads[HttpResponse]],
      hc.withExtraHeaders("Csrf-Token" -> "nocheck"),
      implicitly
    )

  def createUserAnswers(returnAndUserDetails: ReturnAndUserDetails)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.POST(config.adrCacheCreateUserAnswersUrl(), returnAndUserDetails)(
      implicitly[Writes[ReturnAndUserDetails]],
      implicitly[HttpReads[HttpResponse]],
      hc.withExtraHeaders("Csrf-Token" -> "nocheck"),
      implicitly
    )

  def releaseLocks(returnId: ReturnId)(implicit hc: HeaderCarrier): Future[Unit] =
    httpClient
      .DELETE(config.adrReleaseLockUrl(returnId.appaId, returnId.periodKey))(
        implicitly[HttpReads[HttpResponse]],
        hc.withExtraHeaders("Csrf-Token" -> "nocheck"),
        implicitly
      )
      .map(_ => ())

  def keepAlive(returnId: ReturnId)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.GET(config.adrKeepAliveUrl(returnId.appaId, returnId.periodKey))(
      implicitly[HttpReads[HttpResponse]],
      hc.withExtraHeaders("Csrf-Token" -> "nocheck"),
      implicitly
    )
}
