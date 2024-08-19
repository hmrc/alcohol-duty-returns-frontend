package viewmodels.checkAnswers.returns

import base.SpecBase
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.i18n.Messages
import viewmodels.returns.ViewPastPaymentsViewModel

class ViewPastPaymentsViewModelSpec extends SpecBase with ScalaCheckPropertyChecks {
  val application: Application = applicationBuilder().build()
  implicit val messages: Messages = getMessages(application)
  val viewPastPaymentsViewModel = new ViewPastPaymentsViewModel()

  "ViewPastPaymentsViewModel" - {

    "must return a table with the correct head" {
      val table = viewPastPaymentsViewModel.getOutstandingPaymentsTable(openPaymentsData.outstandingPayments)
      table.head.size shouldBe 3
    }
/*
    "must return a table with the correct rows" {
      val openPaymentsData = openPaymentsData
      val table = viewPastPaymentsViewModel.getOutstandingPaymentsTable(openPaymentsData.outstandingPayments)
      table.rows.size shouldBe obligationData.size
      table.rows.map { row =>
        row.actions.head.href shouldBe controllers.routes.BeforeStartReturnController.onPageLoad(periodKeyAug)
      }
    }

    "must return the Completed status for a fulfilled obligation" {
      val obligationData = Seq(obligationDataSingleFulfilled)
      val table = viewPastReturnsHelper.getReturnsTable(obligationData)
      table.rows.size shouldBe obligationData.size
      table.rows.map { row =>
        row.cells(1).content.asHtml shouldBe new GovukTag()(
          Tag(content = Text(messages("Completed")), classes = "govuk-tag--green")
        )
      }
    }

    "must have the correct view return action link fulfilled obligation" {
      val obligationData = Seq(obligationDataSingleFulfilled).sortBy(_.dueDate)(Ordering[LocalDate].reverse)
      val table = viewPastReturnsHelper.getReturnsTable(obligationData)
      table.rows.size shouldBe obligationData.size
      obligationData.map(_.periodKey).zip(table.rows).map { case (periodKey, row) =>
        row.actions.head.href shouldBe controllers.returns.routes.ViewReturnController.onPageLoad(periodKey)
      }
    }

    "must return the Due status an open obligation" {
      val obligationData = Seq(obligationDataSingleOpen)
      val table = viewPastReturnsHelper.getReturnsTable(obligationData)
      table.rows.size shouldBe obligationData.size
      table.rows.map { row =>
        row.cells(1).content.asHtml shouldBe new GovukTag()(
          Tag(content = Text(messages("Due")), classes = "govuk-tag--blue")
        )
      }
    }

    "must throw an exception for an invalid period" in new SetUp {
      val obligationData = Seq(invalidPeriodKeyData)
      val exception = intercept[RuntimeException] {
        viewPastReturnsHelper.getReturnsTable(obligationData)
      }
      exception.getMessage shouldBe "Couldn't fetch period from periodKey"
    }

    "must return a sorted table by due date in descending order for Open obligations" in new SetUp {
      val table = viewPastReturnsHelper.getReturnsTable(multipleOpenObligations)
      table.rows.size shouldBe multipleOpenObligations.size
      table.rows.map(row => row.cells.head.content.asHtml.toString()) shouldBe Seq(
        Text("December 2024").asHtml.toString(),
        Text("November 2024").asHtml.toString(),
        Text("October 2024").asHtml.toString(),
        Text("September 2024").asHtml.toString(),
        Text("August 2024").asHtml.toString()
      )
    }

    "must return a sorted table by due date in descending order for fulfilled obligations" in new SetUp {
      val table = viewPastReturnsHelper.getReturnsTable(multipleFulfilledObligations)
      table.rows.size shouldBe multipleFulfilledObligations.size
      table.rows.map(row => row.cells.head.content.asHtml.toString()) shouldBe Seq(
        Text("July 2024").asHtml.toString(),
        Text("June 2024").asHtml.toString(),
        Text("May 2024").asHtml.toString(),
        Text("April 2024").asHtml.toString()
      )
    }*/
  }

}
