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

package viewmodels.checkAnswers.adjustment

import base.SpecBase
import cats.data.NonEmptySeq
import generators.ModelGenerators
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.Spoilt
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, RangeDetailsByRegime, RateBand, RateType}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.adjustment.AdjustmentEntryListPage
import viewmodels.Money
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

import java.time.YearMonth

class AdjustmentListSummaryHelperSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {
  val dutyDue             = BigDecimal(34.2)
  val rate                = BigDecimal(9.27)
  val pureAlcoholVolume   = BigDecimal(3.69)
  val taxCode             = "311"
  val volume              = BigDecimal(10)
  val repackagedRate      = BigDecimal(10)
  val repackagedDuty      = BigDecimal(33.2)
  val newDuty             = BigDecimal(1)
  val rateBand            = RateBand(
    "310",
    "some band",
    RateType.DraughtRelief,
    Some(rate),
    Set(
      RangeDetailsByRegime(
        AlcoholRegime.Beer,
        NonEmptySeq.one(
          ABVRange(
            AlcoholType.Beer,
            AlcoholByVolume(0.1),
            AlcoholByVolume(5.8)
          )
        )
      )
    )
  )
  val adjustmentEntry     = AdjustmentEntry(
    pureAlcoholVolume = Some(pureAlcoholVolume),
    totalLitresVolume = Some(volume),
    rateBand = Some(rateBand),
    duty = Some(dutyDue),
    adjustmentType = Some(Spoilt),
    period = Some(YearMonth.of(24, 1))
  )
  val adjustmentEntry2    = adjustmentEntry.copy(pureAlcoholVolume = Some(BigDecimal(10)), newDuty = Some(BigDecimal(10)))
  val adjustmentEntryList = List(adjustmentEntry, adjustmentEntry2)
  val total               = BigDecimal(44.2)
  val pageNumber          = 1
  "AdjustmentListSummaryHelper" - {

    "must return a table with the correct head" in {
      val userAnswers = emptyUserAnswers.set(AdjustmentEntryListPage, adjustmentEntryList).success.value
      val table       =
        new AdjustmentListSummaryHelper().adjustmentEntryTable(userAnswers, total, pageNumber)(getMessages(app))
      table.head.size mustBe 4
    }

    "must return a table with the correct rows" in {
      val userAnswers = emptyUserAnswers.set(AdjustmentEntryListPage, adjustmentEntryList).success.value
      val table       =
        new AdjustmentListSummaryHelper().adjustmentEntryTable(userAnswers, total, pageNumber)(getMessages(app))
      table.rows.size mustBe adjustmentEntryList.size
      table.rows.zipWithIndex.foreach { case (row, index) =>
        row.actions.head.href mustBe controllers.adjustment.routes.CheckYourAnswersController
          .onPageLoad(Some(index))
        row.actions(1).href   mustBe controllers.adjustment.routes.DeleteAdjustmentController.onPageLoad(index)
      }
    }

    "must return the correct total" in {
      val userAnswers = emptyUserAnswers.set(AdjustmentEntryListPage, adjustmentEntryList).success.value
      val table       =
        new AdjustmentListSummaryHelper().adjustmentEntryTable(userAnswers, total, pageNumber)(getMessages(app))
      table.total.map(_.rows.head.value).get.content mustBe Text(
        Money.format(
          adjustmentEntryList.flatMap(duty => duty.newDuty.orElse(duty.duty)).sum
        )(getMessages(app))
      )
    }

    "must return the correct total if one of the adjustment entries has an undefined duty" in {
      val expectedSum                  = adjustmentEntryList.flatMap(duty => duty.newDuty.orElse(duty.duty)).sum
      val undefinedDutyAdjustmentEntry = adjustmentEntry.copy(duty = Some(BigDecimal(0)))

      val userAnswers =
        emptyUserAnswers.set(AdjustmentEntryListPage, adjustmentEntryList :+ undefinedDutyAdjustmentEntry).success.value
      val table       =
        new AdjustmentListSummaryHelper().adjustmentEntryTable(userAnswers, total, pageNumber)(getMessages(app))

      table.total.map(_.rows.head.value).get.content mustBe Text(Money.format(expectedSum)(getMessages(app)))
    }

    "must throw an exception if adjustment type is missing" in {
      val adjustmentEntryWithoutType     = adjustmentEntry.copy(adjustmentType = None)
      val adjustmentEntryListMissingType = List(adjustmentEntryWithoutType)
      val userAnswers                    = emptyUserAnswers.set(AdjustmentEntryListPage, adjustmentEntryListMissingType).success.value
      val exception                      = intercept[RuntimeException] {
        new AdjustmentListSummaryHelper().adjustmentEntryTable(userAnswers, total, pageNumber)(getMessages(app))
      }
      exception.getMessage mustBe "Couldn't fetch adjustment type value from user answers"
    }
  }

}
