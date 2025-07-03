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

import config.FrontendAppConfig
import models.adjustment.AdjustmentType
import models.adjustment.AdjustmentType._
import play.api.i18n.Messages

import javax.inject.Inject

case class AdjustmentReturnPeriodViewModel(adjustmentType: AdjustmentType, exciseEnquiriesUrl: String) {

  def getHeading(implicit messages: Messages): String =
    if (adjustmentType == Underdeclaration) {
      messages("whenDidYouPayDuty.under-declaration.title")
    } else if (adjustmentType == Spoilt) {
      throw new IllegalArgumentException("Not expecting WhenDidYouPayDuty to be asked for Spoilt")
    } else {
      messages("whenDidYouPayDuty.default.title")
    }

}

class AdjustmentReturnPeriodHelper @Inject() (appConfig: FrontendAppConfig) {

  def createViewModel(adjustmentType: AdjustmentType): AdjustmentReturnPeriodViewModel =
    AdjustmentReturnPeriodViewModel(adjustmentType = adjustmentType, exciseEnquiriesUrl = appConfig.exciseEnquiriesUrl)

}
