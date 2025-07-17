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

import connectors.UserAnswersConnector
import controllers.actions._
import models.RateType.{DraughtAndSmallProducerRelief, SmallProducerRelief}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustment.NonDraughtTaxTypeSummaryListHelper
import views.html.adjustment.CheckYourAnswersNonDraughtTaxTypeView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CheckYourAnswersNonDraughtTaxTypeController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersNonDraughtTaxTypeView,
  checkYourAnswersSummaryListHelper: NonDraughtTaxTypeSummaryListHelper
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    // TODO load this from the cache (repackaged non draught)
    Ok(view(checkYourAnswersSummaryListHelper.nonDraughtTaxTypeSummaryList()))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val rateType = for {
      adjustment         <- request.userAnswers.get(pages.adjustment.CurrentAdjustmentEntryPage)
      repackagedRateBand <- adjustment.repackagedRateBand
    } yield repackagedRateBand.rateType
    rateType match {
      case Some(DraughtAndSmallProducerRelief) =>
        Redirect(controllers.adjustment.routes.AdjustmentSmallProducerReliefDutyRateController.onPageLoad(mode))
      case Some(SmallProducerRelief)           =>
        Redirect(controllers.adjustment.routes.AdjustmentSmallProducerReliefDutyRateController.onPageLoad(mode))
      case _                                   =>
        Redirect(controllers.adjustment.routes.AdjustmentDutyDueController.onPageLoad())
    }
  }
}
