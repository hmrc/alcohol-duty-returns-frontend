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

import models.{AlcoholRegimeName, RateBand}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

object TellUsAboutMultipleSPRRateHelper {
  def radioItems(rateBands: Set[RateBand])(implicit messages: Messages): Seq[RadioItem] = {
    val categoryViewModels                = CategoriesByRateTypeHelper.rateBandCategories(rateBands, isRecap = true)
    val smallProducerRadioItems           = categoryViewModels.smallProducer
      .map { category =>
        RadioItem(content = Text(category.category), value = Some(category.id))
      }
      .sortBy(_.id)
    val draughtAndSmallProducerRadioItems = categoryViewModels.draughtAndSmallProducer
      .map { category =>
        RadioItem(content = Text(category.category), value = Some(category.id))
      }
      .sortBy(_.id)

    smallProducerRadioItems ++ draughtAndSmallProducerRadioItems
  }
}
