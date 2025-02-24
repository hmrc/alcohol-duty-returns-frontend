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

package models.payments

import base.SpecBase
import generators.ModelGenerators
import models.OutstandingPayment
import models.TransactionType.Return
import models.checkAndSubmit.AdrReturnCreatedDetails
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsResultException, Json}

import java.time.{Instant, LocalDate}

class StartPaymentRequestSpec extends SpecBase with Matchers with ModelGenerators {

  val startPaymentRequest =
    StartPaymentRequest("referenceNumber", BigInt(1045), chargeReference, "/return/url", "/back/url")

  val pastPaymentsStartPaymentRequest = StartPaymentRequest("appaId", BigInt(477334), chargeReference, "/url", "/url")

  val returnDetails = AdrReturnCreatedDetails(
    processingDate = Instant.now(clock),
    amount = BigDecimal(10.45),
    Some(chargeReference),
    Some(paymentDueDate)
  )

  val missingReturnDetails = AdrReturnCreatedDetails(
    processingDate = Instant.now(clock),
    amount = BigDecimal(10.45),
    chargeReference = None,
    Some(paymentDueDate)
  )

  val outstandingPayment = OutstandingPayment(
    Return,
    LocalDate.of(9999, 6, 25),
    Some(chargeReference),
    BigDecimal(4773.34)
  )

  val startPaymentRequestJson = Json.obj(
    "referenceNumber"       -> "referenceNumber",
    "amountInPence"         -> 1045,
    "chargeReferenceNumber" -> chargeReference,
    "returnUrl"             -> "/return/url",
    "backUrl"               -> "/back/url"
  )

  val pastPaymentsStartPaymentRequestJson = Json.obj(
    "referenceNumber"       -> "appaId",
    "amountInPence"         -> 477334,
    "chargeReferenceNumber" -> chargeReference,
    "returnUrl"             -> "/url",
    "backUrl"               -> "/url"
  )

  val startPaymentRequestWithNumericAmountJson = Json.obj(
    "referenceNumber"       -> "referenceNumber",
    "amountInPence"         -> "1045",
    "chargeReferenceNumber" -> chargeReference,
    "returnUrl"             -> "/return/url",
    "backUrl"               -> "/back/url"
  )

  val startPaymentRequestInvalidNumericStringJson = Json.obj(
    "referenceNumber"       -> "referenceNumber",
    "chargeReferenceNumber" -> chargeReference,
    "amountInPence"         -> "32700x",
    "returnUrl"             -> "/return/url",
    "backUrl"               -> "/back/url"
  )

  val startPaymentRequestInvalidAmountTypeJson = Json.obj(
    "referenceNumber"       -> "referenceNumber",
    "chargeReferenceNumber" -> chargeReference,
    "amountInPence"         -> true,
    "returnUrl"             -> "/return/url",
    "backUrl"               -> "/back/url"
  )

  ".formats writes" - {

    "must generate a json representation, including a numeric 'amountInPence' value for return payment" in {
      Json.toJson(startPaymentRequest) mustBe startPaymentRequestJson
    }

    "must generate a json representation, including a numeric 'amountInPence' value for past payment" in {
      Json.toJson(pastPaymentsStartPaymentRequest) mustBe pastPaymentsStartPaymentRequestJson
    }
  }

  ".formats reads" - {

    "must return a new PaymentStart instance" - {

      "when all fields are present & correct" in {
        startPaymentRequestJson.as[StartPaymentRequest]                  mustBe startPaymentRequest
        pastPaymentsStartPaymentRequestJson.as[StartPaymentRequest]      mustBe pastPaymentsStartPaymentRequest
        startPaymentRequestWithNumericAmountJson.as[StartPaymentRequest] mustBe startPaymentRequest
      }
    }

    "must throw an exception if unable to read value as a BigInt" in {
      a[JsResultException] mustBe thrownBy(startPaymentRequestInvalidNumericStringJson.as[StartPaymentRequest])
      a[JsResultException] mustBe thrownBy(startPaymentRequestInvalidAmountTypeJson.as[StartPaymentRequest])
    }
  }
  ".apply" - {

    "must generate a new startPaymentRequest, converting the duty amount from pounds into a pence value for return payment" in {

      StartPaymentRequest.apply(
        returnDetails,
        "referenceNumber",
        "/return/url",
        "/back/url"
      ) mustEqual startPaymentRequest

    }

    "must throw an exception if unable to fetch charge reference for return payment" in {

      a[RuntimeException] mustBe thrownBy(
        StartPaymentRequest.apply(missingReturnDetails, "referenceNumber", "/return/url", "/back/url")
      )

    }

    "must generate a new startPaymentRequest, converting the duty amount from pounds into a pence value for past payment" in {

      StartPaymentRequest.apply(
        outstandingPayment,
        "appaId",
        "/url"
      ) mustEqual pastPaymentsStartPaymentRequest

    }

    "must throw an exception if unable to fetch charge reference for Past payment transaction" in {

      a[RuntimeException] mustBe thrownBy(
        StartPaymentRequest.apply(outstandingPaymentMissingChargeReference, "appaId", "/url")
      )

    }

  }
}
