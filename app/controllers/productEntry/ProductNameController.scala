/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.productEntry

import connectors.CacheConnector
import controllers.actions._
import forms.productEntry.ProductNameFormProvider

import javax.inject.Inject
import models.Mode
import models.productEntry.ProductEntry
import navigation.ProductEntryNavigator
import pages.productEntry.{CurrentProductEntryPage, ProductNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.productEntry.ProductNameView

import scala.concurrent.{ExecutionContext, Future}

class ProductNameController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ProductEntryNavigator,
  authorise: AuthorisedAction,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ProductNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ProductNameView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen identify andThen getData andThen requireData) {
    implicit request =>
      val name = request.userAnswers.get(CurrentProductEntryPage).flatMap(_.name)

      val preparedForm = name match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen identify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value => {
            val product = request.userAnswers.get(CurrentProductEntryPage).getOrElse(ProductEntry())
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(CurrentProductEntryPage, product.copy(name = Some(value))))
              _              <- cacheConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ProductNamePage, mode, updatedAnswers))
          }
        )
    }
}
