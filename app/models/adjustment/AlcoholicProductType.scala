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

package models.adjustment

import models.AlcoholRegimes
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import viewmodels.AlcoholRegimesViewOrder.regimesInViewOrder

sealed trait AlcoholicProductType

object AlcoholicProductType {

  def radioOptions(regimes: AlcoholRegimes)(implicit messages: Messages): Seq[RadioItem] = {
    val orderedRegimes = regimesInViewOrder(regimes)
    orderedRegimes.zipWithIndex.map { case (value, _) =>
      RadioItem(
        content = Text(messages(s"alcoholType.$value")),
        value = Some(value.toString),
        id = Some(value.toString)
      )
    }
  }
}
