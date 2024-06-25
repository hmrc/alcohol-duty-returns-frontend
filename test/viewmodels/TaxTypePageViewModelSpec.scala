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

package viewmodels

import base.SpecBase
import generators.ModelGenerators
import models.{AlcoholByVolume, AlcoholRegime, RateBand, RateType}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{RadioItem, Text}

class TaxTypePageViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  val application: Application    = applicationBuilder().build()
  implicit val messages: Messages = getMessages(application)

  "TaxTypePageViewModel apply" - {
    "should return TaxTypePageViewModel with Radio items" - {
      "when provided with all the parameters and a list of rate band list" in {
        val rateBandList =
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

        TaxTypePageViewModel(
          AlcoholByVolume(10.1),
          eligibleForDraughtRelief = true,
          eligibleForSmallProducerRelief = false,
          rateBandList
        ) mustBe
          TaxTypePageViewModel(
            s"10.1${messages("site.unit.percentage")}",
            eligibleForDraughtRelief = true,
            eligibleForSmallProducerRelief = false,
            List(
              RadioItem(
                Text("Beer, tax type code 310"),
                Some("310_Beer"),
                Some("310_Beer"),
                None,
                None,
                None,
                checked = false,
                None,
                disabled = false,
                Map()
              )
            )
          )
      }
    }
    "should return TaxTypePageViewModel without Radio items" - {
      "when provided with all the parameters with empty radio items" in {

        TaxTypePageViewModel(
          AlcoholByVolume(10.1),
          eligibleForDraughtRelief = false,
          eligibleForSmallProducerRelief = true,
          Seq.empty
        ) mustBe
          TaxTypePageViewModel(
            s"10.1${messages("site.unit.percentage")}",
            eligibleForDraughtRelief = false,
            eligibleForSmallProducerRelief = true,
            Seq.empty
          )
      }
    }
  }
}
