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

package controllers.returns

import controllers.actions._
import models.AlcoholRegime

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.HeadCell
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.{TableRowActionViewModel, TableRowViewModel, TableViewModel}
import views.html.returns.DutyCalculationView

class DutyCalculationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: DutyCalculationView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(regime: AlcoholRegime): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val dutyDueViewModel = TableViewModel(
        head = Seq(
          HeadCell(Text("Description")),
          HeadCell(Text("Litres of pure alcohol")),
          HeadCell(Text("Duty rate")),
          HeadCell(Text("Duty due")),
          HeadCell(Text("Action"))
        ),
        rows = Seq(
          TableRowViewModel(
            cells = Seq(
              Text("Beer (311), non-draught, between 1.3% and 3.4%"),
              Text("1,111l"),
              Text("£9.27 per litre"),
              Text("£10,298.97")
            ),
            actions = Seq(
              TableRowActionViewModel(
                label = "Change",
                href = controllers.returns.routes.CheckYourAnswersController.onPageLoad(regime)
              )
            )
          )
        ),
        total = BigDecimal("238692.57")
      )

      Ok(view(regime, dutyDueViewModel))
  }
}
