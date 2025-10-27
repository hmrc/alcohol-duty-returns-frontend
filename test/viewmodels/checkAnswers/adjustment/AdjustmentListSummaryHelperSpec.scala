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
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType._
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, RangeDetailsByRegime, RateBand, RateType}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.adjustment.AdjustmentEntryListPage
import viewmodels.TableRowActionViewModel

import java.time.YearMonth

class AdjustmentListSummaryHelperSpec extends SpecBase with ScalaCheckPropertyChecks {
  implicit val messages = getMessages(app)

  val dutyDue             = BigDecimal(34.2)
  val rate                = BigDecimal(9.27)
  val pureAlcoholVolume   = BigDecimal(3.69)
  val taxCode             = "311"
  val volume              = BigDecimal(10)
  val repackagedRate      = BigDecimal(10)
  val repackagedDuty      = BigDecimal(33.2)
  val newDuty             = BigDecimal(10)
  val rateBand            = RateBand(
    taxCode,
    "some band",
    RateType.Core,
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
  val rateBand2           = RateBand(
    "351",
    "some band",
    RateType.DraughtRelief,
    Some(BigDecimal(8.42)),
    Set(
      RangeDetailsByRegime(
        AlcoholRegime.Beer,
        NonEmptySeq.one(
          ABVRange(
            AlcoholType.Beer,
            AlcoholByVolume(1.3),
            AlcoholByVolume(3.4)
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
  val adjustmentEntry2    = adjustmentEntry.copy(
    adjustmentType = Some(RepackagedDraughtProducts),
    rateBand = Some(rateBand2),
    repackagedRateBand = Some(rateBand),
    pureAlcoholVolume = Some(BigDecimal(10)),
    repackagedDuty = Some(repackagedDuty),
    newDuty = Some(newDuty)
  )
  val adjustmentEntryList = List(adjustmentEntry, adjustmentEntry2)
  val total               = BigDecimal(44.2)
  val pageNumber          = 1

  "AdjustmentListSummaryHelper" - {

    "must return a table with the correct content" in {
      val userAnswers      = emptyUserAnswers.set(AdjustmentEntryListPage, adjustmentEntryList).success.value
      val table            = AdjustmentListSummaryHelper.adjustmentEntryTable(userAnswers, total, pageNumber)
      val totalSummaryList = table.total.get

      val expectedHeader  = List("Adjustment type", "Description", "Duty value", "Action")
      val expectedRows    = List(
        List("Spoilt", "Non-draught beer between 0.1% and 5.8% ABV (tax type code 311)", "£34.20"),
        List("Repackaged", "Draught beer between 1.3% and 3.4% ABV (tax type code 351)", "£10.00")
      )
      val expectedActions = Seq(
        Seq(
          TableRowActionViewModel(
            label = "Change",
            href = controllers.adjustment.routes.CheckYourAnswersController.onPageLoad(Some(0)),
            visuallyHiddenText = Some("Spoilt adjustment with duty value £34.20")
          ),
          TableRowActionViewModel(
            label = "Remove",
            href = controllers.adjustment.routes.DeleteAdjustmentController.onPageLoad(0),
            visuallyHiddenText = Some("Spoilt adjustment with duty value £34.20")
          )
        ),
        Seq(
          TableRowActionViewModel(
            label = "Change",
            href = controllers.adjustment.routes.CheckYourAnswersController.onPageLoad(Some(1)),
            visuallyHiddenText = Some("Repackaged adjustment with duty value £10.00")
          ),
          TableRowActionViewModel(
            label = "Remove",
            href = controllers.adjustment.routes.DeleteAdjustmentController.onPageLoad(1),
            visuallyHiddenText = Some("Repackaged adjustment with duty value £10.00")
          )
        )
      )

      table.head.map(_.content.asHtml.toString)              mustBe expectedHeader
      table.rows.map(_.cells.map(_.content.asHtml.toString)) mustBe expectedRows
      table.rows.map(_.actions)                              mustBe expectedActions

      totalSummaryList.rows.map(_.key.content.asHtml.toString)   mustBe Seq("Total due")
      totalSummaryList.rows.map(_.value.content.asHtml.toString) mustBe Seq("£44.20")
    }

    "must return the correct total if one of the adjustment entries has an undefined duty" in {
      val undefinedDutyAdjustmentEntry = adjustmentEntry.copy(duty = Some(BigDecimal(0)))

      val userAnswers      =
        emptyUserAnswers.set(AdjustmentEntryListPage, adjustmentEntryList :+ undefinedDutyAdjustmentEntry).success.value
      val table            = AdjustmentListSummaryHelper.adjustmentEntryTable(userAnswers, total, pageNumber)
      val totalSummaryList = table.total.get

      totalSummaryList.rows.map(_.key.content.asHtml.toString)   mustBe Seq("Total due")
      totalSummaryList.rows.map(_.value.content.asHtml.toString) mustBe Seq("£44.20")
    }

    "must throw an exception if adjustment type is missing" in {
      val adjustmentEntryWithoutType     = adjustmentEntry.copy(adjustmentType = None)
      val adjustmentEntryListMissingType = List(adjustmentEntryWithoutType)
      val userAnswers                    = emptyUserAnswers.set(AdjustmentEntryListPage, adjustmentEntryListMissingType).success.value
      val exception                      = intercept[RuntimeException] {
        AdjustmentListSummaryHelper.adjustmentEntryTable(userAnswers, total, pageNumber)
      }
      exception.getMessage mustBe "Couldn't fetch adjustment type value from user answers"
    }
  }

}
