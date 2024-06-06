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

package viewmodels

import base.SpecBase
import models.{ObligationData, ObligationStatus}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukTag, Tag}

import java.time.LocalDate

class ViewPastReturnsHelperSpec extends SpecBase with ScalaCheckPropertyChecks {
  val application: Application      = applicationBuilder().build()
  implicit val messages: Messages   = messages(application)
  val obligationDataSingleOpen      = ObligationData(
    ObligationStatus.Open,
    LocalDate.of(2024, 5, 1),
    LocalDate.of(2024, 5, 31),
    LocalDate.of(2099, 5, 30),
    "99AE"
  )
  val obligationDataSingleFulfilled = ObligationData(
    ObligationStatus.Fulfilled,
    LocalDate.of(2023, 8, 1),
    LocalDate.of(2023, 8, 1),
    LocalDate.of(2023, 8, 1),
    "24AH"
  )
  val invalidPeriodKeyData          = obligationDataSingleFulfilled.copy(periodKey = invalidPeriodKeyGen.sample.get)

  val multipleOpen = Seq(
    obligationDataSingleOpen.copy(dueDate = LocalDate.of(2024, 11, 30), periodKey = "24AK"),
    obligationDataSingleOpen.copy(dueDate = LocalDate.of(2024, 12, 30), periodKey = "24AL"),
    obligationDataSingleOpen.copy(dueDate = LocalDate.of(2024, 9, 30), periodKey = "24AI"),
    obligationDataSingleOpen,
    obligationDataSingleOpen.copy(dueDate = LocalDate.of(2024, 10, 28), periodKey = "24AJ")
  )

  val multipleFulfilled = Seq(
    obligationDataSingleFulfilled,
    obligationDataSingleFulfilled.copy(dueDate = LocalDate.of(2023, 6, 30), periodKey = "24AF"),
    obligationDataSingleFulfilled.copy(dueDate = LocalDate.of(2023, 7, 30), periodKey = "24AG"),
    obligationDataSingleFulfilled.copy(dueDate = LocalDate.of(2023, 5, 30), periodKey = "24AE")
  )
  "ViewPastReturnsHelper" - {

    "must return a table with the correct head" in {
      val table = ViewPastReturnsHelper.getReturnsTable(Seq(obligationDataSingleOpen))
      table.head.size shouldBe 3
    }

    "must return a table with the correct rows" in {
      val obligationData = Seq(obligationDataSingleOpen)
      val table          = ViewPastReturnsHelper.getReturnsTable(obligationData)
      table.rows.size shouldBe obligationData.size
      table.rows.map { row =>
        row.actions.head.href shouldBe controllers.routes.BeforeStartReturnController.onPageLoad("99AE")
      }
    }
    "must return the COMPLETED status for a fulfilled obligation" in {
      val obligationData = Seq(obligationDataSingleFulfilled)
      val table          = ViewPastReturnsHelper.getReturnsTable(obligationData)
      table.rows.size shouldBe obligationData.size
      table.rows.map { row =>
        row.cells(1).asHtml shouldBe new GovukTag()(
          Tag(content = Text(messages("COMPLETED")), classes = "govuk-tag--green")
        )
      }
    }
    "must return the DUE status an open obligation" in {
      val obligationData = Seq(obligationDataSingleOpen)
      val table          = ViewPastReturnsHelper.getReturnsTable(obligationData)
      table.rows.size shouldBe obligationData.size
      table.rows.map { row =>
        row.cells(1).asHtml shouldBe new GovukTag()(
          Tag(content = Text(messages("DUE")), classes = "govuk-tag--blue")
        )
      }
    }
    "must throw an exception for an invalid period" in {
      val obligationData = Seq(invalidPeriodKeyData)
      val exception      = intercept[RuntimeException] {
        ViewPastReturnsHelper.getReturnsTable(obligationData)
      }
      exception.getMessage shouldBe "Couldn't fetch period from periodKey"
    }

    "must return a sorted table by due date in descending order for Open obligations" in {
      val table = ViewPastReturnsHelper.getReturnsTable(multipleOpen)
      table.rows.size                                         shouldBe multipleOpen.size
      table.rows.map(row => row.cells.head.asHtml.toString()) shouldBe Seq(
        Text("May 2099").asHtml.toString(),
        Text("December 2024").asHtml.toString(),
        Text("November 2024").asHtml.toString(),
        Text("October 2024").asHtml.toString(),
        Text("September 2024").asHtml.toString()
      )
    }
    "must return a sorted table by due date in descending order for fulfilled obligations" in {
      val table = ViewPastReturnsHelper.getReturnsTable(multipleFulfilled)
      table.rows.size                                         shouldBe multipleFulfilled.size
      table.rows.map(row => row.cells.head.asHtml.toString()) shouldBe Seq(
        Text("August 2024").asHtml.toString(),
        Text("July 2024").asHtml.toString(),
        Text("June 2024").asHtml.toString(),
        Text("May 2024").asHtml.toString()
      )
    }

  }

}
