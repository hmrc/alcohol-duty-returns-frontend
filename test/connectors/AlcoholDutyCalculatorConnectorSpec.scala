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

import base.SpecBase
import cats.data.NonEmptySeq
import config.FrontendAppConfig
import generators.ModelGenerators
import models.AlcoholRegime.{Beer, Wine}
import models.RateType.DraughtRelief
import models.productEntry.TaxDuty
import models.{ABVInterval, ABVIntervalLabel, AlcoholByVolume, AlcoholRegime, RateBand, RatePeriod, RateType, RateTypeResponse}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{atLeastOnce, mock, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, UpstreamErrorResponse}

import java.time.YearMonth
import scala.concurrent.{ExecutionContext, Future}

class AlcoholDutyCalculatorConnectorSpec extends SpecBase with ScalaFutures with ModelGenerators {

  protected implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  protected implicit val hc: HeaderCarrier    = HeaderCarrier()
  val mockConfig: FrontendAppConfig           = mock[FrontendAppConfig]
  val connector                               = new AlcoholDutyCalculatorConnector(config = mockConfig, httpClient = mock[HttpClient])
  val rateBand                                = RateBand(
    "310",
    "some band",
    RateType.DraughtRelief,
    Set(AlcoholRegime.Beer),
    intervals = NonEmptySeq.one(
      ABVInterval(
        ABVIntervalLabel.Beer,
        AlcoholByVolume(0.1),
        AlcoholByVolume(5.8)
      )
    ),
    Some(BigDecimal(10.99))
  )
  val rateBandList: Seq[RateBand]             = Seq(rateBand)
  val rateType                                = RateTypeResponse(DraughtRelief)
  val ratePeriod                              = returnPeriodGen.sample.get

  "rates" - {
    "successfully retrieve rates" in {
      when {
        connector.httpClient.GET[Seq[RateBand]](any(), any(), any())(any(), any(), any())
      } thenReturn Future.successful(rateBandList)

      whenReady(connector.rates(DraughtRelief, AlcoholByVolume(3.5), YearMonth.of(2023, 1), Set(Beer, Wine))) {
        result =>
          result mustBe rateBandList
          verify(connector.httpClient, atLeastOnce)
            .GET[Seq[RateBand]](
              any(),
              ArgumentMatchers.eq(
                Seq(
                  ("ratePeriod", Json.toJson(YearMonth.of(2023, 1))(RatePeriod.yearMonthFormat).toString),
                  ("alcoholRegimes", Json.toJson(Set("Beer", "Wine")).toString()),
                  ("rateType", Json.toJson[RateType](DraughtRelief).toString),
                  ("abv", "3.5")
                )
              ),
              any()
            )(any(), any(), any())
      }
    }
  }

  "calculateTaxDuty" - {
    "successfully retrieve tax duty" in {
      when {
        connector.httpClient.POST[DutyCalculationRequest, TaxDuty](any(), any(), any())(any(), any(), any(), any())
      } thenReturn Future.successful(TaxDuty(BigDecimal(1), BigDecimal(1)))

      whenReady(connector.calculateTaxDuty(AlcoholByVolume(3.5), BigDecimal(1), BigDecimal(1))) { result =>
        result mustBe TaxDuty(BigDecimal(1), BigDecimal(1))
        verify(connector.httpClient, atLeastOnce)
          .POST[DutyCalculationRequest, TaxDuty](
            any(),
            ArgumentMatchers.eq(DutyCalculationRequest(AlcoholByVolume(3.5), BigDecimal(1), BigDecimal(1))),
            any()
          )(any(), any(), any(), any())
      }
    }
  }

  "rateType" - {
    "successfully retrieve rates" in {
      when {
        connector.httpClient.GET[RateTypeResponse](any(), any(), any())(any(), any(), any())
      } thenReturn Future.successful(rateType)

      whenReady(connector.rateType(AlcoholByVolume(3.5), YearMonth.of(2023, 1), Set(Beer, Wine))) { result =>
        result mustBe rateType
        verify(connector.httpClient, atLeastOnce)
          .GET[RateTypeResponse](
            any(),
            ArgumentMatchers.eq(
              Seq(
                ("ratePeriod", Json.toJson(YearMonth.of(2023, 1))(RatePeriod.yearMonthFormat).toString),
                ("alcoholRegimes", Json.toJson(Set("Beer", "Wine")).toString()),
                ("abv", "3.5")
              )
            ),
            any()
          )(any(), any(), any())
      }
    }
  }

  "rateBand" - {
    "successfully retrieve rate band" in {
      val rateBandResponse: Future[Either[UpstreamErrorResponse, HttpResponse]] = Future.successful(
        Right(
          HttpResponse(OK, Json.toJson[RateBand](rateBand).toString())
        )
      )
      when {
        connector.httpClient.GET[Either[UpstreamErrorResponse, HttpResponse]](any(), any(), any())(any(), any(), any())
      } thenReturn rateBandResponse

      whenReady(connector.rateBand("310", YearMonth.of(2023, 1))) { result =>
        result mustBe Some(rateBand)
        verify(connector.httpClient, atLeastOnce)
          .GET[Either[UpstreamErrorResponse, HttpResponse]](
            any(),
            ArgumentMatchers.eq(
              Seq(
                ("ratePeriod", Json.toJson(YearMonth.of(2023, 1))(RatePeriod.yearMonthFormat).toString),
                ("taxType", "310")
              )
            ),
            any()
          )(any(), any(), any())
      }
    }

    "return None when unable to retrieve rate band" in {
      val rateBandResponse: Future[Either[UpstreamErrorResponse, HttpResponse]] = Future.successful(
        Right(
          HttpResponse(NOT_FOUND, "RateBand not found")
        )
      )
      when {
        connector.httpClient.GET[Either[UpstreamErrorResponse, HttpResponse]](any(), any(), any())(any(), any(), any())
      } thenReturn rateBandResponse

      whenReady(connector.rateBand("123", YearMonth.of(2023, 1))) { result =>
        result mustBe None
        verify(connector.httpClient, atLeastOnce)
          .GET[Either[UpstreamErrorResponse, HttpResponse]](
            any(),
            ArgumentMatchers.eq(
              Seq(
                ("ratePeriod", Json.toJson(YearMonth.of(2023, 1))(RatePeriod.yearMonthFormat).toString),
                ("taxType", "123")
              )
            ),
            any()
          )(any(), any(), any())
      }
    }

    "rateBandByRegime" - {
      "successfully retrieve rate band list given a regime" in {
        when {
          connector.httpClient.GET[Seq[RateBand]](any(), any(), any())(any(), any(), any())
        } thenReturn Future.successful(rateBandList)

        whenReady(connector.rateBandByRegime(ratePeriod = ratePeriod.period, AlcoholRegime.values)) { result =>
          result mustBe rateBandList
          verify(connector.httpClient, atLeastOnce)
            .GET[Seq[RateBand]](
              any(),
              ArgumentMatchers.eq(
                Seq(
                  ("ratePeriod", Json.toJson(ratePeriod.period)(RatePeriod.yearMonthFormat).toString),
                  ("alcoholRegimes", Json.toJson(AlcoholRegime.values).toString())
                )
              ),
              any()
            )(any(), any(), any())
        }
      }

    }
  }
}
