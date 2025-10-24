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

package viewmodels.payments

import base.SpecBase
import config.Constants.Css
import config.FrontendAppConfig
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Configuration
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.html.components.GovukTag
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import viewmodels.TableRowActionViewModel

import java.time.LocalDate

class ViewPastPaymentsHelperSpec extends SpecBase with ScalaCheckPropertyChecks {

  val testConfiguration: Configuration   = app.injector.instanceOf[Configuration]
  val testServicesConfig: ServicesConfig = app.injector.instanceOf[ServicesConfig]

  val testAppConfig: FrontendAppConfig = new FrontendAppConfig(testConfiguration, testServicesConfig) {
    override val claimARefundGformEnabled = false
  }

  implicit val messages: Messages = getMessages(app)
  val viewPastPaymentsHelper      = new ViewPastPaymentsHelper(createDateTimeHelper(), testAppConfig, clock)

  val testAppConfigToggleOn: FrontendAppConfig = new FrontendAppConfig(testConfiguration, testServicesConfig) {
    override val claimARefundGformEnabled = true
  }

  val viewPastPaymentsHelperToggleOn =
    new ViewPastPaymentsHelper(createDateTimeHelper(), testAppConfigToggleOn, clock)

  val dueTag          = new GovukTag()(
    Tag(content = Text("Due"), classes = Css.blueTagCssClass)
  )
  val overdueTag      = new GovukTag()(
    Tag(content = Text("Overdue"), classes = Css.redTagCssClass)
  )
  val nothingToPayTag = new GovukTag()(
    Tag(content = Text("Nothing to pay"), classes = Css.greyTagCssClass)
  )

  val expectedOutstandingHeader = Seq("To be paid by", "Description", "Left to pay", "Status", "Action")
  val expectedUnallocatedHeader = Seq("Payment date", "Description", "Amount", "Action")
  val expectedClearedHeader     = Seq("Return period", "Description", "Amount")

  val expectedOutstandingRows = List(
    List(
      "25 June 9999",
      s"<span class='break'>Payment for Alcohol Duty return</span><span class='break'>(ref: $chargeReference)</span>",
      "£4,773.34",
      dueTag.toString
    ),
    List(
      "25 July 9998",
      s"<span class='break'>Payment for Alcohol Duty return</span><span class='break'>(ref: $chargeReference)</span>",
      "£3,234.12",
      dueTag.toString
    ),
    List(
      "25 August 9997",
      s"<span class='break'>Late payment interest charge</span><span class='break'>(ref: $chargeReference)</span>",
      "£3,234.18",
      dueTag.toString
    ),
    List(
      "25 October 2024",
      s"<span class='break'>Credit for Alcohol Duty return</span><span class='break'>(ref: $chargeReference)</span>",
      "−£4,773.34",
      nothingToPayTag.toString
    ),
    List(
      "25 September 2024",
      s"<span class='break'>Central assessment interest charge</span><span class='break'>(ref: $chargeReference)</span>",
      "£3,234.18",
      dueTag.toString
    ),
    List(
      "25 August 2024",
      s"<span class='break'>Central assessment charge</span><span class='break'>(ref: $chargeReference)</span>",
      "£3,234.18",
      dueTag.toString
    ),
    List(
      "25 July 2024",
      s"<span class='break'>Refund payment interest charge</span><span class='break'>(ref: $chargeReference)</span>",
      "−£2,011.00",
      nothingToPayTag.toString
    ),
    List(
      "25 September 2022",
      s"<span class='break'>Payment for Alcohol Duty return</span><span class='break'>(ref: $chargeReference)</span>",
      "£4,773.34",
      overdueTag.toString
    )
  )

  val expectedUnallocatedRows = List(
    List("25 September 2024", "Payment", "−£123.00"),
    List("25 August 2024", "Payment", "−£1,273.00"),
    List("25 July 2024", "Payment", "−£1,273.00")
  )

  val expectedClearedRows = List(
    List(
      "July 2025",
      s"<span class='break'>Cleared central assessment interest payment</span><span class='break'>(ref: $chargeReference)</span>",
      "£123.45"
    ),
    List(
      "June 2025",
      s"<span class='break'>Cleared late payment interest charge payments</span><span class='break'>(ref: $chargeReference)</span>",
      "£12.45"
    ),
    List(
      "May 2025",
      s"<span class='break'>Cleared central assessment payment</span><span class='break'>(ref: $chargeReference)</span>",
      "£234.45"
    ),
    List(
      "April 2025",
      s"<span class='break'>Cleared Alcohol Duty payments</span><span class='break'>(ref: $chargeReference)</span>",
      "£1,236.45"
    )
  )

