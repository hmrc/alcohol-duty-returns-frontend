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

package viewmodels.declareDuty

import models.{AlcoholRegime, RateBand, RateType}
import play.api.i18n.Messages

case class CategoryViewModel(
  category: String,
  id: String,
  rateBandRecap: String,
  rateBandRecapSoftMutation: String
)

case class CategoriesByRateTypeViewModel(
  core: Seq[CategoryViewModel],
  draught: Seq[CategoryViewModel],
  smallProducer: Seq[CategoryViewModel],
  draughtAndSmallProducer: Seq[CategoryViewModel]
)

object CategoriesByRateTypeHelper {
  def rateBandCategories(rateBands: Set[RateBand], regime: AlcoholRegime, isRecap: Boolean = false)(implicit
    messages: Messages
  ): CategoriesByRateTypeViewModel = {
    val rateBandsByType = rateBands.toSeq
      .groupBy(_.rateType)
      .view
      .mapValues { rateBands =>
        rateBands.sortBy(_.taxTypeCode).map { rateBand =>
          CategoryViewModel(
            category =
              if (isRecap) RateBandHelper.rateBandRecap(rateBand, Some(regime))
              else RateBandHelper.rateBandContent(rateBand, Some(regime)),
            id = rateBand.taxTypeCode,
            rateBandRecap = RateBandHelper.rateBandRecap(rateBand, Some(regime)),
            rateBandRecapSoftMutation = RateBandHelper.rateBandRecap(rateBand, Some(regime), true)
          )
        }
      }
      .toMap

    CategoriesByRateTypeViewModel(
      core = rateBandsByType.getOrElse(RateType.Core, Seq.empty),
      draught = rateBandsByType.getOrElse(RateType.DraughtRelief, Seq.empty),
      smallProducer = rateBandsByType.getOrElse(RateType.SmallProducerRelief, Seq.empty),
      draughtAndSmallProducer = rateBandsByType.getOrElse(RateType.DraughtAndSmallProducerRelief, Seq.empty)
    )
  }
}
