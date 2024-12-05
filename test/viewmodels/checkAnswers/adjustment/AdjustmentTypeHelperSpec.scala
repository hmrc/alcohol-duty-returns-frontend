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
import cats.data.NonEmptySeq
import config.FrontendAppConfig
import models.AlcoholRegime.{Beer, Cider}
import models.RateType.Core
import models.{ABVRange, AlcoholByVolume, AlcoholRegimes, AlcoholType, RangeDetailsByRegime, RateBand}
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
        val regimes = AlcoholRegimes(Set(regime))
        val userAnswers = emptyUserAnswers
          .copy(regimes = regimes)
          .set(CurrentAdjustmentEntryPage, AdjustmentEntry()).success.value
        when(alcoholicProductTypeHelper.createRateBandFromRegime(regime))
          .thenReturn(spoiltRateBand)
        val result = helper.checkIfOneRegimeAndSpoiltAndUpdateUserAnswers(userAnswers, Spoilt)
        val updatedAnswers = result.get
        val updatedEntry = updatedAnswers.get(CurrentAdjustmentEntryPage).value
        updatedEntry.spoiltRegime mustBe Some(models.AlcoholRegime.Beer)
        updatedEntry.rateBand mustBe Some(spoiltRateBand)
        updatedEntry.period mustBe Some(YearMonth.now().minusMonths(1))
      }

      "should return the original user answers when there is more than one regime" in new SetUp {
        val regimes = AlcoholRegimes(Set(regime, Cider))
        val userAnswers = emptyUserAnswers.copy(regimes = regimes)
        val result = helper.checkIfOneRegimeAndSpoiltAndUpdateUserAnswers(userAnswers, Spoilt)
        result mustBe Success(userAnswers)
      }

      "should return the original user answers when the adjustment type is not Spoilt" in new SetUp {
        val regimes = AlcoholRegimes(Set(models.AlcoholRegime.Beer))
        val userAnswers = emptyUserAnswers.copy(regimes = regimes)
        val result = helper.checkIfOneRegimeAndSpoiltAndUpdateUserAnswers(userAnswers, AdjustmentType.Drawback)
        result mustBe Success(userAnswers)
      }
    }
  }

  class SetUp{
    val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val alcoholicProductTypeHelper = mock[AlcoholicProductTypeHelper]
    val helper = new AdjustmentTypeHelper(alcoholicProductTypeHelper)
    implicit val messages: Messages = getMessages(applicationBuilder().build())
    val regime = Beer
    val spoiltRateBand = RateBand(
      appConfig.getTaxTypeCodeByRegime(regime),
      messages(s"alcoholType.$regime"),
      Core,
      Some(BigDecimal(0.01)),
      Set(
        RangeDetailsByRegime(regime, NonEmptySeq.one(ABVRange(AlcoholType.Beer, AlcoholByVolume(0), AlcoholByVolume(100))))
      )
    )
  }
}