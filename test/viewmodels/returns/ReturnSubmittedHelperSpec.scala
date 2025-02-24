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

  val testReturnDetails: AdrReturnCreatedDetails = AdrReturnCreatedDetails(
    processingDate = Instant.now(clock),
    amount = 999.99d,
    chargeReference = Some("Test string"),
    paymentDueDate = Some(LocalDate.now(clock).plusYears(1))
  )
  val btaUrl: String                             = "http://localhost:9020/business-account/"
  val formattedDate: String                      = "27 August 2019"

  "ReturnSubmittedHelper" - {
    "must return a ReturnSubmittedViewModel" - {
      "when given return details and a valid period key" in {
        when(mockAppConfig.businessTaxAccountUrl).thenReturn(btaUrl)
        when(mockDateTimeHelper.formatDateMonthYear(any())(any())).thenReturn(formattedDate)
        when(mockDateTimeHelper.instantToLocalDate(any())).thenReturn(LocalDate.of(1, 1, 1))
        when(mockReturnPeriodViewModelFactory.apply(any())(any()))
          .thenReturn(ReturnPeriodViewModel("TEST DATE 1", "TEST DATE 2", "TEST DATE 3"))

        val result = returnSubmittedHelper.getReturnSubmittedViewModel(testReturnDetails)(identifierRequest, messages)

        result.businessTaxAccountUrl mustBe btaUrl
        result.periodKey             mustBe periodKey
      }
    }
  }

}
