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
import models.UserAnswers
import models.productEntry.ProductEntry
import models.requests.DataRequest
import pages.productEntry.{CurrentProductEntryPage, ProductEntryListPage}
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
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

  def onPageLoad(index: Option[Int] = None): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getProductEntry(request.userAnswers, index)
        .flatMap { productEntry =>
          CheckYourAnswersSummaryListHelper.currentProductEntrySummaryList(productEntry).map { summaryList =>
            setCurrentProductEntry(request.userAnswers, productEntry, summaryList)
          }
        }
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers.get(CurrentProductEntryPage) match {
      case Some(productEntry) if productEntry.isComplete  =>
        for {
          updatedAnswers <- saveProductEntry(request.userAnswers, productEntry)
          cleanedAnswers <- Future.fromTry(updatedAnswers.remove(CurrentProductEntryPage))
          _              <- cacheConnector.set(cleanedAnswers)
        } yield Redirect(controllers.productEntry.routes.ProductListController.onPageLoad())
      case Some(productEntry) if !productEntry.isComplete =>
        logger.logger.error("Product Entry not completed")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case _                                              =>
        logger.logger.error("Can't fetch product entry from cache")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  private def getProductEntry(answers: UserAnswers, maybeInt: Option[Int]): Option[ProductEntry] =
    maybeInt
      .flatMap(i => answers.getByIndex(ProductEntryListPage, i))
      .orElse(answers.get(CurrentProductEntryPage))
      .map(pe => if (pe.index.isDefined) pe else pe.copy(index = maybeInt))

  private def saveProductEntry(
    answers: UserAnswers,
    productEntry: ProductEntry
  ): Future[UserAnswers] =
    productEntry.index match {
      case Some(i) => Future.fromTry(answers.setByIndex(ProductEntryListPage, i, productEntry.copy(index = None)))
      case None    => Future.fromTry(answers.addToSeq(ProductEntryListPage, productEntry))
    }

  private def setCurrentProductEntry(userAnswers: UserAnswers, productEntry: ProductEntry, summaryList: SummaryList)(
    implicit request: Request[_]
  ): Future[Result] =
    for {
      updateUserAnswers <- Future.fromTry(userAnswers.set(CurrentProductEntryPage, productEntry))
      _                 <- cacheConnector.set(updateUserAnswers)
    } yield Ok(view(summaryList))

}
