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

import play.api.i18n.Messages

object Money {
  private val minus: Char = 0x2212

  /**
    * Note: This is intended to work with values that are already to 2dp. The rounding rules for
    * tax may be up or down depending on the context and this code will round to nearest.
    */
  def format(amount: BigDecimal)(implicit messages: Messages): String =
    if (amount < 0) {
      s"$minus${messages("site.currency.2DP", amount.abs)}"
    } else {
      messages("site.currency.2DP", amount)
    }
}
