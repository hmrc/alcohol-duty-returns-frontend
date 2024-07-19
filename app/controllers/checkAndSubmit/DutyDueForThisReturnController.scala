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

package controllers.checkAndSubmit

import controllers.actions._
import models.UserAnswers
import pages.returns.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage}

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.checkAndSubmit.DutyDueForThisReturnHelper
import views.html.checkAndSubmit.DutyDueForThisReturnView

class DutyDueForThisReturnController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: DutyDueForThisReturnView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    calculationTotal(request.userAnswers) match {
      case Some(totalValue) =>
        val table = DutyDueForThisReturnHelper.dutyDueByRegime(request.userAnswers)
        Ok(view(table, totalValue))
      case None             => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  private def calculationTotal(userAnswers: UserAnswers): Option[BigDecimal] =
    (userAnswers.get(DeclareAlcoholDutyQuestionPage), userAnswers.get(AlcoholDutyPage)) match {
      case (Some(false), _)                  => Some(0.00)
      case (Some(true), Some(alcoholDuties)) => Some(alcoholDuties.map(_._2.totalDuty).sum)
      case (_, _)                            => None

    }
}
