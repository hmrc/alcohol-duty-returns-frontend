/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.adjustment

import base.SpecBase
import cats.data.NonEmptySeq
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.RateType.Core
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholRegimes, AlcoholType, RangeDetailsByRegime, RateBand}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import viewmodels.adjustment.SpoiltAlcoholicProductTypeViewModel
import viewmodels.checkAnswers.adjustment.SpoiltAlcoholicProductTypeHelper

class SpoiltAlcoholicProductTypeHelperSpec extends SpecBase {
  "SpoiltAlcoholicProductTypeHelper" - {
    "createRateBandFromRegime" -
      AlcoholRegime.values.foreach { regime =>
        s"create an appropriate rate band for the $regime regime" in new SetUp {
          val expectedRateBand = RateBand(
            expectedRegimeToTaxType(regime),
            messages(regime.regimeMessageKey),
            Core,
            Some(expectedSpoiltRate),
            Set(
              RangeDetailsByRegime(
                regime,
                NonEmptySeq.one(
                  ABVRange(
                    AlcoholType.fromAlcoholRegime(regime),
                    AlcoholByVolume(expectedRangeMinABV),
                    AlcoholByVolume(expectedRangeMaxABV)
                  )
                )
              )
            )
          )

          spoiltAlcoholicProductTypeHelper.createRateBandFromRegime(regime) mustBe expectedRateBand
        }
      }

    "getViewModel" - {
      "create a view model containing radio options in view order for a set of regimes" in new SetUp {
        val viewModel: SpoiltAlcoholicProductTypeViewModel =
          spoiltAlcoholicProductTypeHelper.getViewModel(AlcoholRegimes(Set(Cider, OtherFermentedProduct, Beer)))

        viewModel.radioOptions mustBe Seq(
          RadioItem(Text("Beer"), Some("Beer"), Some("Beer")),
          RadioItem(Text("Cider"), Some("Cider"), Some("Cider")),
          RadioItem(
            Text("Other fermented products"),
            Some("OtherFermentedProduct"),
            Some("OtherFermentedProduct")
          )
        )
      }
    }
  }

  class SetUp {
    implicit val messages: Messages = getMessages(app)

    val expectedRegimeToTaxType: Map[AlcoholRegime, String] =
      Map(Beer -> "356", Wine -> "333", Cider -> "357", Spirits -> "345", OtherFermentedProduct -> "324")

    val expectedSpoiltRate: BigDecimal  = BigDecimal(0.01)
    val expectedRangeMinABV: BigDecimal = BigDecimal(0)
    val expectedRangeMaxABV: BigDecimal = BigDecimal(100)

    val spoiltAlcoholicProductTypeHelper = new SpoiltAlcoholicProductTypeHelper(appConfig)
  }
}
