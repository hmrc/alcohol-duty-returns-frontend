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

import models.{AlcoholRegime, RateBand}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

import javax.inject.Inject

class TellUsAboutMultipleSPRRateHelper @Inject() (categoriesByRateTypeHelper: CategoriesByRateTypeHelper) {
  def radioItems(rateBands: Set[RateBand], regime: AlcoholRegime)(implicit messages: Messages): Seq[RadioItem] = {
    val categoryViewModels                = categoriesByRateTypeHelper.rateBandCategories(rateBands, regime)
    val smallProducerRadioItems           = categoryViewModels.smallProducer
      .map { category =>
        RadioItem(content = Text(category.description.capitalize), value = Some(category.taxTypeCode))
      }
      .sortBy(_.id)
    val draughtAndSmallProducerRadioItems = categoryViewModels.draughtAndSmallProducer
      .map { category =>
        RadioItem(content = Text(category.description.capitalize), value = Some(category.taxTypeCode))
      }
      .sortBy(_.id)

    smallProducerRadioItems ++ draughtAndSmallProducerRadioItems
  }
}
