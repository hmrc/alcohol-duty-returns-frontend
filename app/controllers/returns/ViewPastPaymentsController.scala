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

import connectors.AlcoholDutyAccountConnector
import controllers.actions.IdentifierAction
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.returns.ViewPastPaymentsViewModel
import views.html.returns.ViewPastPaymentsView
import play.api.libs.json.Json
import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import config.Constants.pastPaymentsSessionKey

class ViewPastPaymentsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  viewPastPaymentsModel: ViewPastPaymentsViewModel,
  view: ViewPastPaymentsView,
  alcoholDutyAccountConnector: AlcoholDutyAccountConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    val appaId = request.appaId
    alcoholDutyAccountConnector
      .outstandingPayments(appaId)
      .flatMap { outstandingPaymentsData =>
        val sortedOutstandingPaymentsData =
          outstandingPaymentsData.outstandingPayments.sortBy(_.dueDate)(Ordering[LocalDate].reverse)
        val outstandingPaymentsTable      =
          viewPastPaymentsModel.getOutstandingPaymentsTable(
            sortedOutstandingPaymentsData
          )
        val session                       = request.session + (pastPaymentsSessionKey -> Json.toJson(sortedOutstandingPaymentsData).toString)

        val unallocatedPaymentsTable =
          viewPastPaymentsModel.getUnallocatedPaymentsTable(outstandingPaymentsData.unallocatedPayments)

        Future.successful(
          Ok(view(outstandingPaymentsTable, unallocatedPaymentsTable, outstandingPaymentsData.totalOpenPaymentsAmount))
            .withSession(session)
        )
      }
      .recover { case _ =>
        logger.warn(s"Unable to fetch open payments data for $appaId")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }
}
