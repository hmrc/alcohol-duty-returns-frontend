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

package testonly.controllers

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import testonly.connectors.TestOnlyCacheConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TestOnlyController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  testOnlyConnector: TestOnlyCacheConnector
)(implicit val ec: ExecutionContext)
    extends FrontendBaseController {

  def clearAllData(): Action[AnyContent] = Action.async { implicit request =>
    testOnlyConnector.clearAllData().map(httpResponse => Ok(httpResponse.body))
  }

  def clearReturnData(appaId: String, periodKey: String): Action[AnyContent] = Action.async { implicit request =>
    testOnlyConnector.clearReturnData(appaId, periodKey).map(httpResponse => Ok(httpResponse.body))
  }

}
