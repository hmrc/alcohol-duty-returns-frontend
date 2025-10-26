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
import models.AlcoholRegime.Beer
import models.NormalMode
import pages.declareDuty.{MultipleSPRListPage, WhatDoYouNeedToDeclarePage}
import viewmodels.TableRowActionViewModel

class MultipleSPRListHelperSpec extends SpecBase {

  "MultipleSPRListHelper" - {
    val regime = Beer
    "must return a TableViewModel with correct rows when user answers are valid" in {
      val userAnswers = emptyUserAnswers
        .setByKey(WhatDoYouNeedToDeclarePage, regime, allRateBands)
        .success
        .value
        .setByKey(MultipleSPRListPage, regime, Seq(volumeAndRateByTaxType2, volumeAndRateByTaxType3))
        .success
        .value

      val expectedHeader = List(
        "Description",
        "Total volume declared (litres)",
        "Litres of pure alcohol (LPA)",
        "Duty rate (per litre)",
        "Action"
      )

      val expectedRows = List(
        List("Non-draught beer between 3% and 4% ABV (tax type code 125 SPR)", "1,000.00", "3.5000", "£1.46"),
        List("Draught beer between 4% and 5% ABV (tax type code 126 SPR)", "10,000.00", "4.5000", "£1.66")
      )

      val expectedActions = Seq(
        Seq(
          TableRowActionViewModel(
            label = "Change",
            href = controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController
              .onPageLoad(NormalMode, regime, Some(0)),
            visuallyHiddenText = Some("non-draught beer between 3% and 4% ABV (tax type code 125 SPR)")
          ),
          TableRowActionViewModel(
            label = "Remove",
            href = controllers.declareDuty.routes.DeleteMultipleSPREntryController.onPageLoad(regime, Some(0)),
            visuallyHiddenText = Some("non-draught beer between 3% and 4% ABV (tax type code 125 SPR)")
          )
        ),
        Seq(
          TableRowActionViewModel(
            label = "Change",
            href = controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController
              .onPageLoad(NormalMode, regime, Some(1)),
            visuallyHiddenText = Some("draught beer between 4% and 5% ABV (tax type code 126 SPR)")
          ),
          TableRowActionViewModel(
            label = "Remove",
            href = controllers.declareDuty.routes.DeleteMultipleSPREntryController.onPageLoad(regime, Some(1)),
            visuallyHiddenText = Some("draught beer between 4% and 5% ABV (tax type code 126 SPR)")
          )
        )
      )

      val result = MultipleSPRListHelper.sprTableViewModel(userAnswers, regime)(getMessages(app))

      result.isRight mustBe true

      val tableViewModel = result.getOrElse(fail("Expected Right(TableViewModel) but got Left"))

      tableViewModel.head.map(_.content.asHtml.toString)              mustBe expectedHeader
      tableViewModel.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedRows
      tableViewModel.rows.map(_.actions)                              mustBe expectedActions
    }

    "must return a Left with error message when MultipleSPRListPage is not in user answers" in {
      val userAnswers = emptyUserAnswers
        .setByKey(WhatDoYouNeedToDeclarePage, regime, allRateBands)
        .success
        .value

      val result = MultipleSPRListHelper.sprTableViewModel(userAnswers, regime)(getMessages(app))

      result mustBe Left("Error retrieving SPR entries and rate bands")
    }

    "must return a Left with error message when WhatDoYouNeedToDeclarePage is not in user answers" in {
      val userAnswers = emptyUserAnswers
        .setByKey(MultipleSPRListPage, regime, Seq(volumeAndRateByTaxType2, volumeAndRateByTaxType3))
        .success
        .value

      val result = MultipleSPRListHelper.sprTableViewModel(userAnswers, regime)(getMessages(app))

      result mustBe Left("Error retrieving SPR entries and rate bands")
    }

    "must return a Left with error message when WhatDoYouNeedToDeclarePage is missing a tax type in MultipleSPRListPage" in {
      val userAnswers = emptyUserAnswers
        .setByKey(WhatDoYouNeedToDeclarePage, regime, allNonSmallProducerReliefRateBands)
        .success
        .value
        .setByKey(MultipleSPRListPage, regime, Seq(volumeAndRateByTaxType2, volumeAndRateByTaxType3))
        .success
        .value

      val result = MultipleSPRListHelper.sprTableViewModel(userAnswers, regime)(getMessages(app))

      result mustBe Left("Tax types not found: 125, 126")
    }
  }
}
