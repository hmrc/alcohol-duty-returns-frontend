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
import pages.productEntry.{CurrentProductEntryPage, ProductEntryListPage}
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.productEntry.CheckYourAnswersSummaryListHelper
import views.html.productEntry.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val checkYourAnswersHelper = new CheckYourAnswersSummaryListHelper(request.userAnswers)
    checkYourAnswersHelper.currentProductEntrySummaryList match {
      case Some(summaryList) => Ok(view(summaryList))
      case None              => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers.get(CurrentProductEntryPage) match {
      case Some(productEntry) if productEntry.isComplete =>
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.addToSeq(ProductEntryListPage, productEntry))
          _              <- cacheConnector.set(updatedAnswers)
        } yield Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      case Some(productEntry) if !productEntry.isComplete =>
        logger.logger.error("Product Entry not completed")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case _                                             =>
        logger.logger.error("Can't fetch product entry from cache")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

    }

  }

}
