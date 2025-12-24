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

import connectors.AlcoholDutyReturnsConnector
import controllers.actions.IdentifyWithEnrolmentAction
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.returns.ViewPastReturnsHelper
import views.html.returns.CompletedReturnsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CompletedReturnsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifyWithEnrolmentAction,
  val controllerComponents: MessagesControllerComponents,
  viewModelHelper: ViewPastReturnsHelper,
  view: CompletedReturnsView,
  alcoholDutyReturnsConnector: AlcoholDutyReturnsConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(year: Int): Action[AnyContent] = identify.async { implicit request =>
    val appaId = request.appaId

    val fulfilledObligationsFuture =
      alcoholDutyReturnsConnector.fulfilledObligations(appaId).map { fulfilledObligationsData =>
        fulfilledObligationsData.find(_.year == year) match {
          case Some(fulfilledObligations) if fulfilledObligations.obligations.nonEmpty =>
            val fulfilledObligationsTable = viewModelHelper.getReturnsTable(fulfilledObligations.obligations)
            Ok(view(fulfilledObligationsTable, year))
          case _                                                                       =>
            logger.warn(
              s"[CompletedReturnsController] [onPageLoad] No fulfilled obligations available for year $year (appaId: $appaId)"
            )
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }

    fulfilledObligationsFuture.recover { case ex =>
      logger.warn(
        s"[CompletedReturnsController] [onPageLoad] Error fetching fulfilled obligations for $appaId, year $year",
        ex
      )
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
