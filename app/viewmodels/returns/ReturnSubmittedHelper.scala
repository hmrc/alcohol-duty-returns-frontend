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

import com.google.inject.Inject
import config.Constants.periodKeySessionKey
import config.FrontendAppConfig
import models.ReturnPeriod
import models.checkAndSubmit.AdrReturnCreatedDetails
import models.requests.IdentifierRequest
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.warningtext.WarningText
import viewmodels.{DateTimeHelper, ReturnPeriodViewModel}

case class ReturnSubmittedViewModel(
  returnDetails: AdrReturnCreatedDetails,
  periodStartDate: String,
  periodEndDate: String,
  formattedProcessingDate: String,
  formattedPaymentDueDate: String,
  periodKey: String,
  businessTaxAccountUrl: String,
  warningText: WarningText
)

class ReturnSubmittedHelper @Inject() (dateTimeHelper: DateTimeHelper, appConfig: FrontendAppConfig) {

  def getReturnSubmittedViewModel(returnDetails: AdrReturnCreatedDetails)(implicit
    request: IdentifierRequest[AnyContent],
    messages: Messages
  ): ReturnSubmittedViewModel = {

    val periodKey                          = request.session
      .get(periodKeySessionKey)
      .getOrElse(throw new RuntimeException("There was no period key in the session"))
    val returnPeriod                       = ReturnPeriod
      .fromPeriodKey(periodKey)
      .getOrElse(throw new IllegalArgumentException("Invalid period key was provided. Did not match regex."))
    val returnPeriodViewModel              = ReturnPeriodViewModel(returnPeriod)
    val formattedProcessingDateAsLocalDate = dateTimeHelper.instantToLocalDate(returnDetails.processingDate)
    val formattedProcessingDate            = dateTimeHelper.formatDateMonthYear(formattedProcessingDateAsLocalDate)
    val formattedPaymentDueDate            = returnDetails.paymentDueDate.map(dateTimeHelper.formatDateMonthYear).getOrElse("")

    ReturnSubmittedViewModel(
      returnDetails = returnDetails,
      periodStartDate = returnPeriodViewModel.fromDate,
      periodEndDate = returnPeriodViewModel.toDate,
      formattedProcessingDate = formattedProcessingDate,
      formattedPaymentDueDate = formattedPaymentDueDate,
      periodKey = periodKey,
      businessTaxAccountUrl = appConfig.businessTaxAccountUrl,
      warningText = getWarningText
    )

  }

  private def getWarningText(implicit messages: Messages): WarningText =
    WarningText(
      iconFallbackText = Some("Warning"),
      content = Text(messages("returnSubmitted.warningText"))
    )

}