  "ViewPastPaymentsViewModel" - {
    "when the gform feature toggle is disabled" - {

      "must NOT present the link to the Gform for an outstanding overpaid payment" in {
        val table = viewPastPaymentsHelper.getOutstandingPaymentsTable(openPaymentsData.outstandingPayments)

        table.rows.head.actions.headOption mustBe None
      }

      "must NOT present the link to the Gform for an unallocated payment" in {
        val table = viewPastPaymentsHelper.getUnallocatedPaymentsTable(openPaymentsData.unallocatedPayments)

        table.rows.head.actions.headOption mustBe None
      }

      "must not return a table when outstanding payments are not present" in {
        val table =
          viewPastPaymentsHelper.getOutstandingPaymentsTable(emptyOutstandingPaymentData.outstandingPayments)
        table.head.size mustBe 0
        table.rows.size mustBe 0
      }

      "must not return a table when unallocated payments are not present" in {
        val table =
          viewPastPaymentsHelper.getUnallocatedPaymentsTable(openPaymentsWithoutUnallocatedData.unallocatedPayments)
        table.head.size mustBe 0
        table.rows.size mustBe 0
      }

      "must not return a table when historic payments are not present" in {
        val table =
          viewPastPaymentsHelper.getHistoricPaymentsTable(emptyHistoricPayments.payments)
        table.head.size mustBe 0
        table.rows.size mustBe 0
      }

      "must return the Due status for an outstanding payment which is Due" in {
        val table =
          viewPastPaymentsHelper.getOutstandingPaymentsTable(Seq(outstandingDuePayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe dueTag
        }
      }

      "must return the Overdue status for an outstanding payment which is Overdue and partially paid" in {
        val table =
          viewPastPaymentsHelper.getOutstandingPaymentsTable(Seq(outstandingOverduePartialPayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe overdueTag
        }
      }

      "must return the Nothing to pay status for an outstanding credit payment" in {
        val table =
          viewPastPaymentsHelper.getOutstandingPaymentsTable(Seq(outstandingCreditPayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe nothingToPayTag
        }
      }

      "must return the Due status for an outstanding LPI payment" in {
        val table =
          viewPastPaymentsHelper.getOutstandingPaymentsTable(Seq(outstandingLPIPayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe dueTag
        }
      }

      "must return the Nothing to pay status for an RPI payment" in {
        val table =
          viewPastPaymentsHelper.getOutstandingPaymentsTable(Seq(RPIPayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe nothingToPayTag
        }
      }

      "must have the correct pay now action link for Due/Overdue return" in {
        val table = viewPastPaymentsHelper.getOutstandingPaymentsTable(Seq(outstandingDuePayment))
        table.rows.map { row =>
          row.actions.head.href mustBe controllers.payments.routes.StartPaymentController
            .initiateAndRedirectFromPastPayments(0)
        }
      }

      "must have the correct 'Manage' action link for Central Assessment" in {
        val table = viewPastPaymentsHelper.getOutstandingPaymentsTable(Seq(outstandingCAPayment))
        table.rows.map { row =>
          row.actions.head.href mustBe controllers.payments.routes.ManageCentralAssessmentController
            .onPageLoad(chargeReference)
        }
      }

      "must return a sorted table by due date in descending order for outstanding payments" in {
        val table = viewPastPaymentsHelper.getOutstandingPaymentsTable(
          openPaymentsData.outstandingPayments.sortBy(_.dueDate)(Ordering[LocalDate].reverse)
        )

        val expectedActions = Seq(
          Seq(
            TableRowActionViewModel(
              label = "Pay now",
              href = controllers.payments.routes.StartPaymentController.initiateAndRedirectFromPastPayments(0),
              visuallyHiddenText = Some("amount of £4,773.34 due 25 June 9999")
            )
          ),
          Seq(
            TableRowActionViewModel(
              label = "Pay now",
              href = controllers.payments.routes.StartPaymentController.initiateAndRedirectFromPastPayments(1),
              visuallyHiddenText = Some("amount of £3,234.12 due 25 July 9998")
            )
          ),
          Seq(
            TableRowActionViewModel(
              label = "Pay now",
              href = controllers.payments.routes.StartPaymentController.initiateAndRedirectFromPastPayments(2),
              visuallyHiddenText = Some("amount of £3,234.18 due 25 August 9997")
            )
          ),
          Seq.empty,
          Seq(
            TableRowActionViewModel(
              label = "Pay now",
              href = controllers.payments.routes.StartPaymentController.initiateAndRedirectFromPastPayments(4),
              visuallyHiddenText = Some("amount of £3,234.18 due 25 September 2024")
            )
          ),
          Seq(
            TableRowActionViewModel(
              label = "Manage",
              href = controllers.payments.routes.ManageCentralAssessmentController.onPageLoad(chargeReference),
              visuallyHiddenText = Some("central assessment charge of £3,234.18")
            )
          ),
          Seq.empty,
          Seq(
            TableRowActionViewModel(
              label = "Pay now",
              href = controllers.payments.routes.StartPaymentController.initiateAndRedirectFromPastPayments(7),
              visuallyHiddenText = Some("amount of £4,773.34 due 25 September 2022")
            )
          )
        )

        table.head.map(_.content.asHtml.toString)              mustBe expectedOutstandingHeader
        table.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedOutstandingRows
        table.rows.map(_.actions)                              mustBe expectedActions
      }

      "must return a sorted table by due date in descending order for unallocated payments" in {
        val table = viewPastPaymentsHelper.getUnallocatedPaymentsTable(openPaymentsData.unallocatedPayments)

        table.head.map(_.content.asHtml.toString)              mustBe Seq("Payment date", "Description", "Amount")
        table.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedUnallocatedRows
        table.rows.flatMap(_.actions)                          mustBe Seq.empty
      }

      "must return a sorted table by return period in descending order for historic payments" in {
        val table = viewPastPaymentsHelper.getHistoricPaymentsTable(historicPayments2025.payments)

        table.head.map(_.content.asHtml.toString)              mustBe expectedClearedHeader
        table.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedClearedRows
        table.rows.flatMap(_.actions)                          mustBe Seq.empty
      }
    }

    "when the gform feature toggle is enabled" - {

      "must present the link to the Gform for an outstanding overpaid payment" in {
        val table            = viewPastPaymentsHelperToggleOn.getOutstandingPaymentsTable(openPaymentsData.outstandingPayments)
        val gformExpectedUrl =
          "http://localhost:9195/submissions/new-form/claim-refund-for-overpayment-of-alcohol-duty?amount=4773.34"
        table.rows.head.actions.head.href.url mustBe gformExpectedUrl
      }

      "must present the link to the Gform for an unallocated payment" in {
        val table            = viewPastPaymentsHelperToggleOn.getUnallocatedPaymentsTable(openPaymentsData.unallocatedPayments)
        val gformExpectedUrl =
          "http://localhost:9195/submissions/new-form/claim-refund-for-overpayment-of-alcohol-duty?amount=123"
        table.rows.head.actions.head.href.url mustBe gformExpectedUrl
      }

      "must not return a table when outstanding payments are not present" in {
        val table =
          viewPastPaymentsHelperToggleOn.getOutstandingPaymentsTable(emptyOutstandingPaymentData.outstandingPayments)
        table.head.size mustBe 0
        table.rows.size mustBe 0
      }

      "must not return a table when unallocated payments are not present" in {
        val table =
          viewPastPaymentsHelperToggleOn.getUnallocatedPaymentsTable(
            openPaymentsWithoutUnallocatedData.unallocatedPayments
          )
        table.head.size mustBe 0
        table.rows.size mustBe 0
      }

      "must not return a table when historic payments are not present" in {
        val table =
          viewPastPaymentsHelperToggleOn.getHistoricPaymentsTable(emptyHistoricPayments.payments)
        table.head.size mustBe 0
        table.rows.size mustBe 0
      }

      "must return the Due status for an outstanding payment which is Due" in {
        val table =
          viewPastPaymentsHelperToggleOn.getOutstandingPaymentsTable(Seq(outstandingDuePayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe dueTag
        }
      }

      "must return the Overdue status for an outstanding payment which is Overdue and partially paid" in {
        val table =
          viewPastPaymentsHelperToggleOn.getOutstandingPaymentsTable(Seq(outstandingOverduePartialPayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe overdueTag
        }
      }

      "must return the Nothing to pay status for an outstanding credit payment" in {
        val table =
          viewPastPaymentsHelperToggleOn.getOutstandingPaymentsTable(Seq(outstandingCreditPayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe nothingToPayTag
        }
      }

      "must return the Due status for an outstanding LPI payment" in {
        val table =
          viewPastPaymentsHelperToggleOn.getOutstandingPaymentsTable(Seq(outstandingLPIPayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe dueTag
        }
      }

      "must return the Nothing to pay status for an RPI payment" in {
        val table =
          viewPastPaymentsHelperToggleOn.getOutstandingPaymentsTable(Seq(RPIPayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe nothingToPayTag
        }
      }

      "must have the correct pay now action link for Due/Overdue return" in {
        val table = viewPastPaymentsHelperToggleOn.getOutstandingPaymentsTable(Seq(outstandingDuePayment))
        table.rows.map { row =>
          row.actions.head.href mustBe controllers.payments.routes.StartPaymentController
            .initiateAndRedirectFromPastPayments(0)
        }
      }

      "must have the correct 'Manage' action link for Central Assessment" in {
        val table = viewPastPaymentsHelperToggleOn.getOutstandingPaymentsTable(Seq(outstandingCAPayment))
        table.rows.map { row =>
          row.actions.head.href mustBe controllers.payments.routes.ManageCentralAssessmentController
            .onPageLoad(chargeReference)
        }
      }

      "must return a sorted table by due date in descending order for outstanding payments" in {
        val table = viewPastPaymentsHelperToggleOn.getOutstandingPaymentsTable(
          openPaymentsData.outstandingPayments.sortBy(_.dueDate)(Ordering[LocalDate].reverse)
        )

        val expectedActions = Seq(
          TableRowActionViewModel(
            label = "Pay now",
            href = controllers.payments.routes.StartPaymentController.initiateAndRedirectFromPastPayments(0),
            visuallyHiddenText = Some("amount of £4,773.34 due 25 June 9999")
          ),
          TableRowActionViewModel(
            label = "Pay now",
            href = controllers.payments.routes.StartPaymentController.initiateAndRedirectFromPastPayments(1),
            visuallyHiddenText = Some("amount of £3,234.12 due 25 July 9998")
          ),
          TableRowActionViewModel(
            label = "Pay now",
            href = controllers.payments.routes.StartPaymentController.initiateAndRedirectFromPastPayments(2),
            visuallyHiddenText = Some("amount of £3,234.18 due 25 August 9997")
          ),
          TableRowActionViewModel(
            label = "Claim refund",
            href = Call("GET", appConfig.claimRefundGformUrl("4773.34")),
            visuallyHiddenText = Some("of £4773.34")
          ),
          TableRowActionViewModel(
            label = "Pay now",
            href = controllers.payments.routes.StartPaymentController.initiateAndRedirectFromPastPayments(4),
            visuallyHiddenText = Some("amount of £3,234.18 due 25 September 2024")
          ),
          TableRowActionViewModel(
            label = "Manage",
            href = controllers.payments.routes.ManageCentralAssessmentController.onPageLoad(chargeReference),
            visuallyHiddenText = Some("central assessment charge of £3,234.18")
          ),
          TableRowActionViewModel(
            label = "Claim refund",
            href = Call("GET", appConfig.claimRefundGformUrl("2011")),
            visuallyHiddenText = Some("of £2011")
          ),
          TableRowActionViewModel(
            label = "Pay now",
            href = controllers.payments.routes.StartPaymentController.initiateAndRedirectFromPastPayments(7),
            visuallyHiddenText = Some("amount of £4,773.34 due 25 September 2022")
          )
        )

        table.head.map(_.content.asHtml.toString)              mustBe expectedOutstandingHeader
        table.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedOutstandingRows
        table.rows.map(_.actions.head)                         mustBe expectedActions
      }

      "must return a sorted table by due date in descending order for unallocated payments" in {
        val table = viewPastPaymentsHelperToggleOn.getUnallocatedPaymentsTable(openPaymentsData.unallocatedPayments)

        val expectedActions = Seq(
          TableRowActionViewModel(
            label = "Claim refund",
            href = Call("GET", appConfig.claimRefundGformUrl("123")),
            visuallyHiddenText = Some("of £123")
          ),
          TableRowActionViewModel(
            label = "Claim refund",
            href = Call("GET", appConfig.claimRefundGformUrl("1273")),
            visuallyHiddenText = Some("of £1273")
          ),
          TableRowActionViewModel(
            label = "Claim refund",
            href = Call("GET", appConfig.claimRefundGformUrl("1273")),
            visuallyHiddenText = Some("of £1273")
          )
        )

        table.head.map(_.content.asHtml.toString)              mustBe expectedUnallocatedHeader
        table.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedUnallocatedRows
        table.rows.map(_.actions.head)                         mustBe expectedActions
      }

      "must return a sorted table by return period in descending order for historic payments" in {
        val table = viewPastPaymentsHelperToggleOn.getHistoricPaymentsTable(historicPayments2025.payments)

        table.head.map(_.content.asHtml.toString)              mustBe expectedClearedHeader
        table.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedClearedRows
        table.rows.flatMap(_.actions)                          mustBe Seq.empty
      }
    }
  }

}
