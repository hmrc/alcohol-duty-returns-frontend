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

package viewmodels.checkAnswers.returns

import models.{AlcoholRegime, RateBand, RateType}
import play.api.i18n.Messages

case class QuantityViewModel(
  category: String,
  id: String
)

case class HowMuchDoYouNeedToDeclareViewModel(
  core: Seq[QuantityViewModel],
  draught: Seq[QuantityViewModel]
)

object HowMuchDoYouNeedToDeclareHelper {
  def apply(regime: AlcoholRegime, rateBands: Set[RateBand])(implicit
    messages: Messages
  ): HowMuchDoYouNeedToDeclareViewModel = {
    val rateBandsByType = rateBands.toSeq
      .groupBy(_.rateType)
      .view
      .mapValues { rateBands =>
        rateBands.map { rateBand =>
          QuantityViewModel(
            category = messages(
              "howMuchDoYouNeedToDeclare.abv.interval",
              messages(s"return.regime.$regime"),
              rateBand.minABV.value,
              rateBand.maxABV.value,
              rateBand.taxType
            ).capitalize,
            id = rateBand.taxType
          )
        }
      }
      .toMap

    HowMuchDoYouNeedToDeclareViewModel(
      core = rateBandsByType.getOrElse(RateType.Core, Seq.empty),
      draught = rateBandsByType.getOrElse(RateType.DraughtRelief, Seq.empty)
    )
  }
}
