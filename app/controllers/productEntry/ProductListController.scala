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

package controllers.productEntry

import controllers.actions._
import forms.productEntry.ProductListFormProvider

import javax.inject.Inject
import models.{Mode, NormalMode, UserAnswers}
import navigation.ProductEntryNavigator
import pages.productEntry.ProductListPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import models.productEntry.ProductEntry
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.SummaryListRowNoValue
import views.html.productEntry.ProductListView

import scala.concurrent.{ExecutionContext, Future}

class ProductListController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cacheConnector: CacheConnector,
                                         navigator: ProductEntryNavigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: ProductListFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: ProductListView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

     val preparedForm = request.userAnswers.get(ProductListPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ProductListPage, value))
            _              <- cacheConnector.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ProductListPage,NormalMode, updatedAnswers))
      )
  }
}
