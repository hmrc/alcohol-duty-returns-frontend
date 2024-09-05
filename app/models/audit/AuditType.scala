package models.audit

import enumeratum._

sealed trait AuditType extends EnumEntry

object AuditType extends Enum[AuditType] {
  val values = findValues

  case object ContinueReturn extends AuditType

  case object PaymentStarted extends AuditType
}
