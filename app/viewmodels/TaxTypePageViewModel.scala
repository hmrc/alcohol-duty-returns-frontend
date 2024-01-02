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

import models.{RatePeriod, UserAnswers}
import pages.{AlcoholByVolumeQuestionPage, DraughtReliefQuestionPage, SmallProducerReliefQuestionPage}
import views.ViewUtils.withPercentage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
case class TaxTypePageViewModel(
  abv: String,
  eligibleForDraughtRelief: Boolean,
  eligibleForSmallProducerRelief: Boolean,
  radioItems: Seq[RadioItem]
)

object TaxTypePageViewModel {
  def apply(userAnswers: UserAnswers, rates: Seq[RatePeriod])(implicit
    messages: Messages
  ): Option[TaxTypePageViewModel] =
    for {
      abvBigDecimal                  <- userAnswers.get(AlcoholByVolumeQuestionPage)
      eligibleForDraughtRelief       <- userAnswers.get(DraughtReliefQuestionPage)
      eligibleForSmallProducerRelief <- userAnswers.get(SmallProducerReliefQuestionPage)
      ratePeriod                     <- rates.headOption
    } yield {
      val radioItems = ratePeriod.rateBands.map { rateBand =>
        RadioItem(
          content = Text(s"${rateBand.alcoholRegime}, ${messages("taxType.taxTypeRadio.taxType")} ${rateBand.taxType}"),
          value = Some(rateBand.taxType),
          id = Some(s"value_${rateBand.taxType}")
        )
      }
      TaxTypePageViewModel(
        withPercentage(abvBigDecimal),
        eligibleForDraughtRelief,
        eligibleForSmallProducerRelief,
        radioItems
      )
    }
}
