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

package viewmodels.declareDuty

import models.AlcoholRegimes
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.AlcoholRegimesViewOrder.regimesInViewOrder
import viewmodels.govuk.checkbox._

object AlcoholTypeViewModel {

  def checkboxItems(regimes: AlcoholRegimes)(implicit messages: Messages): Seq[CheckboxItem] = {
    val orderedRegimes = regimesInViewOrder(regimes)
    orderedRegimes.zipWithIndex.map { case (regime, index) =>
      CheckboxItemViewModel(
        content = Text(messages(regime.regimeMessageKey).capitalize),
        fieldId = "value",
        index = index,
        value = regime.toString
      )
    }
  }
}
