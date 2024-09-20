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
import models.adjustment.{AdjustmentDuty, AdjustmentTypes}
import models.returns.AlcoholDuty
import models.{AlcoholRegime, RateBand, RatePeriod}
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsInstances, HttpResponse, StringContextOps, UpstreamErrorResponse}

import java.time.YearMonth
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AlcoholDutyCalculatorConnector @Inject() (
  config: FrontendAppConfig,
  implicit val httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances {

  def rateBandByRegime(ratePeriod: YearMonth, approvedAlcoholRegimes: Seq[AlcoholRegime])(implicit
    hc: HeaderCarrier
  ): Future[Seq[RateBand]] = {
    val queryParams: Seq[(String, String)] = Seq(
      "ratePeriod"     -> Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString,
      "alcoholRegimes" -> Json
        .toJson(
          approvedAlcoholRegimes.map(Json.toJson[AlcoholRegime](_))
        )
        .toString
    )
    httpClient.get(url"${config.adrCalculatorRatesUrl()}?$queryParams").execute[Seq[RateBand]]
  }

  def calculateTotalDuty(requestBody: TotalDutyCalculationRequest)(implicit
    hc: HeaderCarrier
  ): Future[AlcoholDuty] =
    httpClient
      .post(url"${config.adrCalculatorCalculateTotalDutyUrl()}")
      .withBody(Json.toJson(requestBody))
      .execute[AlcoholDuty]

  def rateBand(taxTypeCode: String, ratePeriod: YearMonth)(implicit
    hc: HeaderCarrier
  ): Future[Option[RateBand]] = {
    val queryParams: Seq[(String, String)] = Seq(
      "ratePeriod"  -> Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString,
      "taxTypeCode" -> taxTypeCode
    )
    httpClient
      .get(url"${config.adrCalculatorRateBandUrl()}?$queryParams")
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
      .map({
        case Right(response) if response.status == OK => response.json.asOpt[RateBand]
        case _                                        => None
      })
  }
  def calculateAdjustmentDuty(pureAlcoholVolume: BigDecimal, rate: BigDecimal, adjustmentType: AdjustmentTypes)(implicit
    hc: HeaderCarrier
  ): Future[AdjustmentDuty] = {
    val body: AdjustmentDutyCalculationRequest =
      AdjustmentDutyCalculationRequest(adjustmentType, pureAlcoholVolume, rate)
    httpClient
      .post(url"${config.adrCalculatorCalculateAdjustmentDutyUrl()}")
      .withBody(Json.toJson(body))
      .execute[AdjustmentDuty]
  }
  def calculateRepackagedDutyChange(newDuty: BigDecimal, oldDuty: BigDecimal)(implicit
    hc: HeaderCarrier
  ): Future[AdjustmentDuty] = {
    val body: RepackagedDutyChangeRequest = RepackagedDutyChangeRequest(newDuty, oldDuty)
    httpClient
      .post(url"${config.adrCalculatorCalculateRepackagedDutyChangeUrl()}")
      .withBody(Json.toJson(body))
      .execute[AdjustmentDuty]
  }

  def calculateTotalAdjustment(
    duties: Seq[BigDecimal]
  )(implicit hc: HeaderCarrier): Future[AdjustmentDuty] = {
    val body: AdjustmentTotalCalculationRequest = AdjustmentTotalCalculationRequest(duties)
    httpClient
      .post(url"${config.adrCalculatorCalculateTotalAdjustmentUrl()}")
      .withBody(Json.toJson(body))
      .execute[AdjustmentDuty]
  }
}
