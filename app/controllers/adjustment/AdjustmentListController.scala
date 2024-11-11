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

package controllers.adjustment

import config.Constants.rowsPerPage
import connectors.{AlcoholDutyCalculatorConnector, UserAnswersConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifyWithEnrolmentAction}
import forms.adjustment.AdjustmentListFormProvider
import navigation.AdjustmentNavigator
import models.{NormalMode, UserAnswers}
import pages.adjustment.{AdjustmentEntryListPage, AdjustmentListPage, AdjustmentTotalPage}
import views.html.adjustment.AdjustmentListView
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustment.{AdjustmentListSummaryHelper, AdjustmentOverUnderDeclarationCalculationHelper}
import play.api.Logging
import viewmodels.PaginatedViewModel

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdjustmentListController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AdjustmentListFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentListView,
  alcoholDutyCalculatorConnector: AlcoholDutyCalculatorConnector,
  adjustmentOverUnderDeclarationCalculationHelper: AdjustmentOverUnderDeclarationCalculationHelper
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(pageNumber: Int = 1): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val duties = request.userAnswers
        .get(AdjustmentEntryListPage)
        .getOrElse(Seq.empty)
        .flatMap(duty => duty.newDuty.orElse(duty.duty))

      val preparedForm = request.userAnswers.get(AdjustmentListPage).fold(form)(form.fill)

      alcoholDutyCalculatorConnector
        .calculateTotalAdjustment(duties)
        .flatMap { total =>
          val paginatedViewModel = getPaginatedViewModel(pageNumber, request.userAnswers, total.duty)
          if (pageNumber < 1 || pageNumber > paginatedViewModel.totalPages) {
            Future.successful(Redirect(controllers.adjustment.routes.AdjustmentListController.onPageLoad(1)))
          } else {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AdjustmentTotalPage, total.duty))
              _              <- userAnswersConnector.set(updatedAnswers)
            } yield Ok(view(preparedForm, paginatedViewModel.tableViewModel, paginatedViewModel.totalPages, pageNumber))
          }
        }
        .recover { case _ =>
          logger.warn("Unable to fetch adjustment total")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
  }

  def onSubmit(pageNumber: Int = 1): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val (userAnswers, total) =
        (request.userAnswers, request.userAnswers.get(AdjustmentTotalPage).getOrElse(BigDecimal(0)))
      val paginatedViewModel   = getPaginatedViewModel(pageNumber, userAnswers, total)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(formWithErrors, paginatedViewModel.tableViewModel, paginatedViewModel.totalPages, pageNumber)
              )
            ),
          value =>
            for {
              updatedAnswers                 <- Future.fromTry(request.userAnswers.set(AdjustmentListPage, value))
              userAnswersWithOverUnderTotals <-
                adjustmentOverUnderDeclarationCalculationHelper.fetchOverUnderDeclarationTotals(updatedAnswers, value)
              _                              <- userAnswersConnector.set(userAnswersWithOverUnderTotals)
            } yield Redirect(navigator.nextPage(AdjustmentListPage, NormalMode, userAnswersWithOverUnderTotals))
        )
  }

  private def getPaginatedViewModel(pageNumber: Int, userAnswers: UserAnswers, total: BigDecimal)(implicit
    messages: Messages
  ): PaginatedViewModel = {
    val adjustmentEntries       = userAnswers.get(AdjustmentEntryListPage).getOrElse(Seq.empty)
    val totalPages              =
      if (adjustmentEntries.isEmpty) 1 else Math.ceil(adjustmentEntries.size.toDouble / rowsPerPage).toInt
    val paginatedTableViewModel = AdjustmentListSummaryHelper.adjustmentEntryTable(userAnswers, total, pageNumber)
    PaginatedViewModel(totalPages, paginatedTableViewModel)
  }

}
