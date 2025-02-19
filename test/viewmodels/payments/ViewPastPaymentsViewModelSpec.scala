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
import play.api.{Application, Configuration}
import play.api.i18n.Messages
import play.api.inject.bind
import uk.gov.hmrc.govukfrontend.views.html.components.GovukTag
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate

class ViewPastPaymentsViewModelSpec extends SpecBase with ScalaCheckPropertyChecks {

  val testConfiguration: Configuration   = app.injector.instanceOf[Configuration]
  val testServicesConfig: ServicesConfig = app.injector.instanceOf[ServicesConfig]

  val testAppConfig: FrontendAppConfig = new FrontendAppConfig(testConfiguration, testServicesConfig) {
    override val claimARefundGformEnabled = false
  }

  val application: Application = applicationBuilder()
    .overrides(bind[FrontendAppConfig].toInstance(testAppConfig))
    .build()

  implicit val messages: Messages = getMessages(application)
  val viewPastPaymentsViewModel   = new ViewPastPaymentsViewModel(createDateTimeHelper(), testAppConfig)

  val testAppConfigToggleOn: FrontendAppConfig = new FrontendAppConfig(testConfiguration, testServicesConfig) {
    override val claimARefundGformEnabled = true
  }

  val viewPastPaymentsViewModelToggleOn = new ViewPastPaymentsViewModel(createDateTimeHelper(), testAppConfigToggleOn)

