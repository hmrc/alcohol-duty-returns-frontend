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
import connectors.AlcoholDutyCalculatorConnector
import models.adjustment.{AdjustmentDuty, AdjustmentEntry}
import models.adjustment.AdjustmentType.{Overdeclaration, Underdeclaration}
import org.mockito.ArgumentMatchers.any
import pages.adjustment.{AdjustmentEntryListPage, OverDeclarationReasonPage, OverDeclarationTotalPage, UnderDeclarationReasonPage, UnderDeclarationTotalPage}
import play.api.Application
import play.api.i18n.Messages

import scala.concurrent.Future

class AdjustmentOverUnderDeclarationCalculationHelperSpec extends SpecBase {
  val application: Application                        = applicationBuilder().build()
  implicit val messages: Messages                     = getMessages(application)
  val mockAlcoholDutyCalculatorConnector              = mock[AlcoholDutyCalculatorConnector]
  val adjustmentOverUnderDeclarationCalculationHelper = new AdjustmentOverUnderDeclarationCalculationHelper(
    mockAlcoholDutyCalculatorConnector
  )
  val underDeclarationDuties                          = Seq(BigDecimal(200), BigDecimal(300))
  val overDeclarationDuties                           = Seq(BigDecimal(100))
  val underDeclarationTotal                           = AdjustmentDuty(BigDecimal(500))
  val overDeclarationTotal                            = AdjustmentDuty(BigDecimal(100))
  val adjustmentEntry                                 = AdjustmentEntry(
    adjustmentType = Some(Underdeclaration),
    duty = Some(BigDecimal(200))
  )
  val adjustmentEntry2                                = adjustmentEntry.copy(adjustmentType = Some(Overdeclaration), duty = Some(BigDecimal(100)))
  val adjustmentEntry3                                = adjustmentEntry.copy(duty = Some(BigDecimal(300)))
  val adjustmentEntryList                             = List(adjustmentEntry, adjustmentEntry2, adjustmentEntry3)
  val userAnswers                                     = emptyUserAnswers.set(AdjustmentEntryListPage, adjustmentEntryList).success.value
  "AdjustmentOverUnderDeclarationCalculationHelperSpec" - {

    "must calculate totals and update userAnswers correctly" in {

      val expectedUserAnswers = userAnswers
        .set(UnderDeclarationTotalPage, underDeclarationTotal.duty)
        .success
        .value
        .set(OverDeclarationTotalPage, overDeclarationTotal.duty)
        .success
        .value
      when(mockAlcoholDutyCalculatorConnector.calculateTotalAdjustment(underDeclarationDuties))
        .thenReturn(Future.successful(underDeclarationTotal))
      when(mockAlcoholDutyCalculatorConnector.calculateTotalAdjustment(overDeclarationDuties))
        .thenReturn(Future.successful(overDeclarationTotal))

      whenReady(adjustmentOverUnderDeclarationCalculationHelper.fetchOverUnderDeclarationTotals(userAnswers, false)) {
        result =>
          result mustBe expectedUserAnswers
      }
    }

    "must removes reason page if total is below threshold" in {
      val updatedUserAnswersWithReason = emptyUserAnswers
        .set(OverDeclarationReasonPage, "test")
        .success
        .value

      val expectedUserAnswers = emptyUserAnswers
        .set(UnderDeclarationTotalPage, BigDecimal(0))
        .success
        .value
        .set(OverDeclarationTotalPage, BigDecimal(0))
        .success
        .value
      whenReady(
        adjustmentOverUnderDeclarationCalculationHelper
          .fetchOverUnderDeclarationTotals(updatedUserAnswersWithReason, false)
      ) { result =>
        result mustBe expectedUserAnswers
      }
    }

    "must not remove reason page if total is above threshold" in {
      val adjustmentEntry4             = adjustmentEntry.copy(duty = Some(BigDecimal(1000)))
      val adjustmentEntryList          = List(adjustmentEntry, adjustmentEntry2, adjustmentEntry3, adjustmentEntry4)
      val updatedUserAnswersWithReason = userAnswers
        .set(AdjustmentEntryListPage, adjustmentEntryList)
        .success
        .value
        .set(UnderDeclarationReasonPage, "test")
        .success
        .value

      val expectedUserAnswers = updatedUserAnswersWithReason
        .set(UnderDeclarationTotalPage, BigDecimal(1500))
        .success
        .value
        .set(OverDeclarationTotalPage, overDeclarationTotal.duty)
        .success
        .value

      when(mockAlcoholDutyCalculatorConnector.calculateTotalAdjustment(any())(any()))
        .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(1500))))
      when(mockAlcoholDutyCalculatorConnector.calculateTotalAdjustment(overDeclarationDuties))
        .thenReturn(Future.successful(overDeclarationTotal))

      whenReady(
        adjustmentOverUnderDeclarationCalculationHelper
          .fetchOverUnderDeclarationTotals(updatedUserAnswersWithReason, false)
      ) { result =>
        result mustBe expectedUserAnswers
      }
    }
  }
}
