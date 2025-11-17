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
      "must return a model with data when alcohol declared" in new SetUp {
        val alcoholDeclaredViewModel =
          viewModel.createAlcoholDeclaredViewModel(returnDetails, exampleRateBands(periodKey))
        val totalSummaryList         = alcoholDeclaredViewModel.total.get

        val expectedRows = List(
          List("311", "450.0000", "£9.27", "£4,171.50"),
          List("Non-draught beer between 1% and 3% ABV (tax type code 125)", "450.0000", "£21.01", "£9,454.50"),
          List("Non-draught beer between 1% and 3% ABV (tax type code 124)", "450.0000", "£28.50", "£12,825.00"),
          List("Non-draught beer between 1% and 3% ABV (tax type code 123)", "450.0000", "£31.64", "£14,238.00"),
          List("351", "450.0000", "£8.42", "£3,789.00"),
          List("356", "450.0000", "£19.08", "£8,586.00"),
          List("361", "450.0000", "£8.40", "£3,780.00"),
          List("366", "450.0000", "£16.47", "£7,411.50"),
          List("371", "450.0000", "£8.20", "£3,960.00"),
          List("376", "450.0000", "£15.63", "£7,033.50")
        )

        alcoholDeclaredViewModel.head.map(_.content.asHtml.toString)              mustBe expectedDeclarationHeader
        alcoholDeclaredViewModel.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedRows
        alcoholDeclaredViewModel.rows.flatMap(_.actions)                          mustBe Seq.empty

        totalSummaryList.rows.map(_.key.content.asHtml.toString)   mustBe Seq("Total declared duty value")
        totalSummaryList.rows.map(_.value.content.asHtml.toString) mustBe Seq("£75,249.00")
      }

      "must return a model with no entries when a nil return" in new SetUp {
        val alcoholDeclaredViewModel = viewModel.createAlcoholDeclaredViewModel(nilReturn, emptyRateBands)

        val expectedRows = Seq(
          Seq("No alcohol declared", "Nil")
        )

        alcoholDeclaredViewModel.head.map(_.content.asHtml.toString)              mustBe expectedNilHeader
        alcoholDeclaredViewModel.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedRows
        alcoholDeclaredViewModel.rows.flatMap(_.actions)                          mustBe Seq.empty

        alcoholDeclaredViewModel.total mustBe None
      }

      "must return a model with no entries when a nil return with empty sections" in new SetUp {
        val alcoholDeclaredViewModel = viewModel.createAlcoholDeclaredViewModel(emptyReturnDetails, emptyRateBands)

        val expectedRows = Seq(
          Seq("No alcohol declared", "Nil")
        )

        alcoholDeclaredViewModel.head.map(_.content.asHtml.toString)              mustBe expectedNilHeader
        alcoholDeclaredViewModel.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedRows
        alcoholDeclaredViewModel.rows.flatMap(_.actions)                          mustBe Seq.empty

        alcoholDeclaredViewModel.total mustBe None
      }
    }

    "createAdjustmentsViewModel" - {
      "must return a model with data when adjustments declared" in new SetUp {
        when(appConfig.getRegimeNameByTaxTypeCode("321")).thenReturn(None) // Needed for the Spoilt row we don't check

        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(returnDetails, exampleRateBands(periodKey3))
        val totalSummaryList     = adjustmentsViewModel.total.get

        val expectedRows = List(
          List("Under-declared", "321", "150.0000", "£21.01", "£3,151.50"),
          List("Over-declared", "321", "1,150.0000", "£21.01", "−£24,161.50"),
          List("Repackaged", "321", "150.0000", "£21.01", "£3,151.50"),
          List("Spoilt", "321", "1,150.0000", "not applicable", "−£24,161.50"),
          List(
            "Drawback",
            "Non-draught beer between 1% and 3% ABV (tax type code 125)",
            "75.0000",
            "£21.01",
            "−£1,575.50"
          )
        )

        adjustmentsViewModel.head.map(_.content.asHtml.toString)              mustBe expectedAdjustmentsHeader
        adjustmentsViewModel.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedRows
        adjustmentsViewModel.rows.flatMap(_.actions)                          mustBe Seq.empty

        totalSummaryList.rows.map(_.key.content.asHtml.toString)   mustBe Seq("Total adjustments duty value")
        totalSummaryList.rows.map(_.value.content.asHtml.toString) mustBe Seq(minus + "£19,434.00")
      }

      "must return a model with data when a spoilt adjustment declared where Description is the regime name and duty rate is NA" in new SetUp {
        when(appConfig.getRegimeNameByTaxTypeCode("123")).thenReturn(None)
        when(appConfig.getRegimeNameByTaxTypeCode("333")).thenReturn(Some("Wine"))
        val returnDetailWithSpoilt = returnWithSpoiltAdjustment(periodKey, Instant.now(clock))

        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(
          returnDetailWithSpoilt,
          exampleRateBands(periodKey2)
        )
        val totalSummaryList     = adjustmentsViewModel.total.get

        val expectedRows = List(
          List("Spoilt", "123", "150.0000", "not applicable", "−£3,151.50"),
          List("Spoilt", "Wine", "150.0000", "not applicable", "−£3,151.50")
        )

        adjustmentsViewModel.head.map(_.content.asHtml.toString)              mustBe expectedAdjustmentsHeader
        adjustmentsViewModel.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedRows
        adjustmentsViewModel.rows.flatMap(_.actions)                          mustBe Seq.empty

        totalSummaryList.rows.map(_.key.content.asHtml.toString)   mustBe Seq("Total adjustments duty value")
        totalSummaryList.rows.map(_.value.content.asHtml.toString) mustBe Seq(minus + "£3,151.50")
      }

      "must return a model with no entries when a nil return (nothing declared, no total)" in new SetUp {
        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(nilReturn, emptyRateBands)

        val expectedRows = Seq(
          Seq("No adjustments declared", "Nil")
        )

        adjustmentsViewModel.head.map(_.content.asHtml.toString)              mustBe expectedNilHeader
        adjustmentsViewModel.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedRows
        adjustmentsViewModel.rows.flatMap(_.actions)                          mustBe Seq.empty

        adjustmentsViewModel.total mustBe None
      }

      "must return a model with no entries when a nil return with empty sections (nothing declared, no total)" in new SetUp {
        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(emptyReturnDetails, emptyRateBands)

        val expectedRows = Seq(
          Seq("No adjustments declared", "Nil")
        )

        adjustmentsViewModel.head.map(_.content.asHtml.toString)              mustBe expectedNilHeader
        adjustmentsViewModel.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedRows
        adjustmentsViewModel.rows.flatMap(_.actions)                          mustBe Seq.empty

        adjustmentsViewModel.total mustBe None
      }
    }

    "createTotalDueSummaryList" - {
      "must return a summary list with a total when a total exists" in new SetUp {
        val totalSummaryList = viewModel.createTotalDueSummaryList(returnDetails)

        totalSummaryList.rows.map(_.key.content)   mustBe Seq(Text("Total duty value"))
        totalSummaryList.rows.map(_.value.content) mustBe Seq(Text("£55,815.00"))
      }

      "must return a summary list with value 'Nil' when a nil return" in new SetUp {
        val totalSummaryList = viewModel.createTotalDueSummaryList(nilReturn)

        totalSummaryList.rows.map(_.key.content)   mustBe Seq(Text("Total duty value"))
        totalSummaryList.rows.map(_.value.content) mustBe Seq(Text("Nil"))
      }

      "must return a summary list with a total when a total exists even if no declarations" in new SetUp {
        val totalSummaryList = viewModel.createTotalDueSummaryList(
          emptyReturnDetails.copy(totalDutyDue = ReturnTotalDutyDue(totalDue = nonZeroAmount))
        )

        totalSummaryList.rows.map(_.key.content)   mustBe Seq(Text("Total duty value"))
        totalSummaryList.rows.map(_.value.content) mustBe Seq(Text("£12,345.67"))
      }

      "must return a summary list with a total when a total exists even if no alcohol is declared" in new SetUp {
        val totalSummaryList = viewModel.createTotalDueSummaryList(
          returnDetails.copy(alcoholDeclared =
            ReturnAlcoholDeclared(alcoholDeclaredDetails = None, total = returnDetails.alcoholDeclared.total)
          )
        )

        totalSummaryList.rows.map(_.key.content)   mustBe Seq(Text("Total duty value"))
        totalSummaryList.rows.map(_.value.content) mustBe Seq(Text("£55,815.00"))
      }

      "must return a summary list with a total when a total exists when no adjustments exist" in new SetUp {
        val totalSummaryList = viewModel.createTotalDueSummaryList(
          returnDetails.copy(adjustments =
            ReturnAdjustments(adjustmentDetails = None, total = returnDetails.adjustments.total)
          )
        )

        totalSummaryList.rows.map(_.key.content)   mustBe Seq(Text("Total duty value"))
        totalSummaryList.rows.map(_.value.content) mustBe Seq(Text("£55,815.00"))
      }
    }

    "createNetDutySuspensionViewModel" - {
      "must return a model with data when duty suspension is declared" in new SetUp {
        val netDutySuspensionViewModel = viewModel.createNetDutySuspensionViewModel(returnDetails)

        val expectedRows = List(
          List("Beer", "0.15", "0.4248"),
          List("Cider", "0.38", "0.0379"),
          List("Wine", "0.44", "0.5965"),
          List("Spirits", "0.02", "0.2492"),
          List("Other fermented products", "0.02", "0.1894")
        )

        netDutySuspensionViewModel.head.map(_.content.asHtml.toString)              mustBe expectedDutySuspendedHeader
        netDutySuspensionViewModel.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedRows
        netDutySuspensionViewModel.rows.flatMap(_.actions)                          mustBe Seq.empty
      }

      "must return a model excluding the alcohol types not declared in the return" in new SetUp {
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

        val expectedRows = List(
          List("Beer", "0.15", "0.4248"),
          List("Wine", "0.44", "0.5965"),
          List("Spirits", "0.02", "0.2492"),
          List("Other fermented products", "0.02", "0.1894")
        )

        netDutySuspensionViewModel.head.map(_.content.asHtml.toString)              mustBe expectedDutySuspendedHeader
        netDutySuspensionViewModel.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedRows
        netDutySuspensionViewModel.rows.flatMap(_.actions)                          mustBe Seq.empty
      }

      "must return a model with the right label when nothing declared" in new SetUp {
        val netDutySuspensionViewModel = viewModel.createNetDutySuspensionViewModel(nilReturn)

        val expectedRows = List(
          List("Nothing declared")
        )

        netDutySuspensionViewModel.head.map(_.content.asHtml.toString)              mustBe Seq("Description")
        netDutySuspensionViewModel.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedRows
        netDutySuspensionViewModel.rows.flatMap(_.actions)                          mustBe Seq.empty
      }
    }

    "createSpiritsViewModels" - {
      "must return a model with data when quarterly spirits are declared" in new SetUp {
        val spiritsViewModels = viewModel.createSpiritsViewModels(
          returnDetails.copy(spirits =
            Some(returnDetails.spirits.get.copy(typesOfSpirit = AdrTypeOfSpirit.values.toSet))
          )
        )

        val spiritVolumesTable = spiritsViewModels.mainTable.get
        val spiritTypesTable   = spiritsViewModels.spiritTypesTable.get

        val expectedVolumeRows =
          List(List("Total volume of spirits", "0.05"), List("Scotch Whisky", "0.26"), List("Irish Whiskey", "0.16"))

        val expectedTypeRows = List(
          List(
            "Malt spirit, Grain spirit, Neutral spirit (agricultural origin), Neutral spirit (industrial origin), Beer-based spirit, Cider or perry-based spirit, Wine or made-wine-based spirit, Coco Pops Vodka"
          )
        )

        spiritVolumesTable.head.map(_.content.asHtml.toString)              mustBe expectedSpiritsHeader
        spiritVolumesTable.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedVolumeRows
        spiritVolumesTable.rows.flatMap(_.actions)                          mustBe Seq.empty

        spiritTypesTable.head.map(_.content.asHtml.toString)              mustBe Seq("Types of spirits produced")
        spiritTypesTable.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedTypeRows
        spiritTypesTable.rows.flatMap(_.actions)                          mustBe Seq.empty
      }

      "must return a model with data when quarterly spirits is declared and handling missing other spirits type name gracefully" in new SetUp {
        val spiritsViewModels = viewModel.createSpiritsViewModels(
          returnDetails.copy(spirits =
            Some(
              returnDetails.spirits.get.copy(typesOfSpirit = AdrTypeOfSpirit.values.toSet, otherSpiritTypeName = None)
            )
          )
        )

        spiritsViewModels.mainTable mustBe defined
        val spiritTypesTable = spiritsViewModels.spiritTypesTable.get

        val expectedTypeRows = List(
          List(
            "Malt spirit, Grain spirit, Neutral spirit (agricultural origin), Neutral spirit (industrial origin), Beer-based spirit, Cider or perry-based spirit, Wine or made-wine-based spirit"
          )
        )

        spiritTypesTable.head.map(_.content.asHtml.toString)              mustBe Seq("Types of spirits produced")
        spiritTypesTable.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedTypeRows
        spiritTypesTable.rows.flatMap(_.actions)                          mustBe Seq.empty
      }

      "must return a model with the right label when nothing declared" in new SetUp {
        val spiritsViewModels = viewModel.createSpiritsViewModels(nilReturn)

        val spiritVolumesTable = spiritsViewModels.mainTable.get
        spiritsViewModels.spiritTypesTable mustNot be(defined)

        val expectedVolumeRows = List(
          List("Nothing declared")
        )

        spiritVolumesTable.head.map(_.content.asHtml.toString)              mustBe Seq("Description")
        spiritVolumesTable.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedVolumeRows
        spiritVolumesTable.rows.flatMap(_.actions)                          mustBe Seq.empty
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

    val expectedNilHeader           = Seq("Description", "Duty value")
    val expectedDeclarationHeader   =
      Seq("Description", "Litres of pure alcohol (LPA)", "Duty rate (per litre)", "Duty value")
    val expectedAdjustmentsHeader   =
      Seq("Adjustment", "Description", "Litres of pure alcohol (LPA)", "Duty rate (per litre)", "Duty value")
    val expectedDutySuspendedHeader = Seq("Description", "Total volume (litres)", "Litres of pure alcohol (LPA)")
    val expectedSpiritsHeader       = Seq("Description", "Total volume (LPA)")
  }

}
