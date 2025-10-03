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

package testonly.connectors

import config.FrontendAppConfig
import models.UserAnswers
import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnAndUserDetails
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestOnlyUserAnswersConnector @Inject() (
  appConfig: FrontendAppConfig,
  httpClient: HttpClientV2
)(implicit val ec: ExecutionContext) {

  def clearAllData()(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.delete(url"${appConfig.adrUserAnswersClearAllUrl()}").execute[HttpResponse]

  def createUserAnswers(
    returnAndUserDetails: ReturnAndUserDetails,
    beer: Boolean,
    cider: Boolean,
    wine: Boolean,
    spirits: Boolean,
    OFP: Boolean
  )(implicit hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, UserAnswers]] =
    httpClient
      .post(url"${appConfig.adrUserAnswersTestOnlyCreateUrl(beer, cider, wine, spirits, OFP)}")
      .withBody(Json.toJson(returnAndUserDetails))
      .setHeader("Csrf-Token" -> "nocheck")
      .execute[Either[UpstreamErrorResponse, UserAnswers]]

  def clearPastPaymentsData()(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.delete(url"${appConfig.adrClearPastPaymentsUrl()}").execute[HttpResponse]

  def clearFulfilledObligationData()(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.delete(url"${appConfig.adrClearFulfilledObligationsUrl()}").execute[HttpResponse]
}
