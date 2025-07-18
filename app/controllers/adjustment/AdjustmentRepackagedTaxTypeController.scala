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

import controllers.actions._
import forms.adjustment.AdjustmentRepackagedTaxTypeFormProvider
import models.Mode
import models.adjustment.AdjustmentEntry
import navigation.AdjustmentNavigator
import pages.adjustment.{AdjustmentRepackagedTaxTypePage, CurrentAdjustmentEntryPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustment.NonDraughtTaxTypeSummaryListHelper
import views.html.adjustment.AdjustmentRepackagedTaxTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdjustmentRepackagedTaxTypeController @Inject() (
  override val messagesApi: MessagesApi,
  navigator: AdjustmentNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AdjustmentRepackagedTaxTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentRepackagedTaxTypeView,
  checkYourAnswersSummaryListHelper: NonDraughtTaxTypeSummaryListHelper
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(CurrentAdjustmentEntryPage) match {
      case Some(
            AdjustmentEntry(
              _,
              Some(adjustmentType),
              _,
              _,
              Some(rateBand),
              _,
              _,
              _,
              Some(repackagedRateBand),
              _,
              _,
              _,
              _
            )
          ) =>
        val summaryList = checkYourAnswersSummaryListHelper.nonDraughtTaxTypeSummaryList(rateBand, repackagedRateBand)
        Ok(
          view(
            form,
            mode,
            adjustmentType,
            Some(summaryList)
          )
        )
      case _ =>
        logger.warn("Couldn't fetch the adjustmentType and repackagedRateBand in AdjustmentEntry from user answers")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      Future.successful(
        Redirect(navigator.nextPage(AdjustmentRepackagedTaxTypePage, mode, request.userAnswers, Some(true)))
      )
  }
}
