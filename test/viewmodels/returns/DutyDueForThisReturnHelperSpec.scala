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
import connectors.AlcoholDutyCalculatorConnector
import models.adjustment.AdjustmentDuty
import org.mockito.ArgumentMatchers.any
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import pages.adjustment.DeclareAdjustmentQuestionPage
import pages.returns.DeclareAlcoholDutyQuestionPage
import viewmodels.TableViewModel
import viewmodels.checkAnswers.checkAndSubmit.DutyDueForThisReturnHelper

import scala.concurrent.Future

class DutyDueForThisReturnHelperSpec extends SpecBase with Matchers with ScalaFutures {

  "DutyDueForThisReturnHelper" - {

    "getDutyDueViewModel" - {
      val mockConnector = mock[AlcoholDutyCalculatorConnector]
      val helper = new DutyDueForThisReturnHelper(mockConnector)
      "must return a valid DutyDueForThisReturnViewModel when valid data is provided" in {

        when(mockConnector.calculateTotalAdjustment(any())(any()))
          .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(100))))

        val result = helper.getDutyDueViewModel(fullUserAnswers)(hc,getMessages(app)).value.futureValue

        result mustBe a[Right[_, _]]

        val viewModel = result.toOption.get
        viewModel.totalDue mustBe BigDecimal(100)
        viewModel.dutiesBreakdownTable mustBe a[TableViewModel]
        viewModel.dutiesBreakdownTable.rows.size mustBe 6
      }

      "must return an error message when unable to get duties due" in {

        val result = helper.getDutyDueViewModel(emptyUserAnswers)(hc,getMessages(app)).value.futureValue

        result mustBe Left("")
      }

      "must return an error message when unable to get adjustment totals" in {
        val userAnswers = emptyUserAnswers
          .set(DeclareAlcoholDutyQuestionPage, false)
          .success
          .value
          .set(DeclareAdjustmentQuestionPage, true)
          .success
          .value

        val result = helper.getDutyDueViewModel(userAnswers)(hc,getMessages(app)).value.futureValue

        result mustBe Left("")
      }

      "must return 0 if adjustments and returns are not declared" in {

        val userAnswers = emptyUserAnswers
          .set(DeclareAdjustmentQuestionPage, false)
          .success
          .value
          .set(DeclareAlcoholDutyQuestionPage, false)
          .success
          .value

        when(mockConnector.calculateTotalAdjustment(any())(any()))
          .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(0))))

        val result = helper.getDutyDueViewModel(userAnswers)(hc, getMessages(app)).value.futureValue
        val viewModel = result.toOption.get
        viewModel.totalDue mustBe BigDecimal(0)
      }

      "must return an error message when calculator call for adjustment totals fails" in {

        val errorMessage = "Error Message"

        when(mockConnector.calculateTotalAdjustment(any())(any())).thenReturn(
          Future.failed(new Exception(errorMessage))
        )

        val result = helper.getDutyDueViewModel(fullUserAnswers)(hc, getMessages(app)).value.futureValue

        result mustBe Left(s"Failed to calculate total duty due: $errorMessage")
      }
    }
  }
}