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
import config.FrontendAppConfig
import models.checkAndSubmit.AdrReturnCreatedDetails
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import viewmodels.{DateTimeHelper, ReturnPeriodViewModel, ReturnPeriodViewModelFactory}

import java.time.{Instant, LocalDate}

class ReturnSubmittedHelperSpec extends SpecBase {

  val mockDateTimeHelper               = mock[DateTimeHelper]
  val mockAppConfig                    = mock[FrontendAppConfig]
  val mockReturnPeriodViewModelFactory = mock[ReturnPeriodViewModelFactory]

  val returnSubmittedHelper =
    new ReturnSubmittedHelper(mockDateTimeHelper, mockAppConfig, mockReturnPeriodViewModelFactory)

  val application: Application                         = applicationBuilder().build()
  val messages: Messages                               = getMessages(application)
  val identifierRequest: IdentifierRequest[AnyContent] =
    IdentifierRequest(
      FakeRequest(),
      appaId,
      groupId,
      internalId
    )

  val testReturnDetailsNotOverdue: AdrReturnCreatedDetails     = AdrReturnCreatedDetails(
    processingDate = Instant.now(clock),
    amount = 999.99d,
    chargeReference = Some("Test string"),
    paymentDueDate = Some(LocalDate.of(2024, 6, 25))
  )
  val testReturnDetailsOverdue: AdrReturnCreatedDetails        = AdrReturnCreatedDetails(
    processingDate = Instant.now(clock),
    amount = 999.99d,
    chargeReference = Some("Test string"),
    paymentDueDate = Some(LocalDate.of(2024, 5, 25))
  )
  val testReturnDetailsNegativeAmount: AdrReturnCreatedDetails = AdrReturnCreatedDetails(
    processingDate = Instant.now(clock),
    amount = -999.99d,
    chargeReference = None,
    paymentDueDate = None
  )

  val btaUrl: String                     = "http://localhost:9020/business-account/"
  val formattedDueDateNotOverdue: String = "25 June 2024"
  val formattedDueDateOverdue: String    = "25 May 2024"

  "ReturnSubmittedHelper" - {
    "must return a ReturnSubmittedViewModel with the correct details" - {
      "when given return details with a positive amount and processing date is before payment due date" in {
        when(mockAppConfig.businessTaxAccountUrl).thenReturn(btaUrl)
        when(mockDateTimeHelper.instantToLocalDate(eqTo(Instant.now(clock)))).thenReturn(LocalDate.of(2024, 6, 11))
        when(mockDateTimeHelper.formatDateMonthYear(eqTo(LocalDate.of(2024, 6, 25)))(any()))
          .thenReturn(formattedDueDateNotOverdue)
        when(mockReturnPeriodViewModelFactory.apply(any())(any()))
          .thenReturn(ReturnPeriodViewModel("TEST DATE 1", "TEST DATE 2", "TEST DATE 3"))

        val result =
          returnSubmittedHelper.getReturnSubmittedViewModel(testReturnDetailsNotOverdue)(identifierRequest, messages)

        result.businessTaxAccountUrl mustBe btaUrl
        result.periodKey             mustBe periodKey
        result.isPaymentOverdue      mustBe false
      }

      "when given return details with a positive amount and processing date is after payment due date" in {
        when(mockAppConfig.businessTaxAccountUrl).thenReturn(btaUrl)
        when(mockDateTimeHelper.instantToLocalDate(eqTo(Instant.now(clock)))).thenReturn(LocalDate.of(2024, 6, 11))
        when(mockDateTimeHelper.formatDateMonthYear(eqTo(LocalDate.of(2024, 5, 25)))(any()))
          .thenReturn(formattedDueDateOverdue)
        when(mockReturnPeriodViewModelFactory.apply(any())(any()))
          .thenReturn(ReturnPeriodViewModel("TEST DATE 1", "TEST DATE 2", "TEST DATE 3"))

        val result =
          returnSubmittedHelper.getReturnSubmittedViewModel(testReturnDetailsOverdue)(identifierRequest, messages)

        result.businessTaxAccountUrl mustBe btaUrl
        result.periodKey             mustBe periodKey
        result.isPaymentOverdue      mustBe true
      }

      "when given return details with a negative amount" in {
        val expectedClaimRefundUrl =
          "http://localhost:9195/submissions/new-form/claim-refund-for-overpayment-of-alcohol-duty?amount=999.99"

        when(mockAppConfig.businessTaxAccountUrl).thenReturn(btaUrl)
        when(mockAppConfig.claimRefundGformUrl(eqTo("999.99"))).thenReturn(expectedClaimRefundUrl)
        when(mockDateTimeHelper.instantToLocalDate(eqTo(Instant.now(clock)))).thenReturn(LocalDate.of(2024, 6, 11))
        when(mockDateTimeHelper.formatDateMonthYear(any())(any())).thenReturn(formattedDueDateNotOverdue)
        when(mockReturnPeriodViewModelFactory.apply(any())(any()))
          .thenReturn(ReturnPeriodViewModel("TEST DATE 1", "TEST DATE 2", "TEST DATE 3"))

        val result =
          returnSubmittedHelper.getReturnSubmittedViewModel(testReturnDetailsNegativeAmount)(
            identifierRequest,
            messages
          )

        result.businessTaxAccountUrl mustBe btaUrl
        result.periodKey             mustBe periodKey

        result.formattedPaymentDueDate mustBe ""
        result.isPaymentOverdue        mustBe false
        result.claimRefundUrl          mustBe expectedClaimRefundUrl
      }
    }
  }

}
