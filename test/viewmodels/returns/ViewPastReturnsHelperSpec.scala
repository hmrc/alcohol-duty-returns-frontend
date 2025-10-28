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
import config.Constants.Css
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukTag, Tag}
import viewmodels.TableRowActionViewModel

import java.time.LocalDate

class ViewPastReturnsHelperSpec extends SpecBase with ScalaCheckPropertyChecks {
  implicit val messages: Messages = getMessages(app)

  val invalidPeriodKeyData = obligationDataSingleFulfilled.copy(periodKey = invalidPeriodKeyGen.sample.get)
  val today                = LocalDate.now(clock)

  "ViewPastReturnsHelper" - {
    "must not return a table when no obligations are present" in new SetUp {
      val table = viewPastReturnsHelper.getReturnsTable(Seq.empty)
      table.head.size mustBe 0
      table.rows.size mustBe 0
    }

    "must return the Completed status for a fulfilled obligation" in new SetUp {
      val obligationData = Seq(obligationDataSingleFulfilled)
      val table          = viewPastReturnsHelper.getReturnsTable(obligationData)
      table.rows.size mustBe obligationData.size
      table.rows.foreach { row =>
        row.cells(1).content.asHtml mustBe completedTag
      }
    }

    "must return the Due status for an open obligation where the dueDate is today" in new SetUp {
      val obligationData = Seq(obligationDataSingleOpenDueToday(today))
      val table          = viewPastReturnsHelper.getReturnsTable(obligationData)
      table.rows.size mustBe obligationData.size
      table.rows.foreach { row =>
        row.cells(1).content.asHtml mustBe dueTag
      }
    }

    "must return the Overdue status for an open obligation where the dueDate is before today" in new SetUp {
      val obligationData = Seq(obligationDataSingleOpenOverdue(today))
      val table          = viewPastReturnsHelper.getReturnsTable(obligationData)
      table.rows.size mustBe obligationData.size
      table.rows.map { row =>
        row.cells(1).content.asHtml mustBe overdueTag
      }
    }

    "must return a sorted table by due date in descending order for Open obligations" in new SetUp {
      val table = viewPastReturnsHelper.getReturnsTable(multipleOpenObligations)

      val expectedRows = Seq(
        Seq("December 2024", dueTag.toString),
        Seq("November 2024", dueTag.toString),
        Seq("October 2024", dueTag.toString),
        Seq("September 2024", dueTag.toString),
        Seq("August 2024", dueTag.toString)
      )

      val expectedActions = Seq(
        TableRowActionViewModel(
          label = "Complete return",
          href = controllers.routes.BeforeStartReturnController.onPageLoad("24AL"),
          visuallyHiddenText = Some("for December 2024")
        ),
        TableRowActionViewModel(
          label = "Complete return",
          href = controllers.routes.BeforeStartReturnController.onPageLoad("24AK"),
          visuallyHiddenText = Some("for November 2024")
        ),
        TableRowActionViewModel(
          label = "Complete return",
          href = controllers.routes.BeforeStartReturnController.onPageLoad("24AJ"),
          visuallyHiddenText = Some("for October 2024")
        ),
        TableRowActionViewModel(
          label = "Complete return",
          href = controllers.routes.BeforeStartReturnController.onPageLoad("24AI"),
          visuallyHiddenText = Some("for September 2024")
        ),
        TableRowActionViewModel(
          label = "Complete return",
          href = controllers.routes.BeforeStartReturnController.onPageLoad("24AH"),
          visuallyHiddenText = Some("for August 2024")
        )
      )

      table.head.map(_.content.asHtml.toString)              mustBe expectedHeader
      table.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedRows
      table.rows.map(_.actions.head)                         mustBe expectedActions
    }

    "must return a sorted table by due date in descending order for fulfilled obligations" in new SetUp {
      val table = viewPastReturnsHelper.getReturnsTable(multipleFulfilledObligations)

      val expectedRows = Seq(
        Seq("July 2024", completedTag.toString),
        Seq("June 2024", completedTag.toString),
        Seq("May 2024", completedTag.toString),
        Seq("April 2024", completedTag.toString)
      )

      val expectedActions = Seq(
        TableRowActionViewModel(
          label = "View return",
          href = controllers.returns.routes.ViewReturnController.onPageLoad("24AG"),
          visuallyHiddenText = Some("for July 2024")
        ),
        TableRowActionViewModel(
          label = "View return",
          href = controllers.returns.routes.ViewReturnController.onPageLoad("24AF"),
          visuallyHiddenText = Some("for June 2024")
        ),
        TableRowActionViewModel(
          label = "View return",
          href = controllers.returns.routes.ViewReturnController.onPageLoad("24AE"),
          visuallyHiddenText = Some("for May 2024")
        ),
        TableRowActionViewModel(
          label = "View return",
          href = controllers.returns.routes.ViewReturnController.onPageLoad("24AD"),
          visuallyHiddenText = Some("for April 2024")
        )
      )

      table.head.map(_.content.asHtml.toString)              mustBe expectedHeader
      table.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedRows
      table.rows.map(_.actions.head)                         mustBe expectedActions
    }

    "must throw an exception for an invalid period" in new SetUp {
      val obligationData = Seq(invalidPeriodKeyData)
      val exception      = intercept[RuntimeException] {
        viewPastReturnsHelper.getReturnsTable(obligationData)
      }
      exception.getMessage mustBe "Couldn't fetch period from periodKey"
    }
  }

  class SetUp {
    val viewPastReturnsHelper = new ViewPastReturnsHelper(createDateTimeHelper(), clock)

    val expectedHeader = Seq("Period", "Status", "Action")

    val completedTag = new GovukTag()(
      Tag(content = Text("Completed"), classes = Css.greenTagCssClass)
    )
    val dueTag       = new GovukTag()(
      Tag(content = Text("Due"), classes = Css.blueTagCssClass)
    )
    val overdueTag   = new GovukTag()(
      Tag(content = Text("Overdue"), classes = Css.redTagCssClass)
    )
  }
}
