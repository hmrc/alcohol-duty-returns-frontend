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

package controllers.spiritsQuestions
import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.spiritsQuestions.CheckYourAnswersSummaryListHelper
import views.html.spiritsQuestions.CheckYourAnswersView
class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    (
      CheckYourAnswersSummaryListHelper.spiritsSummaryList(request.userAnswers),
      CheckYourAnswersSummaryListHelper.alcoholUsedSummaryList(request.userAnswers),
      CheckYourAnswersSummaryListHelper.grainsUsedSummaryList(request.userAnswers),
      CheckYourAnswersSummaryListHelper.otherIngredientsUsedSummaryList(request.userAnswers)
    ) match {
      case (Some(list), Some(summaryList), Some(grainsList), Some(otherIngredientsList)) =>
        Ok(view(list, summaryList, grainsList, otherIngredientsList))
      case _                                                                             => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
