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
import models.{ABVRange, AlcoholByVolume, AlcoholType, RangeDetailsByRegime, RateBand, RateType}
import models.RatePeriod._
import models.declareDuty.{AlcoholDuty, DutyByTaxType}
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
          .withQueryParam("ratePeriod", equalTo(Json.toJson(ratePeriod).toString))
          .withQueryParam("alcoholRegimes", equalTo(regime.toString))
          .willReturn(aResponse().withBody(jsonResponse)
          .withStatus(OK)))

        whenReady(connector.rateBandByRegime(ratePeriod, Seq(regime))) { result =>
          result mustBe rateBands.toSeq
        }
      }

      "should fail when upstream service returns an error" in new SetUp {
        server.stubFor(get(urlPathEqualTo(ratesUrl))
          .withQueryParam("ratePeriod", equalTo(Json.toJson(ratePeriod).toString))
          .withQueryParam("alcoholRegimes", equalTo(regime.toString))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR)))

        whenReady(connector.rateBandByRegime(ratePeriod, Seq(regime)).failed) { ex =>
          ex mustBe a[UpstreamErrorResponse]
          ex.asInstanceOf[UpstreamErrorResponse].statusCode mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "rateBand" - {
      "successfully retrieve rate band" in new SetUp{
        val jsonResponse = Json.toJson(rateBand).toString()
        server.stubFor(get(urlPathEqualTo(rateBandUrl))
          .withQueryParam("ratePeriod", equalTo(Json.toJson(ratePeriod).toString))
          .withQueryParam("taxTypeCode", equalTo(taxTypeCode))
          .willReturn(aResponse().withBody(jsonResponse)
            .withStatus(OK)))

        whenReady(connector.rateBand(taxTypeCode, ratePeriod)) { result =>
                    result mustBe Some(rateBand)
        }
      }

      "return None when rate band not found" in new SetUp{
        server.stubFor(
          get(urlMatching(rateBandUrl))
            .withQueryParam("ratePeriod", equalTo(Json.toJson(ratePeriod).toString))
            .withQueryParam("taxTypeCode", equalTo(taxTypeCode))
            .willReturn(aResponse().withStatus(NOT_FOUND))
        )
        whenReady(connector.rateBand(taxTypeCode, ratePeriod)) { result =>
          result mustBe None
        }
      }
    }

    "rateBands" - {
      "successfully retrieve rates band" in new SetUp{
        val jsonResponse = Json.toJson(periodTaxTypeCodeToRateBands).toString()
        server.stubFor(get(urlPathEqualTo(rateBandsUrl))
          .withQueryParam("ratePeriods", equalTo(Seq(Json.toJson(ratePeriod).toString, Json.toJson(ratePeriod2).toString).mkString(",")))
          .withQueryParam("taxTypeCodes", equalTo(s"$taxTypeCode,$taxTypeCode2"))
          .willReturn(aResponse().withBody(jsonResponse)
            .withStatus(OK)))

        whenReady(connector.rateBands(Seq((ratePeriod, taxTypeCode), (ratePeriod2, taxTypeCode2)))) { result =>
          result mustBe periodTaxTypeCodeToRateBands
        }
      }

      "return an empty map when a bad request" in new SetUp{
        server.stubFor(
          get(urlMatching(rateBandsUrl))
            .withQueryParam("ratePeriods", equalTo(Seq(Json.toJson(ratePeriod).toString, Json.toJson(ratePeriod2).toString).mkString(",")))
            .withQueryParam("taxTypeCodes", equalTo(s"$taxTypeCode,$taxTypeCode2"))
            .willReturn(aResponse().withStatus(BAD_REQUEST))
        )
        whenReady(connector.rateBands(Seq((ratePeriod, taxTypeCode), (ratePeriod2, taxTypeCode2)))) { result =>
          result mustBe Map.empty
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
    val regime = regimeGen.sample.value

    val ratePeriod = YearMonth.of(2024, 1)
    val ratePeriod2 = YearMonth.of(2024, 2)
    val taxTypeCode = "310"
    val taxTypeCode2 = "311"

    val rateBand = RateBand(
      taxTypeCode,
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

    val rateBand2 = RateBand(
      taxTypeCode2,
      "some band2",
      RateType.Core,
      Some(BigDecimal(20.99)),
      Set(
        RangeDetailsByRegime(
          regime,
          NonEmptySeq.one(
            ABVRange(
              AlcoholType.Beer,
              AlcoholByVolume(2.1),
              AlcoholByVolume(5.8)
            )
          )
        )
      )
    )

    val periodTaxTypeCodeToRateBands = Map((ratePeriod, taxTypeCode) -> rateBand, (ratePeriod2, taxTypeCode2) -> rateBand2)

    val rateBands = genListOfRateBandForRegime(regime).sample.value.toSet
    val repackagedUrl = s"$url/calculate-repackaged-duty-change"
    val totalAdjustmentUrl = s"$url/calculate-total-adjustment"
    val adjustmentDutyUrl = s"$url/calculate-adjustment-duty"
    val totalDuty = s"$url/calculate-total-duty"
    val rateBandUrl = s"$url/rate-band"
    val rateBandsUrl = s"$url/rate-bands"
    val ratesUrl = s"$url/rates"
  }
}
