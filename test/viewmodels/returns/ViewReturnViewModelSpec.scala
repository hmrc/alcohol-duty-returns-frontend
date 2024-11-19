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

package viewmodels.returns

import base.SpecBase
import config.FrontendAppConfig
import models.returns.{ReturnAdjustments, ReturnAlcoholDeclared, ReturnDetails, ReturnTotalDutyDue}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

import java.time.Instant

class ViewReturnViewModelSpec extends SpecBase {

  "ViewReturnViewModelSpec" - {
    "createAlcoholDeclaredViewModel" - {
      "should return a model with data when alcohol declared" in new SetUp {
        val alcoholDeclaredViewModel =
          viewModel.createAlcoholDeclaredViewModel(returnDetails, exampleRateBands(periodKey))

        alcoholDeclaredViewModel.rows.size                    shouldBe returnDetails.alcoholDeclared.alcoholDeclaredDetails.get.size
        alcoholDeclaredViewModel.total.get.total.content      shouldBe Text(
          messages("site.currency.2DP", returnDetails.alcoholDeclared.total)
        )
        alcoholDeclaredViewModel.rows.head.cells.head.content shouldBe Text("311")
        alcoholDeclaredViewModel.rows(3).cells.head.content   shouldBe Text(
          "Non-draught beer between 1% and 2% ABV (123)"
        )
      }

      "should return a model with no entries when a nil return" in new SetUp {
        val alcoholDeclaredViewModel = viewModel.createAlcoholDeclaredViewModel(nilReturn, emptyRateBands)

        alcoholDeclaredViewModel.rows.size                  shouldBe 1
        alcoholDeclaredViewModel.rows.head.cells(1).content shouldBe Text(messages("site.nil"))
      }

      "should return a model with no entries when a nil return with empty sections" in new SetUp {
        val alcoholDeclaredViewModel = viewModel.createAlcoholDeclaredViewModel(emptyReturnDetails, emptyRateBands)

        alcoholDeclaredViewModel.rows.size                  shouldBe 1
        alcoholDeclaredViewModel.rows.head.cells(1).content shouldBe Text(messages("site.nil"))
      }
    }

    "createAdjustmentsViewModel" - {
      "should return a model with data when adjustments declared" in new SetUp {
        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(returnDetails, exampleRateBands(periodKey2))

        adjustmentsViewModel.rows.size               shouldBe 4
        adjustmentsViewModel.total.get.total.content shouldBe Text(
          s"$minus${messages("site.currency.2DP", returnDetails.adjustments.total.abs)}"
        )

        adjustmentsViewModel.rows.head.cells(1).content shouldBe Text("321")
        adjustmentsViewModel.rows(3).cells(1).content   shouldBe Text("Non-draught beer between 1% and 2% ABV (125)")
      }

      "should return a model with data when a spoilt adjustment declared where Description is the regime name and duty rate is NA" in new SetUp {
        when(appConfig.getRegimeNameByTaxTypeCode("333")).thenReturn(Some("Wine"))
        val returnDetailWithSpoilt = returnWithSpoiltAdjustment(periodKey, Instant.now(clock))
        val adjustmentsViewModel   = viewModel.createAdjustmentsViewModel(
          returnDetailWithSpoilt,
          exampleRateBands(periodKey2)
        )

        adjustmentsViewModel.rows.size                  shouldBe 2
        adjustmentsViewModel.total.get.total.content    shouldBe Text(
          s"$minus${messages("site.currency.2DP", returnDetailWithSpoilt.adjustments.total.abs)}"
        )
        adjustmentsViewModel.rows.head.cells(1).content shouldBe Text("123")
        adjustmentsViewModel.rows.head.cells(3).content shouldBe Text("NA")
        adjustmentsViewModel.rows(1).cells(1).content   shouldBe Text("Wine")
        adjustmentsViewModel.rows(1).cells(3).content   shouldBe Text("NA")
      }

      "should return a model with no entries when a nil return" in new SetUp {
        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(nilReturn, emptyRateBands)

        adjustmentsViewModel.rows.size                  shouldBe 1
        adjustmentsViewModel.rows.head.cells(1).content shouldBe Text(messages("site.nil"))
      }

      "should return a model with no entries when a nil return with empty sections" in new SetUp {
        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(emptyReturnDetails, emptyRateBands)

        adjustmentsViewModel.rows.size                  shouldBe 1
        adjustmentsViewModel.rows.head.cells(1).content shouldBe Text(messages("site.nil"))
      }
    }

    "createTotalDueViewModel" - {
      "should return a model with a total when a total exists" in new SetUp {
        val totalViewModel = viewModel.createTotalDueViewModel(returnDetails)

        totalViewModel.total.content shouldBe Text(messages("site.currency.2DP", returnDetails.totalDutyDue.totalDue))
      }

      "should return a model with no entries when a nil return" in new SetUp {
        val totalViewModel = viewModel.createTotalDueViewModel(nilReturn)

        totalViewModel.total.content shouldBe Text(messages("site.nil"))
      }

      "should return a model with a total when a total exists even if no declarations" in new SetUp {
        val totalViewModel = viewModel.createTotalDueViewModel(
          emptyReturnDetails.copy(totalDutyDue = ReturnTotalDutyDue(totalDue = nonZeroAmount))
        )

        totalViewModel.total.content shouldBe Text(messages("site.currency.2DP", nonZeroAmount))
      }

      "should return a model with a total when a total exists even if no alcohol is declared" in new SetUp {
        val totalViewModel = viewModel.createTotalDueViewModel(
          returnDetails.copy(alcoholDeclared =
            ReturnAlcoholDeclared(alcoholDeclaredDetails = None, total = returnDetails.alcoholDeclared.total)
          )
        )

        totalViewModel.total.content shouldBe Text(messages("site.currency.2DP", returnDetails.totalDutyDue.totalDue))
      }

      "should return a model with a total when a total exists when no adjustments exist" in new SetUp {
        val totalViewModel = viewModel.createTotalDueViewModel(
          returnDetails.copy(adjustments =
            ReturnAdjustments(adjustmentDetails = None, total = returnDetails.adjustments.total)
          )
        )

        totalViewModel.total.content shouldBe Text(messages("site.currency.2DP", returnDetails.totalDutyDue.totalDue))
      }

      "should return a model with no entries when a nil return (nothing declared, no total)" in new SetUp {
        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(nilReturn, emptyRateBands)

        adjustmentsViewModel.rows.size                  shouldBe 1
        adjustmentsViewModel.rows.head.cells(1).content shouldBe Text(messages("site.nil"))
      }

      "should return a model with no entries when a nil return with empty sections (nothing declared, no total)" in new SetUp {
        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(emptyReturnDetails, emptyRateBands)

        adjustmentsViewModel.rows.size                  shouldBe 1
        adjustmentsViewModel.rows.head.cells(1).content shouldBe Text(messages("site.nil"))
      }
    }

    "createNetDutySuspensionViewModel" - {
      "should return a model with data when duty suspension is declared" in new SetUp {
        val netDutySuspensionViewModel = viewModel.createNetDutySuspensionViewModel(returnDetails)

        netDutySuspensionViewModel.head.size shouldBe 3
        netDutySuspensionViewModel.rows.size shouldBe 5
        netDutySuspensionViewModel.rows.foreach { row =>
          row.cells.size shouldBe 3
        }
      }

      "should return a model excluding the alcohol types not declared in the return" in new SetUp {
        val returnDetailsWithoutCider: ReturnDetails = returnDetails
          .copy(netDutySuspension =
            Some(
              returnDetails.netDutySuspension.get.copy(
                totalLtsCider = None,
                totalLtsPureAlcoholCider = None
              )
            )
          )

        val netDutySuspensionViewModel = viewModel.createNetDutySuspensionViewModel(returnDetailsWithoutCider)

        netDutySuspensionViewModel.head.size shouldBe 3
        netDutySuspensionViewModel.rows.size shouldBe 4

      }

      "should return a model with the right label when nothing declared" in new SetUp {
        val netDutySuspensionViewModel = viewModel.createNetDutySuspensionViewModel(nilReturn)

        netDutySuspensionViewModel.rows.size                    shouldBe 1
        netDutySuspensionViewModel.rows.head.cells.head.content shouldBe Text(
          messages("viewReturn.netDutySuspension.noneDeclared")
        )
      }
    }
  }

  class SetUp {
    val application: Application     = applicationBuilder().build()
    implicit val messages: Messages  = getMessages(application)
    val minus: Char                  = 0x2212
    val periodKey                    = periodKeyApr
    val periodKey2                   = periodKeyJan
    val nonZeroAmount                = BigDecimal("12345.67")
    val appConfig: FrontendAppConfig = mock[FrontendAppConfig]
    val viewModel                    = new ViewReturnViewModel(appConfig)
    val returnDetails                = exampleReturnDetails(periodKey, Instant.now(clock))
    val nilReturn                    = nilReturnDetails(periodKey, Instant.now(clock))
    val emptyReturnDetails           = nilReturnDetailsWithEmptySections(periodKey, Instant.now(clock))
  }

}
