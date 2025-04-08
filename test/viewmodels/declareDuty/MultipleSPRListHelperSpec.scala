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
import config.Constants.Css
import models.AlcoholRegime.Beer
import models.NormalMode
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HeadCell, TableRow, Text}
import viewmodels.{TableRowActionViewModel, TableRowViewModel, TableViewModel}

class MultipleSPRListHelperSpec extends SpecBase {
  "MultipleSPRListHelper" - {
    "must return the table view model when all the data available" in new SetUp {
      multipleSPRListHelper.sprTableViewModel(userAnswers, regime) mustBe
        Right(
          TableViewModel(
            Seq(
              HeadCell(Text("Description"), None, "", None, None, Map.empty),
              HeadCell(Text("Total volume declared (litres)"), Some("numeric"), "", None, None, Map.empty),
              HeadCell(Text("Litres of pure alcohol (LPA)"), Some("numeric"), "", None, None, Map.empty),
              HeadCell(Text("Duty rate (per litre)"), Some("numeric"), "", None, None, Map.empty),
              HeadCell(Text("Action"), None, Css.oneQuarterCssClass, None, None, Map.empty)
            ),
            Seq(
              TableRowViewModel(
                Seq(
                  TableRow(
                    Text("Non-draught beer between 3% and 4% ABV (tax type code 125 SPR)"),
                    None,
                    "",
                    None,
                    None,
                    Map.empty
                  ),
                  TableRow(Text("1,000.00"), Some("numeric"), "", None, None, Map.empty),
                  TableRow(Text("3.5000"), Some("numeric"), "", None, None, Map.empty),
                  TableRow(Text("£1.46"), Some("numeric"), "", None, None, Map.empty)
                ),
                Seq(
                  TableRowActionViewModel(
                    "Change",
                    controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController
                      .onPageLoad(NormalMode, regime, Some(0)),
                    Some("non-draught beer between 3% and 4% ABV (tax type code 125 SPR)")
                  ),
                  TableRowActionViewModel(
                    "Remove",
                    controllers.declareDuty.routes.DeleteMultipleSPREntryController.onPageLoad(regime, Some(0)),
                    Some("non-draught beer between 3% and 4% ABV (tax type code 125 SPR)")
                  )
                )
              ),
              TableRowViewModel(
                Seq(
                  TableRow(
                    Text("Draught beer between 4% and 5% ABV (tax type code 126 SPR)"),
                    None,
                    "",
                    None,
                    None,
                    Map.empty
                  ),
                  TableRow(Text("10,000.00"), Some("numeric"), "", None, None, Map.empty),
                  TableRow(Text("4.5000"), Some("numeric"), "", None, None, Map.empty),
                  TableRow(Text("£1.66"), Some("numeric"), "", None, None, Map.empty)
                ),
                Seq(
                  TableRowActionViewModel(
                    "Change",
                    controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController
                      .onPageLoad(NormalMode, regime, Some(1)),
                    Some("draught beer between 4% and 5% ABV (tax type code 126 SPR)")
                  ),
                  TableRowActionViewModel(
                    "Remove",
                    controllers.declareDuty.routes.DeleteMultipleSPREntryController.onPageLoad(regime, Some(1)),
                    Some("draught beer between 4% and 5% ABV (tax type code 126 SPR)")
                  )
                )
              ),
              TableRowViewModel(
                Seq(
                  TableRow(
                    Text("Draught beer between 4% and 5% ABV (tax type code 126 SPR)"),
                    None,
                    "",
                    None,
                    None,
                    Map()
                  ),
                  TableRow(Text("20,000.00"), Some("numeric"), "", None, None, Map()),
                  TableRow(Text("4.8000"), Some("numeric"), "", None, None, Map()),
                  TableRow(Text("£1.66"), Some("numeric"), "", None, None, Map())
                ),
                Seq(
                  TableRowActionViewModel(
                    "Change",
                    controllers.declareDuty.routes.TellUsAboutMultipleSPRRateController
                      .onPageLoad(NormalMode, regime, Some(2)),
                    Some("draught beer between 4% and 5% ABV (tax type code 126 SPR)")
                  ),
                  TableRowActionViewModel(
                    "Remove",
                    controllers.declareDuty.routes.DeleteMultipleSPREntryController.onPageLoad(regime, Some(2)),
                    Some("draught beer between 4% and 5% ABV (tax type code 126 SPR)")
                  )
                )
              )
            ),
            None,
            None
          )
        )
    }

    "must return an error when WhatDoYouNeedToDeclare is missing" in new SetUp {
      multipleSPRListHelper.sprTableViewModel(userAnswersWithoutWhatDoYouNeedToDeclare, regime) mustBe Left(
        "Error retrieving SPR entries and rate bands"
      )
    }

    "must return an error when MultipleSPRList is missing" in new SetUp {
      multipleSPRListHelper.sprTableViewModel(userAnswersWithoutMultipleSPRList, regime) mustBe Left(
        "Error retrieving SPR entries and rate bands"
      )
    }

    "must return an error when a tax type is not found" in new SetUp {
      multipleSPRListHelper.sprTableViewModel(userAnswersWithBadTaxBand, regime) mustBe Left(
        s"Tax types not found: $badTaxCode"
      )
    }
  }

  class SetUp {
    val multipleSPRListHelper       = new MultipleSPRListHelper
    val regime                      = Beer
    implicit val messages: Messages = getMessages(app)

    val volumeAndRateByTaxType = Seq(
      volumeAndRateByTaxType2,
      volumeAndRateByTaxType3,
      volumeAndRateByTaxType4
    )

    val userAnswers = multipleSPRListPage(
      whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands),
      Beer,
      volumeAndRateByTaxType
    )

    val userAnswersWithoutWhatDoYouNeedToDeclare = multipleSPRListPage(
      userAnswersWithoutBeer,
      Beer,
      volumeAndRateByTaxType
    )

    val userAnswersWithoutMultipleSPRList =
      whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands)

    val badTaxCode = "555"

    val badVolumeAndRateByTaxType = Seq(
      volumeAndRateByTaxType2,
      volumeAndRateByTaxType3,
      volumeAndRateByTaxType4.copy(taxType = badTaxCode)
    )

    val userAnswersWithBadTaxBand = multipleSPRListPage(
      whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands),
      Beer,
      badVolumeAndRateByTaxType
    )
  }
}
