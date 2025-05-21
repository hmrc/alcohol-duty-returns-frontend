/*
 * Copyright 2025 HM Revenue & Customs
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

class ViewPastPaymentsViewModelSpec extends SpecBase {

  val currentYear: Int    = 2024
  val gFormState: Boolean = true

  "ViewPastPaymentsViewModel" - {
    "isDutyBalancePositive" - {
      "must return true if the balance is above zero in the view model" in {
        val viewModel = ViewPastPaymentsViewModel(0.4, currentYear, gFormState)

        viewModel.isDutyBalancePositive mustBe true
      }
      "must return false if the balance is below zero in the view model" in {
        val viewModel = ViewPastPaymentsViewModel(-0.4, currentYear, gFormState)

        viewModel.isDutyBalancePositive mustBe false
      }
      "must return false if the balance is zero in the view model" in {
        val viewModel = ViewPastPaymentsViewModel(0.0, currentYear, gFormState)

        viewModel.isDutyBalancePositive mustBe false
      }
    }

    "isDutyBalanceNegative" - {
      "must return true if the balance is below zero in the view model" in {
        val viewModel = ViewPastPaymentsViewModel(-1.0, currentYear, gFormState)

        viewModel.isDutyBalanceNegative mustBe true
      }
      "must return false if the balance is above zero in the view model" in {
        val viewModel = ViewPastPaymentsViewModel(1.0, currentYear, gFormState)

        viewModel.isDutyBalanceNegative mustBe false
      }
      "must return false if the balance is zero in the view model" in {
        val viewModel = ViewPastPaymentsViewModel(0.0, currentYear, gFormState)

        viewModel.isDutyBalanceNegative mustBe false
      }
    }

    "isDutyBalanceZero" - {
      "must return false if the balance is above zero in the view model" in {
        val viewModel = ViewPastPaymentsViewModel(0.6, currentYear, gFormState)

        viewModel.isDutyBalanceZero mustBe false
      }
      "must return false if the balance is below zero in the view model" in {
        val viewModel = ViewPastPaymentsViewModel(-0.6, currentYear, gFormState)

        viewModel.isDutyBalanceZero mustBe false
      }
      "must return true if the balance is zero in the view model" in {
        val viewModel = ViewPastPaymentsViewModel(0.0, currentYear, gFormState)

        viewModel.isDutyBalanceZero mustBe true
      }
    }
  }

}
