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

package controllers.auth

import config.FrontendAppConfig
import controllers.actions.CheckSignedInAction
import forms.auth.DoYouHaveAnAppaIdFormProvider

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, WrappedRequest}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.auth.DoYouHaveAnAppaIdView

class DoYouHaveAnAppaIdController @Inject() (
  override val messagesApi: MessagesApi,
  checkSignedIn: CheckSignedInAction,
  appConfig: FrontendAppConfig,
  formProvider: DoYouHaveAnAppaIdFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DoYouHaveAnAppaIdView
) extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  private def wasReferredFromBTA(request: WrappedRequest[_]) =
    request.headers.get("Referer").exists(_.contains(appConfig.fromBusinessAccountPath))

  def onPageLoad: Action[AnyContent] = checkSignedIn { implicit request =>
    val referredFromBTA = wasReferredFromBTA(request)
    val signedIn        = request.signedIn
    Ok(view(form, referredFromBTA, signedIn))
  }

  def onSubmit(wasReferredFromBTA: Boolean): Action[AnyContent] = checkSignedIn { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => BadRequest(view(formWithErrors, wasReferredFromBTA, request.signedIn)),
        if (_) {
          Redirect(controllers.auth.routes.AppaIdAuthController.onPageLoad())
        } else {
          Redirect(controllers.auth.routes.NoAppaIdController.onPageLoad(wasReferredFromBTA))
        }
      )
  }
}