  "ViewPastPaymentsViewModel" - {
    "when the gform feature toggle is disabled" - {

      "must NOT present the link to the Gform for an outstanding overpaid payment" in {
        val table = viewPastPaymentsViewModel.getOutstandingPaymentsTable(openPaymentsData.outstandingPayments)

        table.rows.head.actions.headOption mustBe None
      }

      "must NOT present the link to the Gform for an unallocated payment" in {
        val table = viewPastPaymentsViewModel.getUnallocatedPaymentsTable(openPaymentsData.unallocatedPayments)

        table.rows.head.actions.headOption mustBe None
      }

      "must return a table with the correct number of rows and head for outstanding payments" in {
        val table = viewPastPaymentsViewModel.getOutstandingPaymentsTable(openPaymentsData.outstandingPayments)
        table.head.size mustBe 5
        table.rows.size mustBe openPaymentsData.outstandingPayments.size
      }

      "must return a table with the correct number of rows and head for unallocated payments" in {
        val table = viewPastPaymentsViewModel.getUnallocatedPaymentsTable(openPaymentsData.unallocatedPayments)
        table.head.size mustBe 3
        table.rows.size mustBe openPaymentsData.unallocatedPayments.size
      }

      "must return a table with the correct number of rows and head for historic payments" in {
        val table = viewPastPaymentsViewModel.getHistoricPaymentsTable(historicPayments.payments)
        table.head.size mustBe 3
        table.rows.size mustBe historicPayments.payments.size
      }

      "must not return a table when outstanding payments are not present" in {
        val table =
          viewPastPaymentsViewModel.getOutstandingPaymentsTable(emptyOutstandingPaymentData.outstandingPayments)
        table.head.size mustBe 0
        table.rows.size mustBe emptyOutstandingPaymentData.outstandingPayments.size
      }

      "must not return a table when unallocated payments are not present" in {
        val table =
          viewPastPaymentsViewModel.getUnallocatedPaymentsTable(openPaymentsWithoutUnallocatedData.unallocatedPayments)
        table.head.size mustBe 0
        table.rows.size mustBe openPaymentsWithoutUnallocatedData.unallocatedPayments.size
      }

      "must not return a table when historic payments are not present" in {
        val table =
          viewPastPaymentsViewModel.getHistoricPaymentsTable(emptyHistoricPayment.payments)
        table.head.size mustBe 0
        table.rows.size mustBe emptyHistoricPayment.payments.size
      }

      "must return the Due status for an outstanding payment which is Due" in {
        val table =
          viewPastPaymentsViewModel.getOutstandingPaymentsTable(Seq(outstandingDuePayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe new GovukTag()(
            Tag(content = Text("Due"), classes = Css.blueTagCssClass)
          )
        }
      }

      "must return the Overdue status for an outstanding payment which is Overdue and partially paid" in {
        val table =
          viewPastPaymentsViewModel.getOutstandingPaymentsTable(Seq(outstandingOverduePartialPayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe new GovukTag()(
            Tag(content = Text("Overdue"), classes = Css.redTagCssClass)
          )
        }
      }

      "must return the Nothing to pay status for an outstanding credit payment" in {
        val table =
          viewPastPaymentsViewModel.getOutstandingPaymentsTable(Seq(outstandingCreditPayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe new GovukTag()(
            Tag(content = Text("Nothing to pay"), classes = Css.greyTagCssClass)
          )
        }
      }

      "must return the Due status for an outstanding LPI payment" in {
        val table =
          viewPastPaymentsViewModel.getOutstandingPaymentsTable(Seq(outstandingLPIPayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe new GovukTag()(
            Tag(content = Text("Due"), classes = Css.blueTagCssClass)
          )
        }
      }

      "must return the Nothing to pay status for an RPI payment" in {
        val table =
          viewPastPaymentsViewModel.getOutstandingPaymentsTable(Seq(RPIPayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe new GovukTag()(
            Tag(content = Text("Nothing to pay"), classes = Css.greyTagCssClass)
          )
        }
      }

      "must have the correct pay now action link for Due/Overdue return" in {
        val table = viewPastPaymentsViewModel.getOutstandingPaymentsTable(Seq(outstandingDuePayment))
        table.rows.map { row =>
          row.actions.head.href mustBe controllers.payments.routes.StartPaymentController
            .initiateAndRedirectFromPastPayments(0)
        }
      }

      "must return a sorted table by due date in descending order for outstanding payments" in {
        val table = viewPastPaymentsViewModel.getOutstandingPaymentsTable(
          openPaymentsData.outstandingPayments.sortBy(_.dueDate)(Ordering[LocalDate].reverse)
        )
        table.rows.size                                                 mustBe openPaymentsData.outstandingPayments.size
        table.rows.map(row => row.cells.head.content.asHtml.toString()) mustBe Seq(
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
        table.rows.size                                                 mustBe openPaymentsData.unallocatedPayments.size
        table.rows.map(row => row.cells.head.content.asHtml.toString()) mustBe Seq(
          Text("25 September 2024").asHtml.toString(),
          Text("25 August 2024").asHtml.toString(),
          Text("25 July 2024").asHtml.toString()
        )
      }

      "must return a sorted table by return period in descending order for historic payments" in {
        val table = viewPastPaymentsViewModel.getHistoricPaymentsTable(historicPayments.payments)
        table.rows.size                                                 mustBe historicPayments.payments.size
        table.rows.map(row => row.cells.head.content.asHtml.toString()) mustBe Seq(
          Text("December 2024").asHtml.toString(),
          Text("November 2024").asHtml.toString(),
          Text("October 2024").asHtml.toString(),
          Text("September 2024").asHtml.toString()
        )
      }
    }

    "when the gform feature toggle is enabled" - {

      "must present the link to the Gform for an outstanding overpaid payment" in {
        val table            = viewPastPaymentsViewModelToggleOn.getOutstandingPaymentsTable(openPaymentsData.outstandingPayments)
        val gformExpectedUrl =
          "http://localhost:9195/submissions/new-form/claim-a-refund-for-an-overpayment-of-alcohol-duty?amount=4773.34"
        table.rows.head.actions.head.href.url mustBe gformExpectedUrl
      }

      "must present the link to the Gform for an unallocated payment" in {
        val table            = viewPastPaymentsViewModelToggleOn.getUnallocatedPaymentsTable(openPaymentsData.unallocatedPayments)
        val gformExpectedUrl =
          "http://localhost:9195/submissions/new-form/claim-a-refund-for-an-overpayment-of-alcohol-duty?amount=123"
        table.rows.head.actions.head.href.url mustBe gformExpectedUrl
      }

      "must return a table with the correct number of rows and head for outstanding payments" in {
        val table = viewPastPaymentsViewModelToggleOn.getOutstandingPaymentsTable(openPaymentsData.outstandingPayments)
        table.head.size mustBe 5
        table.rows.size mustBe openPaymentsData.outstandingPayments.size
      }

      "must return a table with the correct number of rows and head for unallocated payments" in {
        val table = viewPastPaymentsViewModelToggleOn.getUnallocatedPaymentsTable(openPaymentsData.unallocatedPayments)
        table.head.size mustBe 4
        table.rows.size mustBe openPaymentsData.unallocatedPayments.size
      }

      "must return a table with the correct number of rows and head for historic payments" in {
        val table = viewPastPaymentsViewModelToggleOn.getHistoricPaymentsTable(historicPayments.payments)
        table.head.size mustBe 3
        table.rows.size mustBe historicPayments.payments.size
      }

      "must not return a table when outstanding payments are not present" in {
        val table =
          viewPastPaymentsViewModelToggleOn.getOutstandingPaymentsTable(emptyOutstandingPaymentData.outstandingPayments)
        table.head.size mustBe 0
        table.rows.size mustBe emptyOutstandingPaymentData.outstandingPayments.size
      }

      "must not return a table when unallocated payments are not present" in {
        val table =
          viewPastPaymentsViewModelToggleOn.getUnallocatedPaymentsTable(
            openPaymentsWithoutUnallocatedData.unallocatedPayments
          )
        table.head.size mustBe 0
        table.rows.size mustBe openPaymentsWithoutUnallocatedData.unallocatedPayments.size
      }

      "must not return a table when historic payments are not present" in {
        val table =
          viewPastPaymentsViewModelToggleOn.getHistoricPaymentsTable(emptyHistoricPayment.payments)
        table.head.size mustBe 0
        table.rows.size mustBe emptyHistoricPayment.payments.size
      }

      "must return the Due status for an outstanding payment which is Due" in {
        val table =
          viewPastPaymentsViewModelToggleOn.getOutstandingPaymentsTable(Seq(outstandingDuePayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe new GovukTag()(
            Tag(content = Text("Due"), classes = Css.blueTagCssClass)
          )
        }
      }

      "must return the Overdue status for an outstanding payment which is Overdue and partially paid" in {
        val table =
          viewPastPaymentsViewModelToggleOn.getOutstandingPaymentsTable(Seq(outstandingOverduePartialPayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe new GovukTag()(
            Tag(content = Text("Overdue"), classes = Css.redTagCssClass)
          )
        }
      }

      "must return the Nothing to pay status for an outstanding credit payment" in {
        val table =
          viewPastPaymentsViewModelToggleOn.getOutstandingPaymentsTable(Seq(outstandingCreditPayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe new GovukTag()(
            Tag(content = Text("Nothing to pay"), classes = Css.greyTagCssClass)
          )
        }
      }

      "must return the Due status for an outstanding LPI payment" in {
        val table =
          viewPastPaymentsViewModelToggleOn.getOutstandingPaymentsTable(Seq(outstandingLPIPayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe new GovukTag()(
            Tag(content = Text("Due"), classes = Css.blueTagCssClass)
          )
        }
      }

      "must return the Nothing to pay status for an RPI payment" in {
        val table =
          viewPastPaymentsViewModelToggleOn.getOutstandingPaymentsTable(Seq(RPIPayment))
        table.rows.map { row =>
          row.cells(3).content.asHtml mustBe new GovukTag()(
            Tag(content = Text("Nothing to pay"), classes = Css.greyTagCssClass)
          )
        }
      }

      "must have the correct pay now action link for Due/Overdue return" in {
        val table = viewPastPaymentsViewModelToggleOn.getOutstandingPaymentsTable(Seq(outstandingDuePayment))
        table.rows.map { row =>
          row.actions.head.href mustBe controllers.payments.routes.StartPaymentController
            .initiateAndRedirectFromPastPayments(0)
        }
      }

      "must return a sorted table by due date in descending order for outstanding payments" in {
        val table = viewPastPaymentsViewModelToggleOn.getOutstandingPaymentsTable(
          openPaymentsData.outstandingPayments.sortBy(_.dueDate)(Ordering[LocalDate].reverse)
        )
        table.rows.size                                                 mustBe openPaymentsData.outstandingPayments.size
        table.rows.map(row => row.cells.head.content.asHtml.toString()) mustBe Seq(
          Text("25 June 9999").asHtml.toString(),
          Text("25 July 9998").asHtml.toString(),
          Text("25 August 9997").asHtml.toString(),
          Text("25 October 2024").asHtml.toString(),
          Text("25 July 2024").asHtml.toString(),
          Text("25 September 2022").asHtml.toString()
        )
      }

      "must return a sorted table by due date in descending order for unallocated payments" in {
        val table = viewPastPaymentsViewModelToggleOn.getUnallocatedPaymentsTable(openPaymentsData.unallocatedPayments)
        table.rows.size                                                 mustBe openPaymentsData.unallocatedPayments.size
        table.rows.map(row => row.cells.head.content.asHtml.toString()) mustBe Seq(
          Text("25 September 2024").asHtml.toString(),
          Text("25 August 2024").asHtml.toString(),
          Text("25 July 2024").asHtml.toString()
        )
      }

      "must return a sorted table by return period in descending order for historic payments" in {
        val table = viewPastPaymentsViewModelToggleOn.getHistoricPaymentsTable(historicPayments.payments)
        table.rows.size                                                 mustBe historicPayments.payments.size
        table.rows.map(row => row.cells.head.content.asHtml.toString()) mustBe Seq(
          Text("December 2024").asHtml.toString(),
          Text("November 2024").asHtml.toString(),
          Text("October 2024").asHtml.toString(),
          Text("September 2024").asHtml.toString()
        )
      }
    }
  }

}
