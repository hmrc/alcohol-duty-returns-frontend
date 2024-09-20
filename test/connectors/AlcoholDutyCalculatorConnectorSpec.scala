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
import models.AlcoholRegime.{Beer, Wine}
import models.RateType.DraughtRelief
import models.adjustment.{AdjustmentDuty, AdjustmentTypes}
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, RangeDetailsByRegime, RateBand, RatePeriod, RateType}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HttpResponse, UpstreamErrorResponse}

import java.time.YearMonth
import scala.concurrent.Future

class AlcoholDutyCalculatorConnectorSpec extends SpecBase {


  val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val connector                     = new AlcoholDutyCalculatorConnector(config = mockConfig, httpClient = mock[HttpClientV2])
  val rateBand                      = RateBand(
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
  val rateBandList: Seq[RateBand]   = Seq(rateBand)
  val ratePeriod                    = returnPeriodGen.sample.get

  "rates" - {
    "successfully retrieve rates" in {
      when(requestBuilder.execute[Seq[RateBand]](any(), any()))
        .thenReturn(Future.successful(rateBandList))

      when {
        connector.httpClient
          .get(any())(any())
      } thenReturn requestBuilder

      whenReady(connector.rates(DraughtRelief, AlcoholByVolume(3.5), YearMonth.of(2023, 1), Set(Beer, Wine))) {
        result =>
          result mustBe rateBandList
//          verify(connector.httpClient, times(1))
//            .get(eqTo(url"$mockUrl"))(any())
          verify(requestBuilder, times(1))
            .execute[Seq[RateBand]](any(), any())
//              ArgumentMatchers.eq(
//                Seq(
//                  ("ratePeriod", Json.toJson(YearMonth.of(2023, 1))(RatePeriod.yearMonthFormat).toString),
//                  ("alcoholRegimes", Json.toJson(Set("Beer", "Wine")).toString()),
//                  ("rateType", Json.toJson[RateType](DraughtRelief).toString),
//                  ("abv", "3.5")
//                )
//            ), any())(any())
      }
    }
  }
  /*
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
                ("taxTypeCode", "310")
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
                ("taxTypeCode", "123")
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

        whenReady(connector.rateBandByRegime(ratePeriod = returnPeriod.period, AlcoholRegime.values)) { result =>
          result mustBe rateBandList
          verify(connector.httpClient, atLeastOnce)
            .GET[Seq[RateBand]](
              any(),
              ArgumentMatchers.eq(
                Seq(
                  ("ratePeriod", Json.toJson(returnPeriod.period)(RatePeriod.yearMonthFormat).toString),
                  ("alcoholRegimes", Json.toJson(AlcoholRegime.values).toString())
                )
              ),
              any()
            )(any(), any(), any())
        }
      }

    }
  }

  "calculateAdjustmentDuty" - {
    "successfully retrieve adjustment duty" in {
      when {
        connector.httpClient
          .POST[AdjustmentDutyCalculationRequest, AdjustmentDuty](any(), any(), any())(any(), any(), any(), any())
      } thenReturn Future.successful(AdjustmentDuty(BigDecimal(1)))

      whenReady(connector.calculateAdjustmentDuty(BigDecimal(1), BigDecimal(1), AdjustmentTypes.Spoilt)) { result =>
        result mustBe AdjustmentDuty(BigDecimal(1))
        verify(connector.httpClient, atLeastOnce)
          .POST[AdjustmentDutyCalculationRequest, AdjustmentDuty](
            any(),
            ArgumentMatchers.eq(AdjustmentDutyCalculationRequest(AdjustmentTypes.Spoilt, BigDecimal(1), BigDecimal(1))),
            any()
          )(any(), any(), any(), any())
      }
    }
  }

  "calculateRepackagedDutyChange" - {
    "successfully retrieve adjustment duty" in {
      when {
        connector.httpClient
          .POST[RepackagedDutyChangeRequest, AdjustmentDuty](any(), any(), any())(any(), any(), any(), any())
      } thenReturn Future.successful(AdjustmentDuty(BigDecimal(1)))

      whenReady(connector.calculateRepackagedDutyChange(BigDecimal(2), BigDecimal(1))) { result =>
        result mustBe AdjustmentDuty(BigDecimal(1))
        verify(connector.httpClient, atLeastOnce)
          .POST[RepackagedDutyChangeRequest, AdjustmentDuty](
            any(),
            ArgumentMatchers.eq(RepackagedDutyChangeRequest(BigDecimal(2), BigDecimal(1))),
            any()
          )(any(), any(), any(), any())
      }
    }
  }

  "calculateTotalAdjustment" - {
    "successfully adjustment duty" in {
      when {
        connector.httpClient
          .POST[AdjustmentTotalCalculationRequest, AdjustmentDuty](any(), any(), any())(any(), any(), any(), any())
      } thenReturn Future.successful(AdjustmentDuty(BigDecimal(10)))

      whenReady(connector.calculateTotalAdjustment(Seq(BigDecimal(8), BigDecimal(1), BigDecimal(1)))) { result =>
        result mustBe AdjustmentDuty(BigDecimal(10))
        verify(connector.httpClient, atLeastOnce)
          .POST[AdjustmentTotalCalculationRequest, AdjustmentDuty](
            any(),
            ArgumentMatchers.eq(AdjustmentTotalCalculationRequest(Seq(BigDecimal(8), BigDecimal(1), BigDecimal(1)))),
            any()
          )(any(), any(), any(), any())
      }
    }
  }*/

   */
}
