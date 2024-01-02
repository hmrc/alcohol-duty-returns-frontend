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
import config.FrontendAppConfig
import models.{AlcoholByVolume, AlcoholRegime, RateBand, RatePeriod, RateType}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import java.time.YearMonth
import scala.concurrent.{ExecutionContext, Future}

class AlcoholDutyCalculatorConnectorSpec extends SpecBase with ScalaFutures {

  protected implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  protected implicit val hc: HeaderCarrier    = HeaderCarrier()
  val mockConfig: FrontendAppConfig           = mock[FrontendAppConfig]
  val connector                               = new AlcoholDutyCalculatorConnector(config = mockConfig, httpClient = mock[HttpClient])
  val ratePeriodList: Seq[RatePeriod]         = Seq(
    RatePeriod(
      "period1",
      isLatest = true,
      YearMonth.of(2023, 1),
      None,
      Seq(
        RateBand(
          "310",
          "some band",
          RateType.DraughtRelief,
          Set(AlcoholRegime.Beer),
          AlcoholByVolume(0.1),
          AlcoholByVolume(5.8),
          Some(BigDecimal(10.99))
        )
      )
    )
  )

  "rates" - {
    "successfully retrieve rates" in {

      when {
        connector.httpClient.GET[Seq[RatePeriod]](any(), any(), any())(any(), any(), any())
      } thenReturn Future.successful(ratePeriodList)

      whenReady(connector.rates()) {
        _ mustBe ratePeriodList
      }
    }
  }
}
