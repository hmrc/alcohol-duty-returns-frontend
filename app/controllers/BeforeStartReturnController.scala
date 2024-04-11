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

package controllers

import connectors.CacheConnector
import controllers.actions._
import models.UserAnswers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BeforeStartReturnView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class BeforeStartReturnController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  val controllerComponents: MessagesControllerComponents,
  view: BeforeStartReturnView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(periodKey: String): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))
    for {
      _                  <- cacheConnector.set(userAnswers)
      updatedUserAnswers <- cacheConnector.get(request.userId)
    } yield updatedUserAnswers match {
      case Some(ua) if ua.validUntil.isDefined =>
        Ok(view())
      case _                                   => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
