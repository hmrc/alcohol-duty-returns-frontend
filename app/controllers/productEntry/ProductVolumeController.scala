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
import forms.productEntry.ProductVolumeFormProvider

import javax.inject.Inject
import models.Mode
import models.productEntry.ProductEntry
import navigation.ProductEntryNavigator
import pages.productEntry.{CurrentProductEntryPage, ProductVolumePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.productEntry.ProductVolumeView

import scala.concurrent.{ExecutionContext, Future}

class ProductVolumeController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ProductEntryNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ProductVolumeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ProductVolumeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val volume = request.userAnswers.get(CurrentProductEntryPage).flatMap(_.volume)

    val preparedForm = volume match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value => {
            val product                      = request.userAnswers.get(CurrentProductEntryPage).getOrElse(ProductEntry())
            val (updatedProduct, hasChanged) = updateProductVolume(product, value)
            for {
              updatedAnswers <-
                Future
                  .fromTry(request.userAnswers.set(CurrentProductEntryPage, updatedProduct.copy(volume = Some(value))))
              _              <- cacheConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ProductVolumePage, mode, updatedAnswers, hasChanged))
          }
        )
  }

  def updateProductVolume(productEntry: ProductEntry, currentValue: BigDecimal): (ProductEntry, Boolean) =
    productEntry.volume match {
      case Some(existingValue) if currentValue == existingValue => (productEntry, false)
      case _                                                    =>
        (
          productEntry.copy(
            volume = None,
            pureAlcoholVolume = None,
            duty = None
          ),
          true
        )
    }
}
