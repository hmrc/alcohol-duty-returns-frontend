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

import models.checkAndSubmit.AdrReturnCreatedDetails
import play.api.libs.json.{Format, JsError, JsNumber, JsResult, JsString, JsSuccess, JsValue, Json, OFormat}

case class StartPaymentRequest(
  referenceNumber: String,
  amountInPence: BigInt,
  chargeReferenceNumber: String,
  returnUrl: String,
  backUrl: String
)

object StartPaymentRequest {

  implicit val bigIntFormats: Format[BigInt] = new Format[BigInt] {

    val error = JsError(s"Unable to read value as a BigInt")

    override def reads(json: JsValue): JsResult[BigInt] = json match {
      case JsString(value) =>
        try JsSuccess(scala.math.BigInt(value))
        catch { case _: Throwable => error }

      case JsNumber(value) => JsSuccess(value.toBigInt)
      case _               => error
    }
    override def writes(bigInt: BigInt): JsValue        = JsNumber(BigDecimal(bigInt))
  }

  implicit val formats: OFormat[StartPaymentRequest] = Json.format[StartPaymentRequest]

  def apply(
    returnDetails: AdrReturnCreatedDetails,
    appaId: String,
    returnUrl: String,
    backUrl: String
  ): StartPaymentRequest =
    returnDetails.chargeReference match {
      case Some(chargeReference) =>
        val amountInPence = (returnDetails.amount * 100).toBigInt

        StartPaymentRequest(
          appaId,
          amountInPence,
          chargeReference,
          returnUrl,
          backUrl
        )
      case _                     => throw new RuntimeException("Cannot generate a StartPaymentRequest without any charge reference")
    }
}
