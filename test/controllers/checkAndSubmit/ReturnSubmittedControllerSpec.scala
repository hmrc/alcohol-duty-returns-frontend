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

package controllers.checkAndSubmit

import base.SpecBase
import config.Constants.{adrReturnCreatedDetails, periodKeySessionKey}
import config.FrontendAppConfig
import models.checkAndSubmit.AdrReturnCreatedDetails
import org.mockito.Mockito.when
import play.api.inject
import play.api.libs.json.Json
import play.api.test.Helpers._
import views.html.checkAndSubmit.ReturnSubmittedView

import java.time.{Instant, LocalDate}

class ReturnSubmittedControllerSpec extends SpecBase {

  val currentDate = LocalDate.now()

  val returnDetails = AdrReturnCreatedDetails(
    processingDate = Instant.now(clock),
    amount = BigDecimal(10.45),
    chargeReference = Some("XA1527404500736"),
    paymentDueDate = LocalDate.of(currentDate.getYear, currentDate.getMonth, 25)
  )

  val periodStartDate = dateTimeHelper.formatDateMonthYear(LocalDate.of(2024, 7, 1))
  val periodEndDate   = dateTimeHelper.formatDateMonthYear(LocalDate.of(2024, 7, 31))

  val localDateProcessingDate = dateTimeHelper.instantToLocalDate(Instant.now())
  val formattedProcessingDate = dateTimeHelper.formatDateMonthYear(localDateProcessingDate)

  val formattedPaymentDueDate      = dateTimeHelper.formatDateMonthYear(LocalDate.of(2024, 8, 25))
  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  "ReturnSubmitted Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.checkAndSubmit.routes.ReturnSubmittedController
            .onPageLoad()
            .url
        ).withSession(
          adrReturnCreatedDetails -> Json.toJson(returnDetails).toString()
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReturnSubmittedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          returnDetails,
          periodStartDate,
          periodEndDate,
          formattedProcessingDate,
          formattedPaymentDueDate,
          "-1976AA",
          appConfig.businessTaxAccountUrl
        )(
          request,
          getMessages(application)
        ).toString
      }
    }
  }
}
