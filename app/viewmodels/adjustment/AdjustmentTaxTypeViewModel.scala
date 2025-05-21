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

package viewmodels.adjustment

import models.adjustment.AdjustmentType
import models.adjustment.AdjustmentType._
import play.api.i18n.Messages

import javax.inject.Inject

case class AdjustmentTaxTypeViewModel(adjustmentType: AdjustmentType) {

  def getTitleContent(implicit messages: Messages): String =
    if (adjustmentType == RepackagedDraughtProducts) {
      messages("adjustmentTaxType.repackaged.title")
    } else {
      messages("adjustmentTaxType.title")
    }

  def getLabel(implicit messages: Messages): String =
    if (adjustmentType == RepackagedDraughtProducts) {
      messages("adjustmentTaxType.repackaged.heading")
    } else {
      messages("adjustmentTaxType.heading")
    }

  def getHint(implicit messages: Messages): String =
    if (adjustmentType == Underdeclaration) {
      messages("adjustmentTaxType.underDeclaration.hint")
    } else {
      messages("adjustmentTaxType.default.hint")
    }

}

class AdjustmentTaxTypeHelper @Inject() () {

  def createViewModel(adjustmentType: AdjustmentType): AdjustmentTaxTypeViewModel =
    AdjustmentTaxTypeViewModel(adjustmentType = adjustmentType)

}
