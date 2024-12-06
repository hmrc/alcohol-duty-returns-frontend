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
import base.SpecBase
import models.AlcoholRegime.{Beer, Cider}
import models.AlcoholRegimes
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import models.adjustment.AdjustmentType.Spoilt
import pages.adjustment.CurrentAdjustmentEntryPage
import play.api.i18n.Messages

import java.time.YearMonth
import scala.util.{Success}

class AdjustmentTypeHelperSpec extends SpecBase {
  "AdjustmentTypeHelper" - {
    "checkIfOneRegimeAndSpoiltAndUpdateUserAnswers" - {
      "should update user answers correctly when there is only one regime and adjustment type is Spoilt" in new SetUp {
        val regimes        = AlcoholRegimes(Set(regime))
        val userAnswers    = emptyUserAnswers
          .copy(regimes = regimes)
          .set(CurrentAdjustmentEntryPage, AdjustmentEntry())
          .success
          .value
        when(alcoholicProductTypeHelper.createRateBandFromRegime(regime))
          .thenReturn(spoiltRateBand)
        val result         = helper.updateIfSingleRegimeAndSpoilt(userAnswers, Spoilt, clock)
        val updatedAnswers = result.get
        val updatedEntry   = updatedAnswers.get(CurrentAdjustmentEntryPage).value
        updatedEntry.spoiltRegime mustBe Some(Beer)
        updatedEntry.rateBand mustBe Some(spoiltRateBand)
        updatedEntry.period mustBe Some(YearMonth.now(clock).minusMonths(1))
      }

      "should return the original user answers when there is more than one regime" in new SetUp {
        val regimes     = AlcoholRegimes(Set(Beer, Cider))
        val userAnswers = emptyUserAnswers.copy(regimes = regimes)
        val result      = helper.updateIfSingleRegimeAndSpoilt(userAnswers, Spoilt, clock)
        result mustBe Success(userAnswers)
      }

      "should return the original user answers when the adjustment type is not Spoilt" in new SetUp {
        val regimes     = AlcoholRegimes(Set(regime))
        val userAnswers = emptyUserAnswers.copy(regimes = regimes)
        val result      = helper.updateIfSingleRegimeAndSpoilt(userAnswers, AdjustmentType.Drawback, clock)
        result mustBe Success(userAnswers)
      }
    }
  }

  class SetUp {
    val alcoholicProductTypeHelper  = mock[AlcoholicProductTypeHelper]
    val helper                      = new AdjustmentTypeHelper(alcoholicProductTypeHelper)
    implicit val messages: Messages = getMessages(applicationBuilder().build())
    val regime                      = Beer
  }
}
