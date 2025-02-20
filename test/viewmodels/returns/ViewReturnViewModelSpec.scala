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
import models.checkAndSubmit.AdrTypeOfSpirit
import models.returns.{ReturnAdjustments, ReturnAlcoholDeclared, ReturnDetails, ReturnTotalDutyDue}
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

        alcoholDeclaredViewModel.rows.size mustBe returnDetails.alcoholDeclared.alcoholDeclaredDetails.get.size
        alcoholDeclaredViewModel.total.get.rows.head.value.content mustBe Text(
          messages(
            "site.currency.2DP",
            returnDetails.alcoholDeclared.total
          )
        )
        alcoholDeclaredViewModel.rows.head.cells.head.content mustBe Text("311")
        alcoholDeclaredViewModel.rows(3).cells.head.content mustBe Text(
          "Non-draught beer between 1% and 2% ABV (123)"
        )
      }

      "should return a model with no entries when a nil return" in new SetUp {
        val alcoholDeclaredViewModel = viewModel.createAlcoholDeclaredViewModel(nilReturn, emptyRateBands)

        alcoholDeclaredViewModel.rows.size mustBe 1
        alcoholDeclaredViewModel.rows.head.cells(1).content mustBe Text(messages("site.nil"))
      }

      "should return a model with no entries when a nil return with empty sections" in new SetUp {
        val alcoholDeclaredViewModel = viewModel.createAlcoholDeclaredViewModel(emptyReturnDetails, emptyRateBands)

        alcoholDeclaredViewModel.rows.size mustBe 1
        alcoholDeclaredViewModel.rows.head.cells(1).content mustBe Text(messages("site.nil"))
      }
    }

    "createAdjustmentsViewModel" - {
      "should return a model with data when adjustments declared" in new SetUp {
        when(appConfig.getRegimeNameByTaxTypeCode("321")).thenReturn(None) // Needed for the Spoilt row we don't check

        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(returnDetails, exampleRateBands(periodKey3))

        adjustmentsViewModel.rows.size mustBe 5
        adjustmentsViewModel.total.get.rows.head.value.content mustBe Text(
          s"$minus${messages("site.currency.2DP", returnDetails.adjustments.total.abs)}"
        )

        adjustmentsViewModel.rows.head.cells(1).content mustBe Text("321")

        adjustmentsViewModel.rows(4).cells(1).content mustBe Text("Non-draught beer between 1% and 2% ABV (125)")
        adjustmentsViewModel.rows(4).cells(3).content mustBe Text("£21.01")
      }

      "should return a model with data when a spoilt adjustment declared where Description is the regime name and duty rate is NA" in new SetUp {
        when(appConfig.getRegimeNameByTaxTypeCode("123")).thenReturn(None)
        when(appConfig.getRegimeNameByTaxTypeCode("333")).thenReturn(Some("Wine"))
        val returnDetailWithSpoilt = returnWithSpoiltAdjustment(periodKey, Instant.now(clock))

        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(
          returnDetailWithSpoilt,
          exampleRateBands(periodKey2)
        )

        adjustmentsViewModel.rows.size mustBe 2
        adjustmentsViewModel.total.get.rows.head.value.content mustBe Text(
          s"$minus${messages("site.currency.2DP", returnDetailWithSpoilt.adjustments.total.abs)}"
        )
        adjustmentsViewModel.rows.head.cells(1).content mustBe Text("123")
        adjustmentsViewModel.rows.head.cells(3).content mustBe Text("not applicable")
        adjustmentsViewModel.rows(1).cells(1).content mustBe Text("Wine")
        adjustmentsViewModel.rows(1).cells(3).content mustBe Text("not applicable")
      }

      "should return a model with no entries when a nil return (nothing declared, no total)" in new SetUp {
        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(nilReturn, emptyRateBands)

        adjustmentsViewModel.rows.size mustBe 1
        adjustmentsViewModel.rows.head.cells(1).content mustBe Text(messages("site.nil"))
      }

      "should return a model with no entries when a nil return with empty sections (nothing declared, no total)" in new SetUp {
        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(emptyReturnDetails, emptyRateBands)

        adjustmentsViewModel.rows.size mustBe 1
        adjustmentsViewModel.rows.head.cells(1).content mustBe Text(messages("site.nil"))
      }
    }

    "createTotalDueSummaryList" - {
      "should return a summary list with a total when a total exists" in new SetUp {
        val totalSummaryList = viewModel.createTotalDueSummaryList(returnDetails)

        totalSummaryList.rows.head.value.content mustBe Text(
          messages("site.currency.2DP", returnDetails.totalDutyDue.totalDue)
        )
      }

      "should return a summary list with value 'Nil' when a nil return" in new SetUp {
        val totalSummaryList = viewModel.createTotalDueSummaryList(nilReturn)

        totalSummaryList.rows.head.value.content mustBe Text(messages("site.nil"))
      }

      "should return a summary list with a total when a total exists even if no declarations" in new SetUp {
        val totalSummaryList = viewModel.createTotalDueSummaryList(
          emptyReturnDetails.copy(totalDutyDue = ReturnTotalDutyDue(totalDue = nonZeroAmount))
        )

        totalSummaryList.rows.head.value.content mustBe Text(messages("site.currency.2DP", nonZeroAmount))
      }

      "should return a summary list with a total when a total exists even if no alcohol is declared" in new SetUp {
        val totalSummaryList = viewModel.createTotalDueSummaryList(
          returnDetails.copy(alcoholDeclared =
            ReturnAlcoholDeclared(alcoholDeclaredDetails = None, total = returnDetails.alcoholDeclared.total)
          )
        )

        totalSummaryList.rows.head.value.content mustBe Text(
          messages("site.currency.2DP", returnDetails.totalDutyDue.totalDue)
        )
      }

      "should return a summary list with a total when a total exists when no adjustments exist" in new SetUp {
        val totalSummaryList = viewModel.createTotalDueSummaryList(
          returnDetails.copy(adjustments =
            ReturnAdjustments(adjustmentDetails = None, total = returnDetails.adjustments.total)
          )
        )

        totalSummaryList.rows.head.value.content mustBe Text(
          messages("site.currency.2DP", returnDetails.totalDutyDue.totalDue)
        )
      }
    }

    "createNetDutySuspensionViewModel" - {
      "should return a model with data when duty suspension is declared" in new SetUp {
        val netDutySuspensionViewModel = viewModel.createNetDutySuspensionViewModel(returnDetails)

        netDutySuspensionViewModel.head.size mustBe 3
        netDutySuspensionViewModel.rows.size mustBe 5
        netDutySuspensionViewModel.rows.foreach { row =>
          row.cells.size mustBe 3
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

        netDutySuspensionViewModel.head.size mustBe 3
        netDutySuspensionViewModel.rows.size mustBe 4

      }

      "should return a model with the right label when nothing declared" in new SetUp {
        val netDutySuspensionViewModel = viewModel.createNetDutySuspensionViewModel(nilReturn)

        netDutySuspensionViewModel.rows.size mustBe 1
        netDutySuspensionViewModel.rows.head.cells.head.content mustBe Text(
          messages("viewReturn.netDutySuspension.noneDeclared")
        )
      }
    }

    "createSpiritsViewModels" - {
      "should return a model with data when quarterly spirits are declared" in new SetUp {
        val spiritsViewModels = viewModel.createSpiritsViewModels(
          returnDetails.copy(spirits =
            Some(returnDetails.spirits.get.copy(typesOfSpirit = AdrTypeOfSpirit.values.toSet))
          )
        )

        spiritsViewModels.size mustBe 2
        spiritsViewModels.head.head.size mustBe 2
        spiritsViewModels.head.head.map(_.content) mustBe Seq(
          Text(messages("viewReturn.table.description.legend")),
          Text(messages("viewReturn.table.totalVolume.lpa.legend"))
        )
        spiritsViewModels.head.rows.size mustBe 3
        spiritsViewModels.head.rows.foreach { row =>
          row.cells.size mustBe 2
        }
        spiritsViewModels.head.rows.map(_.cells.head.content) mustBe Seq(
          Text(messages("viewReturn.spirits.totalVolume")),
          Text(messages("viewReturn.spirits.scotchWhisky")),
          Text(messages("viewReturn.spirits.irishWhiskey"))
        )
        spiritsViewModels.head.caption mustBe Some(messages("viewReturn.spirits.caption"))
        spiritsViewModels.last.head.size mustBe 1
        spiritsViewModels.last.head.map(_.content) mustBe Seq(Text(messages("viewReturn.table.typesOfSpirits.legend")))
        spiritsViewModels.last.rows.size mustBe 1
        spiritsViewModels.last.rows.head.cells.size mustBe 1
        spiritsViewModels.last.rows.head.cells.head.content mustBe Text(
          Seq(
            messages("viewReturn.spirits.type.malt"),
            messages("viewReturn.spirits.type.grain"),
            messages("viewReturn.spirits.type.neutralAgricultural"),
            messages("viewReturn.spirits.type.neutralIndustrial"),
            messages("viewReturn.spirits.type.beer"),
            messages("viewReturn.spirits.type.cider"),
            messages("viewReturn.spirits.type.wine"),
            "Coco Pops Vodka"
          ).mkString(", ")
        )
        spiritsViewModels.last.caption mustBe None
      }

      "should return a model with data when quarterly spirits is declared and handling missing other spirits type name gracefully" in new SetUp {
        val spiritsViewModels = viewModel.createSpiritsViewModels(
          returnDetails.copy(spirits =
            Some(
              returnDetails.spirits.get.copy(typesOfSpirit = AdrTypeOfSpirit.values.toSet, otherSpiritTypeName = None)
            )
          )
        )

        spiritsViewModels.last.rows.head.cells.head.content mustBe Text(
          Seq(
            messages("viewReturn.spirits.type.malt"),
            messages("viewReturn.spirits.type.grain"),
            messages("viewReturn.spirits.type.neutralAgricultural"),
            messages("viewReturn.spirits.type.neutralIndustrial"),
            messages("viewReturn.spirits.type.beer"),
            messages("viewReturn.spirits.type.cider"),
            messages("viewReturn.spirits.type.wine")
          ).mkString(", ")
        )
      }

      "should return a model with the right label when nothing declared" in new SetUp {
        val spiritsViewModels = viewModel.createSpiritsViewModels(nilReturn)

        spiritsViewModels.size mustBe 1
        spiritsViewModels.head.rows.size mustBe 1
        spiritsViewModels.head.rows.head.cells.head.content mustBe Text(
          messages("viewReturn.spirits.noneDeclared")
        )
        spiritsViewModels.head.caption mustBe Some(messages("viewReturn.spirits.caption"))
      }
    }
  }

  class SetUp {
    val application: Application     = applicationBuilder().build()
    implicit val messages: Messages  = getMessages(application)
    val minus: Char                  = 0x2212
    val periodKey                    = periodKeyApr
    val periodKey2                   = periodKeyJan
    val periodKey3                   = periodKeyDec23
    val nonZeroAmount                = BigDecimal("12345.67")
    val appConfig: FrontendAppConfig = mock[FrontendAppConfig]
    val viewModel                    = new ViewReturnViewModel(appConfig)
    val returnDetails                = exampleReturnDetails(periodKey, Instant.now(clock))
    val nilReturn                    = nilReturnDetails(periodKey, Instant.now(clock))
    val emptyReturnDetails           = nilReturnDetailsWithEmptySections(periodKey, Instant.now(clock))
  }

}
