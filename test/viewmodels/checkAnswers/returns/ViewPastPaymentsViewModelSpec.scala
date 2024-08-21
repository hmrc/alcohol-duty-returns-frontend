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

package viewmodels.checkAnswers.returns

import base.SpecBase
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.GovukTag
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import viewmodels.returns.ViewPastPaymentsViewModel

class ViewPastPaymentsViewModelSpec extends SpecBase with ScalaCheckPropertyChecks {
  val application: Application    = applicationBuilder().build()
  implicit val messages: Messages = getMessages(application)
  val viewPastPaymentsViewModel   = new ViewPastPaymentsViewModel()

  "ViewPastPaymentsViewModel" - {

    "must return a table with the correct number of rows and head for outstanding payments" in {
      val table = viewPastPaymentsViewModel.getOutstandingPaymentsTable(openPaymentsData.outstandingPayments)
      table.head.size shouldBe 6
      table.rows.size shouldBe openPaymentsData.outstandingPayments.size
    }

    "must return a table with the correct number of rows and head for unallocated payments" in {
      val table = viewPastPaymentsViewModel.getUnallocatedPaymentsTable(openPaymentsData.unallocatedPayments)
      table.head.size shouldBe 3
      table.rows.size shouldBe openPaymentsData.unallocatedPayments.size
    }

    "must not return a table when the unallocated payments is not present" in {
      val table =
        viewPastPaymentsViewModel.getUnallocatedPaymentsTable(openPaymentsWithoutUnallocatedData.unallocatedPayments)
      table.head.size shouldBe 0
      table.rows.size shouldBe openPaymentsWithoutUnallocatedData.unallocatedPayments.size
    }

    "must return the Partially paid status for an outstanding payment which is Partially paid" in {
      val table = viewPastPaymentsViewModel.getOutstandingPaymentsTable(Seq(outstandingPartialPayment))
      table.rows.map { row =>
        row.cells(4).content.asHtml shouldBe new GovukTag()(
          Tag(content = Text("Partially paid"), classes = "govuk-tag--yellow")
        )
      }
    }

    "must return the Due status for an outstanding payment which is Due" in {
      val table =
        viewPastPaymentsViewModel.getOutstandingPaymentsTable(Seq(outstandingDuePayment))
      table.rows.map { row =>
        row.cells(4).content.asHtml shouldBe new GovukTag()(
          Tag(content = Text("Due"), classes = "govuk-tag--blue")
        )
      }
    }

    "must return the Overdue status for an outstanding payment which is Overdue and partially paid" in {
      val table =
        viewPastPaymentsViewModel.getOutstandingPaymentsTable(Seq(outstandingOverduePartialPayment))
      table.rows.map { row =>
        row.cells(4).content.asHtml shouldBe new GovukTag()(
          Tag(content = Text("Overdue"), classes = "govuk-tag--red")
        )
      }
    }

    "must return the Nothing to pay status for an outstanding credit payment" in {
      val table =
        viewPastPaymentsViewModel.getOutstandingPaymentsTable(Seq(outstandingCreditPayment))
      table.rows.map { row =>
        row.cells(4).content.asHtml shouldBe new GovukTag()(
          Tag(content = Text("Nothing to pay"), classes = "govuk-tag--grey")
        )
      }
    }

    "must return the Due status for an outstanding LPI payment" in {
      val table =
        viewPastPaymentsViewModel.getOutstandingPaymentsTable(Seq(outstandingLPIPayment))
      table.rows.map { row =>
        row.cells(4).content.asHtml shouldBe new GovukTag()(
          Tag(content = Text("Due"), classes = "govuk-tag--blue")
        )
      }
    }

    "must return the Nothing to pay status for an RPI payment" in {
      val table =
        viewPastPaymentsViewModel.getOutstandingPaymentsTable(Seq(RPIPayment))
      table.rows.map { row =>
        row.cells(4).content.asHtml shouldBe new GovukTag()(
          Tag(content = Text("Nothing to pay"), classes = "govuk-tag--grey")
        )
      }
    }

    "must return a sorted table by due date in descending order for outstanding payments" in {
      val table = viewPastPaymentsViewModel.getOutstandingPaymentsTable(openPaymentsData.outstandingPayments)
      table.rows.size                                                 shouldBe openPaymentsData.outstandingPayments.size
      table.rows.map(row => row.cells.head.content.asHtml.toString()) shouldBe Seq(
        Text("25 June 9999").asHtml.toString(),
        Text("25 July 9998").asHtml.toString(),
        Text("25 August 9997").asHtml.toString(),
        Text("25 October 2024").asHtml.toString(),
        Text("25 July 2024").asHtml.toString(),
        Text("25 September 2022").asHtml.toString()
      )
    }

    "must return a sorted table by due date in descending order for unallocated payments" in {
      val table = viewPastPaymentsViewModel.getUnallocatedPaymentsTable(openPaymentsData.unallocatedPayments)
      table.rows.size                                                 shouldBe openPaymentsData.unallocatedPayments.size
      table.rows.map(row => row.cells.head.content.asHtml.toString()) shouldBe Seq(
        Text("25 September 2024").asHtml.toString(),
        Text("25 August 2024").asHtml.toString(),
        Text("25 July 2024").asHtml.toString()
      )
    }
  }

}
