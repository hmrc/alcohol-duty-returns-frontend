package services

import com.google.inject.{Inject, Singleton}
import play.api.Logging
import play.api.libs.json.Writes
import models.audit.AuditEventDetail
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext

@Singleton
class AuditService @Inject() (auditConnector: AuditConnector)(implicit ec: ExecutionContext) extends Logging {
  def audit[T <: AuditEventDetail](detail: T)(implicit hc: HeaderCarrier, writes: Writes[T]): Unit =
    auditConnector.sendExplicitAudit(detail.auditType, detail)
}
