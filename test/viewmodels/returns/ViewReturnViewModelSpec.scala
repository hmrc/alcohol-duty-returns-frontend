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
import models.returns.{ReturnAdjustments, ReturnAlcoholDeclared, ReturnTotalDutyDue}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import viewmodels.returns.ViewReturnViewModel

import java.time.Instant

class ViewReturnViewModelSpec extends SpecBase {

  "ViewReturnViewModelSpec" - {
    "createAlcoholDeclaredViewModel" - {
      "should return a model with data when alcohol declared" in new SetUp {
        val alcoholDeclaredViewModel = viewModel.createAlcoholDeclaredViewModel(returnDetails)

        alcoholDeclaredViewModel.rows.size               shouldBe returnDetails.alcoholDeclared.alcoholDeclaredDetails.get.size
        alcoholDeclaredViewModel.total.get.total.content shouldBe Text(
          messages("site.currency.2DP", returnDetails.alcoholDeclared.total)
        )
      }

      "should return a model with no entries when a nil return" in new SetUp {
        val alcoholDeclaredViewModel = viewModel.createAlcoholDeclaredViewModel(nilReturn)

        alcoholDeclaredViewModel.rows.size                  shouldBe 1
        alcoholDeclaredViewModel.rows.head.cells(1).content shouldBe Text(messages("site.nil"))
      }

      "should return a model with no entries when a nil return with empty sections" in new SetUp {
        val alcoholDeclaredViewModel = viewModel.createAlcoholDeclaredViewModel(emptyReturnDetails)

        alcoholDeclaredViewModel.rows.size                  shouldBe 1
        alcoholDeclaredViewModel.rows.head.cells(1).content shouldBe Text(messages("site.nil"))
      }
    }

    "createAdjustmentsViewModel" - {
      "should return a model with data when adjustments declared" in new SetUp {
        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(returnDetails)

        adjustmentsViewModel.rows.size shouldBe returnDetails.adjustments.adjustmentDetails.get.size

        val minus: Char = 0x2212
        adjustmentsViewModel.total.get.total.content shouldBe Text(
          s"$minus${messages("site.currency.2DP", returnDetails.adjustments.total.abs)}"
        )
      }

      "should return a model with no entries when a nil return" in new SetUp {
        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(nilReturn)

        adjustmentsViewModel.rows.size                  shouldBe 1
        adjustmentsViewModel.rows.head.cells(1).content shouldBe Text(messages("site.nil"))
      }

      "should return a model with no entries when a nil return with empty sections" in new SetUp {
        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(emptyReturnDetails)

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
        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(nilReturn)

        adjustmentsViewModel.rows.size                  shouldBe 1
        adjustmentsViewModel.rows.head.cells(1).content shouldBe Text(messages("site.nil"))
      }

      "should return a model with no entries when a nil return with empty sections (nothing declared, no total)" in new SetUp {
        val adjustmentsViewModel = viewModel.createAdjustmentsViewModel(emptyReturnDetails)

        adjustmentsViewModel.rows.size                  shouldBe 1
        adjustmentsViewModel.rows.head.cells(1).content shouldBe Text(messages("site.nil"))
      }
    }
  }

  class SetUp {
    val application: Application    = applicationBuilder().build()
    implicit val messages: Messages = getMessages(application)

    val periodKey     = periodKeyApr
    val nonZeroAmount = BigDecimal("12345.67")

    val viewModel          = new ViewReturnViewModel()
    val returnDetails      = exampleReturnDetails(periodKey, Instant.now(clock))
    val nilReturn          = nilReturnDetails(periodKey, Instant.now(clock))
    val emptyReturnDetails = nilReturnDetailsWithEmptySections(periodKey, Instant.now(clock))
  }

}
