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

import connectors.{AlcoholDutyCalculatorConnector, CacheConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.adjustment.AdjustmentListFormProvider
import navigation.AdjustmentNavigator
import models.NormalMode
import pages.adjustment.{AdjustmentEntryListPage, AdjustmentListPage}
import views.html.adjustment.AdjustmentListView
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustment.AdjustmentListSummaryHelper
import play.api.Logging

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdjustmentListController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AdjustmentListFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentListView,
  alcoholDutyCalculatorConnector: AlcoholDutyCalculatorConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val duties       = request.userAnswers
      .get(AdjustmentEntryListPage)
      .getOrElse(Seq.empty)
      .flatMap(duty => duty.newDuty.orElse(duty.duty))
    val preparedForm = request.userAnswers.get(AdjustmentListPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }
    alcoholDutyCalculatorConnector
      .calculateTotalAdjustment(duties)
      .map { total =>
        val table = AdjustmentListSummaryHelper.adjustmentEntryTable(request.userAnswers, total.duty)
        Ok(view(preparedForm, table))
      }
      .recover { case _ =>
        logger.warn("Unable to fetch adjustment total")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val table = AdjustmentListSummaryHelper.adjustmentEntryTable(request.userAnswers, 0)
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, table))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AdjustmentListPage, value))
            _              <- cacheConnector.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AdjustmentListPage, NormalMode, updatedAnswers))
      )
  }
}
