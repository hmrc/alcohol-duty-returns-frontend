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

package viewmodels.declareDuty

import base.SpecBase
import pages.declareDuty._
import play.api.i18n.Messages

import scala.util.Success

class MissingSPRRateBandHelperSpec extends SpecBase {
  implicit val messages: Messages = getMessages(app)

  val helper = new MissingSPRRateBandHelper

  val regime = regimeGen.sample.value

  val declaredNonSPRRateBands = MultipleSPRMissingDetails.declaredNonSPRRateBands(regime)
  val declaredSPRRateBands    = MultipleSPRMissingDetails.declaredSPRRateBands(regime)
  val allDeclaredRateBands    = declaredNonSPRRateBands ++ declaredSPRRateBands

  val multipleSPRListItems            = Seq(
    volumeAndRateByTaxType2,
    volumeAndRateByTaxType3,
    volumeAndRateByTaxType4.copy(taxType = "127"),
    volumeAndRateByTaxType4.copy(taxType = "128")
  )
  val multipleSPRListWithMissingItems = Seq(
    volumeAndRateByTaxType2,
    volumeAndRateByTaxType3
  )

  val missingSPRRateBands = MultipleSPRMissingDetails.missingSPRRateBands(regime)

  "findMissingSPRRateBands must" - {
    "return None if the multiple SPR option has not been selected" in {
      val userAnswers = emptyUserAnswers
        .setByKey(WhatDoYouNeedToDeclarePage, regime, declaredNonSPRRateBands)
        .success
        .value
        .setByKey(DoYouHaveMultipleSPRDutyRatesPage, regime, false)
        .success
        .value

      helper.findMissingSPRRateBands(regime, userAnswers) mustBe None
    }

    "return None if no data is found for the WhatDoYouNeedToDeclare page" in {
      val userAnswers = emptyUserAnswers
        .setByKey(DoYouHaveMultipleSPRDutyRatesPage, regime, true)
        .success
        .value
        .setByKey(MultipleSPRListPage, regime, multipleSPRListItems)
        .success
        .value

      helper.findMissingSPRRateBands(regime, userAnswers) mustBe None
    }

    "return None if no data is found for the MultipleSPRList page" in {
      val userAnswers = emptyUserAnswers
        .setByKey(WhatDoYouNeedToDeclarePage, regime, allDeclaredRateBands)
        .success
        .value
        .setByKey(DoYouHaveMultipleSPRDutyRatesPage, regime, true)
        .success
        .value

      helper.findMissingSPRRateBands(regime, userAnswers) mustBe None
    }

    "return an empty set if MultipleSPRList contains all declared SPR rate bands" in {
      val userAnswers = emptyUserAnswers
        .setByKey(WhatDoYouNeedToDeclarePage, regime, allDeclaredRateBands)
        .success
        .value
        .setByKey(DoYouHaveMultipleSPRDutyRatesPage, regime, true)
        .success
        .value
        .setByKey(MultipleSPRListPage, regime, multipleSPRListItems)
        .success
        .value

      helper.findMissingSPRRateBands(regime, userAnswers) mustBe Some(Set.empty)
    }

    "return the set of missing rate bands if MultipleSPRList does not contain all declared SPR rate bands" in {
      val userAnswers = emptyUserAnswers
        .setByKey(WhatDoYouNeedToDeclarePage, regime, allDeclaredRateBands)
        .success
        .value
        .setByKey(DoYouHaveMultipleSPRDutyRatesPage, regime, true)
        .success
        .value
        .setByKey(MultipleSPRListPage, regime, multipleSPRListWithMissingItems)
        .success
        .value

      helper.findMissingSPRRateBands(regime, userAnswers) mustBe Some(missingSPRRateBands)
    }
  }

  "getMissingRateBandDescriptions must" - {
    "return a Seq of HtmlContent containing the rate band descriptions" in {
      val missingRateBandDescriptions = MultipleSPRMissingDetails.missingRateBandDescriptions(regime)

      helper.getMissingRateBandDescriptions(regime, missingSPRRateBands) mustBe missingRateBandDescriptions
    }
  }

  "removeMissingRateBandDeclarations must" - {
    val userAnswers = emptyUserAnswers
      .setByKey(WhatDoYouNeedToDeclarePage, regime, allDeclaredRateBands)
      .success
      .value
      .setByKey(MultipleSPRListPage, regime, multipleSPRListWithMissingItems)
      .success
      .value

    "return the input user answers if shouldRemove is false" in {
      helper.removeMissingRateBandsIfConfirmed(
        shouldRemove = false,
        regime,
        userAnswers,
        missingSPRRateBands
      ) mustBe Success(userAnswers)
    }

    "remove the missing rate bands from WhatDoYouNeedToDeclare if shouldRemove is true" in {
      val expectedResult = userAnswers
        .setByKey(WhatDoYouNeedToDeclarePage, regime, allDeclaredRateBands.diff(missingSPRRateBands))

      helper.removeMissingRateBandsIfConfirmed(
        shouldRemove = true,
        regime,
        userAnswers,
        missingSPRRateBands
      ) mustBe expectedResult
    }

    "throw an exception if shouldRemove is true and no data is found for the WhatDoYouNeedToDeclare page" in {
      val userAnswersMissingData = emptyUserAnswers
        .setByKey(MultipleSPRListPage, regime, multipleSPRListWithMissingItems)
        .success
        .value

      a[RuntimeException] mustBe thrownBy(
        helper
          .removeMissingRateBandsIfConfirmed(shouldRemove = true, regime, userAnswersMissingData, missingSPRRateBands)
      )
    }
  }
}
