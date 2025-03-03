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
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukTag, Tag}

import java.time.LocalDate

class ViewPastReturnsHelperSpec extends SpecBase with ScalaCheckPropertyChecks {
  val application: Application    = applicationBuilder().build()
  implicit val messages: Messages = getMessages(application)

  val invalidPeriodKeyData = obligationDataSingleFulfilled.copy(periodKey = invalidPeriodKeyGen.sample.get)
  val today                = LocalDate.now(clock)

  "ViewPastReturnsHelper" - {
    "must return a table with the correct head size" in new SetUp {
      val table = viewPastReturnsHelper.getReturnsTable(Seq(obligationDataSingleOpen))
      table.head.size mustBe 3
    }

    "must return a table with the correct rows" in new SetUp {
      val obligationData = Seq(obligationDataSingleOpen)
      val table          = viewPastReturnsHelper.getReturnsTable(obligationData)
      table.rows.size mustBe obligationData.size
      table.rows.foreach { row =>
        row.actions.head.href mustBe controllers.routes.BeforeStartReturnController.onPageLoad(periodKeyAug)
      }
    }

    "must not return a table when outstanding returns are not present" in new SetUp {
      val table =
        viewPastReturnsHelper.getReturnsTable(Seq.empty)
      table.head.size mustBe 0
      table.rows.size mustBe 0
    }

    "must return the Completed status for a fulfilled obligation" in new SetUp {
      val obligationData = Seq(obligationDataSingleFulfilled)
      val table          = viewPastReturnsHelper.getReturnsTable(obligationData)
      table.rows.size mustBe obligationData.size
      table.rows.foreach { row =>
        row.cells(1).content.asHtml mustBe new GovukTag()(
          Tag(content = Text(messages("Completed")), classes = Css.greenTagCssClass)
        )
      }
    }

    "must have the correct view return action link fulfilled obligation" in new SetUp {
      val obligationData = Seq(obligationDataSingleFulfilled).sortBy(_.dueDate)(Ordering[LocalDate].reverse)
      val table          = viewPastReturnsHelper.getReturnsTable(obligationData)
      table.rows.size mustBe obligationData.size
      obligationData.map(_.periodKey).zip(table.rows).map { case (periodKey, row) =>
        row.actions.head.href mustBe controllers.returns.routes.ViewReturnController.onPageLoad(periodKey)
      }
    }

    "must return the Due status for an open obligation and the dueDate is today" in new SetUp {
      val obligationData = Seq(obligationDataSingleOpenDueToday(today))
      val table          = viewPastReturnsHelper.getReturnsTable(obligationData)
      table.rows.size mustBe obligationData.size
      table.rows.foreach { row =>
        row.cells(1).content.asHtml mustBe new GovukTag()(
          Tag(content = Text(messages("Due")), classes = Css.blueTagCssClass)
        )
      }
    }

    "must return the Overdue status for an open obligation where the dueDate is before today" in new SetUp {
      val obligationData = Seq(obligationDataSingleOpenOverdue(today))
      val table          = viewPastReturnsHelper.getReturnsTable(obligationData)
      table.rows.size mustBe obligationData.size
      table.rows.map { row =>
        row.cells(1).content.asHtml mustBe new GovukTag()(
          Tag(content = Text(messages("Overdue")), classes = Css.redTagCssClass)
        )
      }
    }

    "must throw an exception for an invalid period" in new SetUp {
      val obligationData = Seq(invalidPeriodKeyData)
      val exception      = intercept[RuntimeException] {
        viewPastReturnsHelper.getReturnsTable(obligationData)
      }
      exception.getMessage mustBe "Couldn't fetch period from periodKey"
    }

    "must return a sorted table by due date in descending order for Open obligations" in new SetUp {
      val table = viewPastReturnsHelper.getReturnsTable(multipleOpenObligations)
      table.rows.size                                                 mustBe multipleOpenObligations.size
      table.rows.map(row => row.cells.head.content.asHtml.toString()) mustBe Seq(
        Text("December 2024").asHtml.toString(),
        Text("November 2024").asHtml.toString(),
        Text("October 2024").asHtml.toString(),
        Text("September 2024").asHtml.toString(),
        Text("August 2024").asHtml.toString()
      )
    }

    "must return a sorted table by due date in descending order for fulfilled obligations" in new SetUp {
      val table = viewPastReturnsHelper.getReturnsTable(multipleFulfilledObligations)
      table.rows.size                                                 mustBe multipleFulfilledObligations.size
      table.rows.map(row => row.cells.head.content.asHtml.toString()) mustBe Seq(
        Text("July 2024").asHtml.toString(),
        Text("June 2024").asHtml.toString(),
        Text("May 2024").asHtml.toString(),
        Text("April 2024").asHtml.toString()
      )
    }
  }

  class SetUp {
    val viewPastReturnsHelper = new ViewPastReturnsHelper(createDateTimeHelper(), clock)
  }
}
