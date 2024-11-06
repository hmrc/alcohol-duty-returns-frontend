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
import models.adjustment.{AdjustmentDuty, AdjustmentTypes}
import models.declareDuty.{AlcoholDuty, DutyByTaxType}
import models.{ABVRange, AdjustmentDutyCalculationRequest, AdjustmentTotalCalculationRequest, AlcoholByVolume, AlcoholRegime, AlcoholType, RangeDetailsByRegime, RateBand, RatePeriod, RateType, RepackagedDutyChangeRequest, TotalDutyCalculationRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HttpResponse, StringContextOps, UpstreamErrorResponse}

import java.time.YearMonth
import scala.concurrent.Future

class AlcoholDutyCalculatorConnectorSpec extends SpecBase {

  "rateBand" - {

    val mockUrl = "http://alcohol-duty-calculator/rate-band"

    "successfully retrieve rate band" in new SetUp {
      when(mockConfig.adrCalculatorRateBandUrl()).thenReturn(mockUrl)
      val queryParams = Seq(
        "ratePeriod"  -> Json.toJson(YearMonth.of(2023, 1))(RatePeriod.yearMonthFormat).toString,
        "taxTypeCode" -> "310"
      )

      val rateBandResponse = HttpResponse(OK, Json.toJson[RateBand](rateBand).toString())

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(rateBandResponse)))

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.rateBand("310", YearMonth.of(2023, 1))) { result =>
        result mustBe Some(rateBand)
        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl?$queryParams"))(any())
        verify(requestBuilder, atLeastOnce)
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }

    "return None when unable to retrieve rate band" in new SetUp {
      when(mockConfig.adrCalculatorRateBandUrl()).thenReturn(mockUrl)
      val queryParams: Seq[(String, String)] = Seq(
        "ratePeriod"  -> Json.toJson(YearMonth.of(2023, 1))(RatePeriod.yearMonthFormat).toString,
        "taxTypeCode" -> "123"
      )

      val rateBandResponse = HttpResponse(NOT_FOUND, "RateBand not found")

      when(requestBuilder.execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any()))
        .thenReturn(Future.successful(Right(rateBandResponse)))

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.rateBand("123", YearMonth.of(2023, 1))) { result =>
        result mustBe None
        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl?$queryParams"))(any())
        verify(requestBuilder, atLeastOnce)
          .execute[Either[UpstreamErrorResponse, HttpResponse]](any(), any())
      }
    }
  }

  "calculateTotalDuty" - {
    "successfully retrieve total duty" in new SetUp {
      val regime          = regimeGen.sample.value
      val rateBands       = genListOfRateBandForRegime(regime).sample.value.toSet
      val volumesAndRates = arbitraryVolumeAndRateByTaxType(
        rateBands.toSeq
      ).arbitrary.sample.value

      val dutiesByTaxType = volumesAndRates.map { volumeAndRate =>
        val totalDuty = volumeAndRate.dutyRate * volumeAndRate.pureAlcohol
        DutyByTaxType(
          taxType = volumeAndRate.taxType,
          totalLitres = volumeAndRate.totalLitres,
          pureAlcohol = volumeAndRate.pureAlcohol,
          dutyRate = volumeAndRate.dutyRate,
          dutyDue = totalDuty
        )
      }

      val alcoholDuty = AlcoholDuty(
        dutiesByTaxType = dutiesByTaxType,
        totalDuty = dutiesByTaxType.map(_.dutyDue).sum
      )

      val request = TotalDutyCalculationRequest(volumesAndRates)
      val mockUrl = "http://alcohol-duty-calculator/calculate-total-duty"
      when(mockConfig.adrCalculatorCalculateTotalDutyUrl()).thenReturn(mockUrl)

      when(requestBuilder.execute[AlcoholDuty](any(), any()))
        .thenReturn(Future.successful(alcoholDuty))

      when(
        requestBuilder.withBody(
          eqTo(Json.toJson(request))
        )(any(), any(), any())
      )
        .thenReturn(requestBuilder)

      when(connector.httpClient.post(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.calculateTotalDuty(request)) { result =>
        result mustBe alcoholDuty
        verify(connector.httpClient, atLeastOnce)
          .post(eqTo(url"$mockUrl"))(any())
      }
    }
  }

  "rateBandByRegime" - {
    val queryParams: Seq[(String, String)] = Seq(
      "ratePeriod"     -> Json.toJson(returnPeriod.period)(RatePeriod.yearMonthFormat).toString,
      "alcoholRegimes" -> AlcoholRegime.values.mkString(",")
    )

    val mockUrl = "http://alcohol-duty-calculator/rates"

    "successfully retrieve rate band list given a regime" in new SetUp {
      when(mockConfig.adrCalculatorRatesUrl()).thenReturn(mockUrl)
      when(requestBuilder.execute[Seq[RateBand]](any(), any()))
        .thenReturn(Future.successful(rateBandList))

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)

      whenReady(connector.rateBandByRegime(ratePeriod = returnPeriod.period, AlcoholRegime.values)) { result =>
        result mustBe rateBandList
        verify(connector.httpClient, times(1))
          .get(eqTo(url"$mockUrl?$queryParams"))(any())
        verify(requestBuilder, atLeastOnce)
          .execute[Seq[RateBand]](any(), any())
      }
    }

    "calculateAdjustmentDuty" - {
      "successfully retrieve adjustment duty" in new SetUp {
        val mockUrl = "http://alcohol-duty-calculator/calculate-adjustment-duty"
        when(mockConfig.adrCalculatorCalculateAdjustmentDutyUrl()).thenReturn(mockUrl)

        when(requestBuilder.execute[AdjustmentDuty](any(), any()))
          .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(1))))

        when(
          requestBuilder.withBody(
            eqTo(Json.toJson(AdjustmentDutyCalculationRequest(AdjustmentTypes.Spoilt, BigDecimal(1), BigDecimal(1))))
          )(any(), any(), any())
        )
          .thenReturn(requestBuilder)

        when(connector.httpClient.post(any())(any())).thenReturn(requestBuilder)

        whenReady(connector.calculateAdjustmentDuty(BigDecimal(1), BigDecimal(1), AdjustmentTypes.Spoilt)) { result =>
          result mustBe AdjustmentDuty(BigDecimal(1))
          verify(connector.httpClient, atLeastOnce)
            .post(eqTo(url"$mockUrl"))(any())
        }
      }
    }

    "calculateRepackagedDutyChange" - {
      "successfully retrieve adjustment duty" in new SetUp {
        val mockUrl = "http://alcohol-duty-calculator/calculate-repackaged-duty-change"
        when(mockConfig.adrCalculatorCalculateRepackagedDutyChangeUrl()).thenReturn(mockUrl)

        when(requestBuilder.execute[AdjustmentDuty](any(), any()))
          .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(1))))

        when(
          requestBuilder.withBody(
            eqTo(Json.toJson(RepackagedDutyChangeRequest(BigDecimal(2), BigDecimal(1))))
          )(any(), any(), any())
        )
          .thenReturn(requestBuilder)

        when(connector.httpClient.post(any())(any())).thenReturn(requestBuilder)

        when(requestBuilder.execute[AdjustmentDuty](any(), any()))
          .thenReturn(Future.successful(AdjustmentDuty(1)))

        whenReady(connector.calculateRepackagedDutyChange(BigDecimal(2), BigDecimal(1))) { result =>
          result mustBe AdjustmentDuty(BigDecimal(1))
          verify(connector.httpClient, atLeastOnce)
            .post(eqTo(url"$mockUrl"))(any())
        }
      }
    }

    "calculateTotalAdjustment" - {
      "successfully adjustment duty" in new SetUp {
        val mockUrl = "http://alcohol-duty-calculator/calculate-total-adjustment"
        when(mockConfig.adrCalculatorCalculateTotalAdjustmentUrl()).thenReturn(mockUrl)

        when(requestBuilder.execute[AdjustmentDuty](any(), any()))
          .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(10))))

        when(
          requestBuilder.withBody(
            eqTo(Json.toJson(AdjustmentTotalCalculationRequest(Seq(BigDecimal(8), BigDecimal(1), BigDecimal(1)))))
          )(any(), any(), any())
        )
          .thenReturn(requestBuilder)

        when(connector.httpClient.post(any())(any())).thenReturn(requestBuilder)

        whenReady(connector.calculateTotalAdjustment(Seq(BigDecimal(8), BigDecimal(1), BigDecimal(1)))) { result =>
          result mustBe AdjustmentDuty(BigDecimal(10))
          verify(connector.httpClient, atLeastOnce)
            .post(eqTo(url"$mockUrl"))(any())
        }
      }
    }
  }

  class SetUp {
    val mockConfig: FrontendAppConfig  = mock[FrontendAppConfig]
    val connector                      = new AlcoholDutyCalculatorConnector(config = mockConfig, httpClient = mock[HttpClientV2])
    val rateBand                       = RateBand(
      "310",
      "some band",
      RateType.DraughtRelief,
      Some(BigDecimal(10.99)),
      Set(
        RangeDetailsByRegime(
          AlcoholRegime.Beer,
          NonEmptySeq.one(
            ABVRange(
              AlcoholType.Beer,
              AlcoholByVolume(0.1),
              AlcoholByVolume(5.8)
            )
          )
        )
      )
    )
    val rateBandList: Seq[RateBand]    = Seq(rateBand)
    val ratePeriod                     = returnPeriodGen.sample.get
    val requestBuilder: RequestBuilder = mock[RequestBuilder]
  }
}
