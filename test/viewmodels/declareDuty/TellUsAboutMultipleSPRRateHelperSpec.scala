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

import base.SpecBase
import models.AlcoholRegime.Beer
import models.RateBand
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

import scala.collection.immutable.SortedSet

class TellUsAboutMultipleSPRRateHelperSpec extends SpecBase {
  "TellUsAboutMultipleSPRRateHelper" - {
    "return the expected radio items" in new SetUp {
      val rateBandsReversed = SortedSet(
        smallProducerReliefRateBand2,
        draughtAndSmallProducerReliefRateBand2,
        coreRateBand,
        draughtReliefRateBand,
        smallProducerReliefRateBand,
        draughtAndSmallProducerReliefRateBand
      )((x: RateBand, y: RateBand) => y.taxTypeCode(2) - x.taxTypeCode(2))
      val result            = TellUsAboutMultipleSPRRateHelper.radioItems(rateBandsReversed, regime)

      result.map(_.content) mustBe Seq(
        Text("Non-draught beer between 3% and 4% ABV (tax type code 125 SPR)"),
        Text("Non-draught beer between 6% and 8% ABV (tax type code 127 SPR)"),
        Text("Draught beer between 4% and 5% ABV (tax type code 126 SPR)"),
        Text("Draught beer between 1% and 3% ABV (tax type code 128 SPR)")
      )
      result.map(_.value)   mustBe Seq(Some("125"), Some("127"), Some("126"), Some("128"))
    }
  }

  class SetUp {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val regime = Beer
  }
}
