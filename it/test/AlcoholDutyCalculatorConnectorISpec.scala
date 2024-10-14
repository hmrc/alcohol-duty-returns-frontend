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
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, RangeDetailsByRegime, RateBand, RatePeriod, RateType}
import play.api.Application
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.Json

import java.time.YearMonth


class AlcoholDutyCalculatorConnectorISpec extends ISpecBase with WireMockHelper {
  override def fakeApplication(): Application = applicationBuilder(None).configure("microservice.services.alcohol-duty-calculator.port" -> server.port()).build()

  "AlcoholDutyCalculatorConnector" - {
    "rateBandByRegime" - {
      "should successfully retrieve rateBandByRegime details" in new SetUp {
        val jsonResponse = Json.toJson(rateBands).toString()
        server.stubFor(get(urlPathEqualTo("/alcohol-duty-calculator/rates"))
          .withQueryParam("ratePeriod", equalTo(Json.toJson(YearMonth.of(2024,1))(RatePeriod.yearMonthFormat).toString))
          .withQueryParam("alcoholRegimes", equalTo("Beer,Cider,Wine,Spirits,OtherFermentedProduct"))
          .willReturn(aResponse().withBody(jsonResponse)
          .withStatus(OK)))

        val regimes = AlcoholRegime.values
        whenReady(connector.rateBandByRegime(YearMonth.of(2024, 1), regimes)) { result =>
                    result mustBe rateBands.toSeq
                  }
      }
    }
    "rateBand" - {
      "successfully retrieve rate band" in new SetUp{
        val jsonResponse = Json.toJson(rateBand).toString()
        server.stubFor(get(urlPathEqualTo("/alcohol-duty-calculator/rate-band"))
          .withQueryParam("ratePeriod", equalTo(Json.toJson(YearMonth.of(2024, 10))(RatePeriod.yearMonthFormat).toString))
          .withQueryParam("taxTypeCode", equalTo("310"))
          .willReturn(aResponse().withBody(jsonResponse)
            .withStatus(OK)))

        whenReady(connector.rateBand("310", YearMonth.of(2024, 10))) { result =>
                    result mustBe Some(rateBand)
        }
      }

      "return None when rate band not found" in new SetUp{
        server.stubFor(
          get(urlMatching(s"$url/rate-band"))
            .withQueryParam("ratePeriod", equalTo(Json.toJson(YearMonth.of(2024, 10))(RatePeriod.yearMonthFormat).toString))
            .withQueryParam("taxTypeCode", equalTo("123"))
            .willReturn(aResponse().withStatus(NOT_FOUND))
        )
        whenReady(connector.rateBand("123", ratePeriod.period)) { result =>
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
          post(urlMatching(s"$url/calculate-total-duty"))
            .withRequestBody(equalToJson(requestJson))
            .willReturn(aResponse().withBody(responseJson).withStatus(OK))
        )
        whenReady(connector.calculateTotalDuty(TotalDutyCalculationRequest(volumesAndRates))) { result =>
          result mustBe alcoholDuty
        }
      }
    }

    "calculateAdjustmentDuty" - {
      "successfully retrieve adjustment duty" in new SetUp{
        val requestJson = Json.toJson(AdjustmentDutyCalculationRequest(AdjustmentTypes.Spoilt, BigDecimal(1), BigDecimal(1))).toString()
        val responseJson = Json.toJson(AdjustmentDuty(BigDecimal(1))).toString()
        server.stubFor(
          post(urlMatching(s"$url/calculate-adjustment-duty"))
            .withRequestBody(equalToJson(requestJson))
            .willReturn(aResponse().withBody(responseJson).withStatus(OK))
        )
        whenReady(connector.calculateAdjustmentDuty(BigDecimal(1), BigDecimal(1), AdjustmentTypes.Spoilt)) { result =>
          result mustBe AdjustmentDuty(BigDecimal(1))
        }
      }
    }

    "calculateRepackagedDutyChange" - {
      "successfully retrieve repackaged adjustment duty" in new SetUp {
        val requestJson = Json.toJson(RepackagedDutyChangeRequest(BigDecimal(2), BigDecimal(1))).toString()
        val responseJson = Json.toJson(AdjustmentDuty(BigDecimal(1))).toString()
        server.stubFor(
          post(urlMatching(s"$url/calculate-repackaged-duty-change"))
            .withRequestBody(equalToJson(requestJson))
            .willReturn(aResponse().withBody(responseJson).withStatus(OK))
        )
        whenReady(connector.calculateRepackagedDutyChange(BigDecimal(2), BigDecimal(1))) { result =>
          result mustBe AdjustmentDuty(BigDecimal(1))
        }
      }
    }

    "calculateTotalAdjustment" - {
      "successfully retrieve total adjustment duties" in new SetUp {
        val requestJson = Json.toJson(AdjustmentTotalCalculationRequest(Seq(BigDecimal(2), BigDecimal(1)))).toString()
        val responseJson = Json.toJson(AdjustmentDuty(BigDecimal(3))).toString()
        server.stubFor(
          post(urlMatching(s"$url/calculate-total-adjustment"))
            .withRequestBody(equalToJson(requestJson))
            .willReturn(aResponse().withBody(responseJson).withStatus(OK))
        )
        whenReady(connector.calculateTotalAdjustment(Seq(BigDecimal(2), BigDecimal(1)))) { result =>
          result mustBe AdjustmentDuty(BigDecimal(3))
        }
      }
    }
  }

  class SetUp {
    val connector = app.injector.instanceOf[AlcoholDutyCalculatorConnector]
    val url = "/alcohol-duty-calculator"
    val ratePeriod = returnPeriodGen.sample.get
    val rateBand = RateBand(
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
    val regime = regimeGen.sample.value
    val rateBands = genListOfRateBandForRegime(regime).sample.value.toSet
  }
}
