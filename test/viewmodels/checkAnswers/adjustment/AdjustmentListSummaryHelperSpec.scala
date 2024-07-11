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
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.adjustment.AdjustmentEntryListPage
import play.api.Application
import play.api.i18n.Messages

import java.time.YearMonth

class AdjustmentListSummaryHelperSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {
  val application: Application    = applicationBuilder().build()
  implicit val messages: Messages = messages(application)

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
  "AdjustmentListSummaryHelper" - {

    "must return a table with the correct head" in {
      val userAnswers = emptyUserAnswers.set(AdjustmentEntryListPage, adjustmentEntryList).success.value
      val table       = AdjustmentListSummaryHelper.adjustmentEntryTable(userAnswers, total)
      table.head.size shouldBe 4
    }

    "must return a table with the correct rows" in {
      val userAnswers = emptyUserAnswers.set(AdjustmentEntryListPage, adjustmentEntryList).success.value
      val table       = AdjustmentListSummaryHelper.adjustmentEntryTable(userAnswers, total)
      table.rows.size shouldBe adjustmentEntryList.size
      table.rows.zipWithIndex.foreach { case (row, index) =>
        row.actions.head.href shouldBe controllers.adjustment.routes.CheckYourAnswersController
          .onPageLoad(Some(index))
        row.actions(1).href   shouldBe controllers.adjustment.routes.DeleteAdjustmentController.onPageLoad(index)
      }
    }

    "must return the correct total" in {
      val userAnswers = emptyUserAnswers.set(AdjustmentEntryListPage, adjustmentEntryList).success.value
      val table       = AdjustmentListSummaryHelper.adjustmentEntryTable(userAnswers, total)
      table.total shouldBe adjustmentEntryList.flatMap(duty => duty.newDuty.orElse(duty.duty)).sum
    }

    "must return the correct total if one of the adjustment entries has an undefined duty" in {
      val expectedSum                  = adjustmentEntryList.flatMap(duty => duty.newDuty.orElse(duty.duty)).sum
      val undefinedDutyAdjustmentEntry = adjustmentEntry.copy(duty = Some(BigDecimal(0)))

      val userAnswers =
        emptyUserAnswers.set(AdjustmentEntryListPage, adjustmentEntryList :+ undefinedDutyAdjustmentEntry).success.value
      val table       = AdjustmentListSummaryHelper.adjustmentEntryTable(userAnswers, total)

      table.total shouldBe expectedSum
    }
  }

}
