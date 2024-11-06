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

import connectors.CacheConnector
import controllers.actions._
import models.{CustomAppaIdProvider, JourneyType, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CustomLoginView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomLoginController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  identify: IdentifyWithEnrolmentAction,
  formProvider: CustomAppaIdProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CustomLoginView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider.form

  def onPageLoad(): Action[AnyContent] = Action.async { implicit request =>
    println("AAAAAAAAA")
    Future.successful(Ok(view(form, JourneyType.checkboxItems)))
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    println("BBBBBBBBB")

    form
      .bindFromRequest()
      .fold(
        _ => {
          println("CCCCCCCCC")
          Future.successful(BadRequest("Something Wrong. Bad request"))
        },
        _ => {
          println("DDDDDDDDD")
          Future.successful(Redirect(controllers.routes.TaskListController.onPageLoad))
        }
      )
  }
}
