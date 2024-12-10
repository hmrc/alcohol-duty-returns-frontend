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

package viewmodels.checkAnswers.adjustment

import models.UserAnswers
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import models.adjustment.AdjustmentType.Spoilt
import pages.adjustment.CurrentAdjustmentEntryPage
import play.api.i18n.Messages

import java.time.{Clock, YearMonth}
import javax.inject.Inject
import scala.util.Try

class AdjustmentTypeHelper @Inject() (
  helper: AlcoholicProductTypeHelper
) {
  def updateIfSingleRegimeAndSpoilt(
    userAnswer: UserAnswers,
    adjustmentType: AdjustmentType,
    clock: Clock
  )(implicit messages: Messages): Try[UserAnswers] =
    userAnswer.regimes.regimes.toList match {
      case List(regime) if adjustmentType == Spoilt =>
        val adjustment       = userAnswer.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
        val rateBand         = helper.createRateBandFromRegime(regime)
        val currentYearMonth = YearMonth.now(clock)
        userAnswer.set(
          CurrentAdjustmentEntryPage,
          adjustment.copy(
            spoiltRegime = Some(regime),
            rateBand = Some(rateBand),
            period = Some(currentYearMonth.minusMonths(1))
          )
        )
      case _                                        => Try(userAnswer)
    }
}