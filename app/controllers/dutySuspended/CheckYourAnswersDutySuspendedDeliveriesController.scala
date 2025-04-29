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

package controllers.dutySuspended

import com.google.inject.Inject
import controllers.actions.{CheckDSDOldJourneyToggleAction, DataRequiredAction, DataRetrievalAction, IdentifyWithEnrolmentAction}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.dutySuspended.CheckYourAnswersSummaryListHelper
import views.html.dutySuspended.CheckYourAnswersDutySuspendedDeliveriesView

class CheckYourAnswersDutySuspendedDeliveriesController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkDSDOldJourneyToggle: CheckDSDOldJourneyToggleAction,
  val controllerComponents: MessagesControllerComponents,
  checkYourAnswersSummaryListHelper: CheckYourAnswersSummaryListHelper,
  view: CheckYourAnswersDutySuspendedDeliveriesView
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkDSDOldJourneyToggle) { implicit request =>
      checkYourAnswersSummaryListHelper.dutySuspendedDeliveriesSummaryList(request.userAnswers) match {
        case summaryList if summaryList.rows.nonEmpty => Ok(view(summaryList))
        case _                                        =>
          logger.warn("No duty suspended summary list items found")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
}
