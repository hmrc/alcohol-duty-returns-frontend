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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait SpiritType

object SpiritType extends Enumerable.Implicits {

  case object Maltspirits extends WithName("maltSpirits") with SpiritType
  case object Grainspirits extends WithName("grainSpirits") with SpiritType
  case object NeutralAgriculturalOrigin extends WithName("neutralAgriculturalOrigin") with SpiritType
  case object NeutralIndustrialOrigin extends WithName("neutralIndustrialOrigin") with SpiritType
  case object Beer extends WithName("beer") with SpiritType
  case object WineOrMadeWine extends WithName("wineOrMadeWine") with SpiritType
  case object CiderOrPerry extends WithName("ciderOrPerry") with SpiritType
  case object Other extends WithName("other") with SpiritType

  val values: Seq[SpiritType] = Seq(
    Maltspirits,
    Grainspirits,
    NeutralAgriculturalOrigin,
    NeutralIndustrialOrigin,
    Beer,
    WineOrMadeWine,
    CiderOrPerry,
    Other
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map { case (value, index) =>
      CheckboxItemViewModel(
        content = Text(messages(s"spiritType.${value.toString}")),
        fieldId = "value",
        index = index,
        value = value.toString
      )
    }

  implicit val enumerable: Enumerable[SpiritType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
