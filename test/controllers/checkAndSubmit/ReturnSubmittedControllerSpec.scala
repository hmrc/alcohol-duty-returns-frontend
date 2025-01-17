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
import config.Constants.returnCreatedDetailsKey
import models.checkAndSubmit.AdrReturnCreatedDetails
import org.mockito.ArgumentMatchers.any
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.Helpers._
import viewmodels.DateTimeHelper
import viewmodels.returns.ReturnSubmittedHelper

import java.time.{Instant, LocalDate}

class ReturnSubmittedControllerSpec extends SpecBase {

  val returnDetails = AdrReturnCreatedDetails(
    processingDate = Instant.now(clock),
    amount = BigDecimal(10.45),
    Some(chargeReference),
    Some(paymentDueDate)
  )

  val mockReturnSubmittedHelper: ReturnSubmittedHelper = mock[ReturnSubmittedHelper]

  "ReturnSubmitted Controller" - {

    "must return OK and the correct view for a GET" in {
      val application                 = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ReturnSubmittedHelper].toInstance(mockReturnSubmittedHelper))
        .build()
      implicit val messages: Messages = getMessages(application)
      val testViewModel               = returnSubmittedViewModel(application.injector.instanceOf[DateTimeHelper])
      when(mockReturnSubmittedHelper.getReturnSubmittedViewModel(any())(any(), any()))
        .thenReturn(testViewModel)

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.checkAndSubmit.routes.ReturnSubmittedController
            .onPageLoad()
            .url
        ).withSession(
          returnCreatedDetailsKey -> Json.toJson(returnDetails).toString()
        )

        val result = route(application, request).value

        status(result) mustEqual OK
        // TODO: add test for the view with correct period key
      }
    }

    "must redirect in the Journey Recovery screen if the return details in session is empty" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ReturnSubmittedHelper].toInstance(mockReturnSubmittedHelper))
        .build()

      running(application) {
        val request =
          FakeRequestWithoutSession(GET, controllers.checkAndSubmit.routes.ReturnSubmittedController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

      }
    }

    "return details" - {
      "should deserialise a valid json into a adrReturnCreatedDetails object" in {
        val json =
          """
            |{
            |"processingDate":"2024-06-11T15:07:47.838Z",
            |"amount":10.45,
            |"chargeReference":"XA1527404500736",
            |"paymentDueDate":"2024-08-25"
            |}
            |
            |""".stripMargin

        val result = Json.parse(json).validate[AdrReturnCreatedDetails].get

        result mustBe a[AdrReturnCreatedDetails]
        result.processingDate mustBe Instant.parse("2024-06-11T15:07:47.838Z")
        result.amount mustBe 10.45
        result.chargeReference mustBe Some("XA1527404500736")
        result.paymentDueDate mustBe Some(LocalDate.parse("2024-08-25"))
      }

      "should deserialise a valid json into a adrReturnCreatedDetails object with minimal payload" in {
        val json =
          """
            |{
            |"processingDate":"2024-06-11T15:07:47.838Z",
            |"amount":10.45
            |}
            |
            |""".stripMargin

        val result = Json.parse(json).validate[AdrReturnCreatedDetails].get

        result mustBe a[AdrReturnCreatedDetails]
        result.processingDate mustBe Instant.parse("2024-06-11T15:07:47.838Z")
        result.amount mustBe 10.45
        result.chargeReference mustBe None
        result.paymentDueDate mustBe None
      }

      "should redirect to journey recovery if not valid" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[ReturnSubmittedHelper].toInstance(mockReturnSubmittedHelper))
          .build()

        val request =
          FakeRequest(GET, controllers.checkAndSubmit.routes.ReturnSubmittedController.onPageLoad().url)
            .withSession(returnCreatedDetailsKey -> "{}")
        val result  = route(application, request).get

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
