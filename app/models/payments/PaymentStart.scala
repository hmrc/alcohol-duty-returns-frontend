package models.payments

import models.checkAndSubmit.AdrReturnCreatedDetails
import play.api.libs.json.{Format, JsError, JsNumber, JsResult, JsString, JsSuccess, JsValue, Json, OFormat}

case class PaymentStart(amountInPence: BigInt, chargeReference: String, returnUrl: String, backUrl: String)
//appaId : String

object PaymentStart {

  implicit val bigIntFormats =  new Format[BigInt] {

    val error = JsError(s"Unable to read value as a BigInt")

    override def reads(json: JsValue): JsResult[BigInt] = json match {
      case JsString(value) => try {
        JsSuccess(scala.math.BigInt(value))
      } catch { case _: Throwable => error }

      case JsNumber(value) => JsSuccess(value.toBigInt)
      case _ => error
    }
    override def writes(bigInt: BigInt): JsValue = JsNumber(BigDecimal(bigInt))
  }

  implicit val formats: OFormat[PaymentStart] = Json.format[PaymentStart]

// TODO: get an appaID from session
  def createPaymentStart(returnDetails : AdrReturnCreatedDetails, returnUrl: String, backUrl: String): PaymentStart = {

    returnDetails match {
      case AdrReturnCreatedDetails(_, amount, Some(chargeReference), _) => {

        val amountInPence = (amount * 100).toBigInt

        PaymentStart(
          amountInPence, chargeReference, returnUrl, backUrl
        )
      }
      case _ => throw new RuntimeException("Cannot generate a PaymentStart without any charge reference")
    }
  }
}
