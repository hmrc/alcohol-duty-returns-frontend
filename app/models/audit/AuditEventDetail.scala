package models.audit

trait AuditEventDetail {
  protected val _auditType: AuditType
  lazy val auditType = _auditType.entryName
}
