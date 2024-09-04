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

package models.audit

import play.api.libs.json.{Json, OFormat}
import models.audit.AuditType.ContinueReturn

import java.time.Instant

case class AuditContinueReturn(
  appaId: String,
  periodKey: String,
  governmentGatewayId: Option[String],
  governmentGatewayGroupId: Option[String],
  returnContinueTime: Option[Instant],
  returnStartedTime: Option[Instant],
  returnValidUntilTime: Option[Instant]
) extends AuditEventDetail {
  protected val _auditType = ContinueReturn
}

object AuditContinueReturn {
  implicit val format: OFormat[AuditContinueReturn] = Json.format[AuditContinueReturn]
}
