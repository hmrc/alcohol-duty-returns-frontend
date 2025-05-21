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

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

import scala.collection.immutable.Seq

sealed trait AdjustmentType

object AdjustmentType extends Enumerable.Implicits {

  case object Underdeclaration extends WithName("under-declaration") with AdjustmentType
  case object Overdeclaration extends WithName("over-declaration") with AdjustmentType
  case object Spoilt extends WithName("spoilt") with AdjustmentType
  case object RepackagedDraughtProducts extends WithName("repackaged-draught-products") with AdjustmentType
  case object Drawback extends WithName("drawback") with AdjustmentType

  val values: Seq[AdjustmentType] = Seq(
    Spoilt,
    Underdeclaration,
    Overdeclaration,
    RepackagedDraughtProducts,
    Drawback
  )

  def adjustmentTypeOptions(implicit messages: Messages): Seq[RadioItem] = values.map { value =>
    RadioItem(
      content = Text(messages(s"adjustmentType.${value.toString}")),
      value = Some(value.toString),
      id = Some(value.toString),
      hint = Some(Hint(content = Text(messages(s"adjustmentType.${value.toString}.hint"))))
    )
  }

  def getTopRadioId(implicit messages: Messages): String =
    adjustmentTypeOptions.headOption.flatMap(_.id).getOrElse("")

  implicit val enumerable: Enumerable[AdjustmentType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
