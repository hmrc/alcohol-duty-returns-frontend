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

import connectors.CacheConnector
import controllers.actions._
import models.productEntry.ProductEntry
import pages.productEntry.CurrentProductEntryPage

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import services.productEntry.ProductEntryService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.productEntry.DutyDueView

import scala.concurrent.{ExecutionContext, Future}

class DutyDueController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  authorise: AuthorisedAction,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  productEntryService: ProductEntryService,
  view: DutyDueView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authorise andThen identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        product     <- productEntryService.createProduct(request.userAnswers)
        userAnswers <- Future.fromTry(request.userAnswers.set(CurrentProductEntryPage, product))
        _           <- cacheConnector.set(userAnswers)
      } yield getView(product)
  }

  private def getView(productEntry: ProductEntry)(implicit request: Request[_]): Result = {
    val result = for {
      abv               <- productEntry.abv
      volume            <- productEntry.volume
      pureAlcoholVolume <- productEntry.pureAlcoholVolume
      taxCode           <- productEntry.taxCode
      duty              <- productEntry.duty
      rate              <- productEntry.rate

    } yield Ok(view(abv.value, volume, duty, pureAlcoholVolume, taxCode, rate))

    result.getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

}
