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
import models.productEntry.TaxDuty
import models.{AlcoholByVolume, AlcoholRegime, RateBand, RatePeriod, RateType, RateTypeResponse, TaxType}
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReadsInstances, HttpResponse, UpstreamErrorResponse}

import java.time.YearMonth
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AlcoholDutyCalculatorConnector @Inject() (
  config: FrontendAppConfig,
  implicit val httpClient: HttpClient
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances {
  def calculateTaxDuty(abv: AlcoholByVolume, volume: BigDecimal, rate: BigDecimal)(implicit
    hc: HeaderCarrier
  ): Future[TaxDuty] = {
    val body: DutyCalculationRequest = DutyCalculationRequest(abv, volume, rate)
    httpClient.POST[DutyCalculationRequest, TaxDuty](url = config.adrCalculatorCalculateDutyUrl(), body = body)
  }

  def rates(
    rateType: RateType,
    abv: AlcoholByVolume,
    ratePeriod: YearMonth,
    approvedAlcoholRegimes: Set[AlcoholRegime]
  )(implicit hc: HeaderCarrier): Future[Seq[RateBand]] = {
    val queryParams: Seq[(String, String)] = Seq(
      "ratePeriod"     -> Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString,
      "alcoholRegimes" -> Json
        .toJson(
          approvedAlcoholRegimes.map(Json.toJson[AlcoholRegime](_))
        )
        .toString,
      "rateType"       -> Json.toJson(rateType).toString,
      "abv"            -> Json.toJson(abv).toString
    )
    httpClient.GET[Seq[RateBand]](url = config.adrCalculatorRatesUrl(), queryParams = queryParams)
  }

  def adjustmentTaxType(taxType: TaxType, ratePeriod: YearMonth)(implicit
    hc: HeaderCarrier
  ): Future[Option[RateBand]] = {
    val queryParams: Seq[(String, String)] = Seq(
      "ratePeriod" -> Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString,
      "taxType"    -> Json.toJson(taxType)(TaxType.format).toString
    )
    httpClient
      .GET[Either[UpstreamErrorResponse, HttpResponse]](
        url = config.adrCalculatorAdjustmentTaxType(),
        queryParams = queryParams
      )
      .map({
        case Right(response) if response.status == OK => response.json.asOpt[RateBand]
        case _                                        => None
      })
  }
  def rateType(
    abv: AlcoholByVolume,
    ratePeriod: YearMonth,
    approvedAlcoholRegimes: Set[AlcoholRegime]
  )(implicit hc: HeaderCarrier): Future[RateTypeResponse] = {
    val queryParams: Seq[(String, String)] = Seq(
      "ratePeriod"     -> Json.toJson(ratePeriod)(RatePeriod.yearMonthFormat).toString,
      "alcoholRegimes" -> Json
        .toJson(
          approvedAlcoholRegimes.map(Json.toJson[AlcoholRegime](_))
        )
        .toString,
      "abv"            -> Json.toJson(abv).toString
    )
    httpClient.GET[RateTypeResponse](url = config.adrCalculatorRateTypeUrl(), queryParams = queryParams)
  }
}
