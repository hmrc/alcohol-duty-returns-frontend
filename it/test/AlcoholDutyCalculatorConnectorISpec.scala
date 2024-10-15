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

import cats.data.NonEmptySeq
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, equalToJson, get, post, urlMatching, urlPathEqualTo}
import connectors.{AdjustmentDutyCalculationRequest, AdjustmentTotalCalculationRequest, AlcoholDutyCalculatorConnector, RepackagedDutyChangeRequest, TotalDutyCalculationRequest}
import models.adjustment.{AdjustmentDuty, AdjustmentTypes}
import models.returns.{AlcoholDuty, DutyByTaxType}
import models.{ABVRange, AlcoholByVolume, AlcoholType, RangeDetailsByRegime, RateBand, RatePeriod, RateType}
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.UpstreamErrorResponse

import java.time.YearMonth


class AlcoholDutyCalculatorConnectorISpec extends ISpecBase with WireMockHelper {
  override def fakeApplication(): Application = applicationBuilder(None).configure("microservice.services.alcohol-duty-calculator.port" -> server.port()).build()

  "AlcoholDutyCalculatorConnector" - {
    "rateBandByRegime" - {
      "should successfully retrieve rateBandByRegime details" in new SetUp {
        val jsonResponse = Json.toJson(rateBands).toString()
        server.stubFor(get(urlPathEqualTo(ratesUrl))
          .withQueryParam("ratePeriod", equalTo(Json.toJson(YearMonth.of(2024,1))(RatePeriod.yearMonthFormat).toString))
          .withQueryParam("alcoholRegimes", equalTo(regime.toString))
          .willReturn(aResponse().withBody(jsonResponse)
          .withStatus(OK)))

        whenReady(connector.rateBandByRegime(YearMonth.of(2024, 1), Seq(regime))) { result =>
          result mustBe rateBands.toSeq
        }
      }

      "should fail when upstream service returns an error" in new SetUp {
        server.stubFor(get(urlPathEqualTo(ratesUrl))
          .withQueryParam("ratePeriod", equalTo(Json.toJson(YearMonth.of(2024, 1))(RatePeriod.yearMonthFormat).toString))
          .withQueryParam("alcoholRegimes", equalTo(regime.toString))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR)))

        whenReady(connector.rateBandByRegime(YearMonth.of(2024, 1), Seq(regime)).failed) { ex =>
          ex mustBe a[UpstreamErrorResponse]
          ex.asInstanceOf[UpstreamErrorResponse].statusCode mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
    "rateBand" - {
      "successfully retrieve rate band" in new SetUp{
        val jsonResponse = Json.toJson(rateBand).toString()
        server.stubFor(get(urlPathEqualTo(rateBandUrl))
          .withQueryParam("ratePeriod", equalTo(Json.toJson(YearMonth.of(2024, 1))(RatePeriod.yearMonthFormat).toString))
          .withQueryParam("taxTypeCode", equalTo("310"))
          .willReturn(aResponse().withBody(jsonResponse)
            .withStatus(OK)))

        whenReady(connector.rateBand("310", YearMonth.of(2024, 1))) { result =>
                    result mustBe Some(rateBand)
        }
      }

      "return None when rate band not found" in new SetUp{
        server.stubFor(
          get(urlMatching(rateBandUrl))
            .withQueryParam("ratePeriod", equalTo(Json.toJson(YearMonth.of(2024, 1))(RatePeriod.yearMonthFormat).toString))
            .withQueryParam("taxTypeCode", equalTo("123"))
            .willReturn(aResponse().withStatus(NOT_FOUND))
        )
        whenReady(connector.rateBand("123", ratePeriod.period)) { result =>
          result mustBe None
        }
      }

      "should fail when upstream service returns an error" in new SetUp {
        server.stubFor(get(urlPathEqualTo(rateBandUrl))
          .withQueryParam("ratePeriod", equalTo(Json.toJson(YearMonth.of(2024, 10))(RatePeriod.yearMonthFormat).toString))
          .withQueryParam("taxTypeCode", equalTo("310"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR)))
        whenReady(connector.rateBand("310", YearMonth.of(2024, 1))) { result =>
          result mustBe None
        }
      }
    }

    "calculateTotalDuty" - {
      "successfully retrieve total duty" in new SetUp{
        val volumesAndRates = arbitraryVolumeAndRateByTaxType(
          rateBands.toSeq
        ).arbitrary.sample.value

        val dutiesByTaxType = volumesAndRates.map { volumeAndRate =>
          DutyByTaxType(
            taxType = volumeAndRate.taxType,
            totalLitres = volumeAndRate.totalLitres,
            pureAlcohol = volumeAndRate.pureAlcohol,
            dutyRate = volumeAndRate.dutyRate,
            dutyDue = volumeAndRate.dutyRate * volumeAndRate.pureAlcohol
          )
        }

        val alcoholDuty = AlcoholDuty(
          dutiesByTaxType = dutiesByTaxType,
          totalDuty = dutiesByTaxType.map(_.dutyDue).sum
        )

        val requestJson = Json.toJson(TotalDutyCalculationRequest(volumesAndRates)).toString()

        val responseJson = Json.toJson(alcoholDuty).toString()
        server.stubFor(
          post(urlMatching(totalDuty))
            .withRequestBody(equalToJson(requestJson))
            .willReturn(aResponse().withBody(responseJson).withStatus(OK))
        )
        whenReady(connector.calculateTotalDuty(TotalDutyCalculationRequest(volumesAndRates))) { result =>
          result mustBe alcoholDuty
        }
      }

      "should fail when upstream service returns a bad request" in new SetUp {
        val volumesAndRates = arbitraryVolumeAndRateByTaxType(
          rateBands.toSeq
        ).arbitrary.sample.value
        val requestJson = Json.toJson(TotalDutyCalculationRequest(volumesAndRates)).toString()
        server.stubFor(post(urlMatching(totalDuty))
          .withRequestBody(equalToJson(requestJson))
          .willReturn(aResponse().withStatus(BAD_REQUEST)))
        whenReady(connector.calculateTotalDuty(TotalDutyCalculationRequest(volumesAndRates)).failed) { ex =>
          ex mustBe a[UpstreamErrorResponse]
          ex.asInstanceOf[UpstreamErrorResponse].statusCode mustBe BAD_REQUEST
        }
      }
    }

    "calculateAdjustmentDuty" - {
      "successfully retrieve adjustment duty" in new SetUp{
        val requestJson = Json.toJson(AdjustmentDutyCalculationRequest(AdjustmentTypes.Spoilt, BigDecimal(1), BigDecimal(1))).toString()
        val responseJson = Json.toJson(AdjustmentDuty(BigDecimal(1))).toString()
        server.stubFor(
          post(urlMatching(adjustmentDutyUrl))
            .withRequestBody(equalToJson(requestJson))
            .willReturn(aResponse().withBody(responseJson).withStatus(OK))
        )
        whenReady(connector.calculateAdjustmentDuty(BigDecimal(1), BigDecimal(1), AdjustmentTypes.Spoilt)) { result =>
          result mustBe AdjustmentDuty(BigDecimal(1))
        }
      }
      "should fail when upstream service returns an error" in new SetUp {
        val requestJson = Json.toJson(AdjustmentDutyCalculationRequest(AdjustmentTypes.Spoilt, BigDecimal(1), BigDecimal(1))).toString()
        server.stubFor(post(urlMatching(adjustmentDutyUrl))
          .withRequestBody(equalToJson(requestJson))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR)))
        whenReady(connector.calculateAdjustmentDuty(BigDecimal(1), BigDecimal(1), AdjustmentTypes.Spoilt).failed) { ex =>
          ex mustBe a[UpstreamErrorResponse]
          ex.asInstanceOf[UpstreamErrorResponse].statusCode mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "calculateRepackagedDutyChange" - {
      "successfully retrieve repackaged adjustment duty" in new SetUp {
        val requestJson = Json.toJson(RepackagedDutyChangeRequest(BigDecimal(2), BigDecimal(1))).toString()
        val responseJson = Json.toJson(AdjustmentDuty(BigDecimal(1))).toString()
        server.stubFor(
          post(urlMatching(repackagedUrl))
            .withRequestBody(equalToJson(requestJson))
            .willReturn(aResponse().withBody(responseJson).withStatus(OK))
        )
        whenReady(connector.calculateRepackagedDutyChange(BigDecimal(2), BigDecimal(1))) { result =>
          result mustBe AdjustmentDuty(BigDecimal(1))
        }
      }

      "should fail when upstream service returns an error" in new SetUp {
        val requestJson = Json.toJson(RepackagedDutyChangeRequest(BigDecimal(2), BigDecimal(1))).toString()
        server.stubFor(post(urlMatching(repackagedUrl))
          .withRequestBody(equalToJson(requestJson))
          .willReturn(aResponse().withStatus(BAD_REQUEST)))
        whenReady(connector.calculateRepackagedDutyChange(BigDecimal(2), BigDecimal(1)).failed) { ex =>
          ex mustBe a[UpstreamErrorResponse]
          ex.asInstanceOf[UpstreamErrorResponse].statusCode mustBe BAD_REQUEST
        }
      }
    }

    "calculateTotalAdjustment" - {
      "successfully retrieve total adjustment duties" in new SetUp {
        val requestJson = Json.toJson(AdjustmentTotalCalculationRequest(Seq(BigDecimal(2), BigDecimal(1)))).toString()
        val responseJson = Json.toJson(AdjustmentDuty(BigDecimal(3))).toString()
        server.stubFor(
          post(urlMatching(totalAdjustmentUrl))
            .withRequestBody(equalToJson(requestJson))
            .willReturn(aResponse().withBody(responseJson).withStatus(OK))
        )
        whenReady(connector.calculateTotalAdjustment(Seq(BigDecimal(2), BigDecimal(1)))) { result =>
          result mustBe AdjustmentDuty(BigDecimal(3))
        }
      }
      "should fail when upstream service returns an error" in new SetUp {
        val requestJson = Json.toJson(AdjustmentTotalCalculationRequest(Seq(BigDecimal(2), BigDecimal(1)))).toString()
        server.stubFor(post(urlMatching(totalAdjustmentUrl))
          .withRequestBody(equalToJson(requestJson))
          .willReturn(aResponse().withStatus(BAD_REQUEST)))
        whenReady(connector.calculateTotalAdjustment(Seq(BigDecimal(2), BigDecimal(1))).failed) { ex =>
          ex mustBe a[UpstreamErrorResponse]
          ex.asInstanceOf[UpstreamErrorResponse].statusCode mustBe BAD_REQUEST
        }
      }
    }
  }

  class SetUp {
    val connector = app.injector.instanceOf[AlcoholDutyCalculatorConnector]
    val url = "/alcohol-duty-calculator"
    val ratePeriod = returnPeriodGen.sample.get
    val regime = regimeGen.sample.value
    val rateBand = RateBand(
      "310",
      "some band",
      RateType.DraughtRelief,
      Some(BigDecimal(10.99)),
      Set(
        RangeDetailsByRegime(
         regime,
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
    val rateBands = genListOfRateBandForRegime(regime).sample.value.toSet
    val repackagedUrl = s"$url/calculate-repackaged-duty-change"
    val totalAdjustmentUrl = s"$url/calculate-total-adjustment"
    val adjustmentDutyUrl = s"$url/calculate-adjustment-duty"
    val totalDuty = s"$url/calculate-total-duty"
    val rateBandUrl = s"$url/rate-band"
    val ratesUrl = s"$url/rates"
  }
}
