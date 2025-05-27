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
import controllers.actions._
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.spiritsQuestions.CheckYourAnswersSummaryListHelper
import views.html.spiritsQuestions.CheckYourAnswersView

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkSpiritsRegime: CheckSpiritsRegimeAction,
  checkSpiritsAndIngredientsToggle: CheckSpiritsAndIngredientsToggleAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkSpiritsRegime andThen checkSpiritsAndIngredientsToggle) {
      implicit request =>
        val result: Option[Result] = for {
          spiritsList <- CheckYourAnswersSummaryListHelper.spiritsSummaryList(request.userAnswers)
        } yield Ok(view(spiritsList))
        result.getOrElse {
          logger.warn("Impossible to create summary list")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }
}
