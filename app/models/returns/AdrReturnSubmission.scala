package models.returns

import play.api.libs.json.{Json, OFormat}

import java.time.{Instant, LocalDate}

case class AdrReturnSubmission()

object AdrReturnSubmission {
  implicit val format = Json.format[AdrReturnSubmission]
}


case class AdrReturnCreatedDetails(
  processingDate: Instant,
  amount: BigDecimal,
  chargeReference: Option[String],
  paymentDueDate: LocalDate
)

object AdrReturnCreatedDetails {
  implicit val format: OFormat[AdrReturnCreatedDetails] = Json.format[AdrReturnCreatedDetails]
}