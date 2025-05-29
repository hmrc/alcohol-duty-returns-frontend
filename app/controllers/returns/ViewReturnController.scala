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

import connectors.{AlcoholDutyCalculatorConnector, AlcoholDutyReturnsConnector}
import controllers.actions._
import models.returns.ReturnDetails
import models.{RateBand, ReturnPeriod}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.DateTimeHelper
import viewmodels.returns.ViewReturnViewModel
import views.html.returns.ViewReturnView

import java.time.YearMonth
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewReturnController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifyWithEnrolmentAction,
  val controllerComponents: MessagesControllerComponents,
  viewModel: ViewReturnViewModel,
  view: ViewReturnView,
  alcoholDutyReturnsConnector: AlcoholDutyReturnsConnector,
  calculatorConnector: AlcoholDutyCalculatorConnector,
  dateTimeHelper: DateTimeHelper
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(periodKey: String): Action[AnyContent] = identify.async { implicit request =>
    val appaId = request.appaId
    ReturnPeriod
      .fromPeriodKey(periodKey)
      .fold {
        logger.warn(s"Cannot parse period key $periodKey for $appaId on return")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      } { returnPeriod =>
        (for {
          returnDetails                     <- alcoholDutyReturnsConnector.getReturn(appaId, periodKey)
          _                                 <- handleInvalidPeriodKey(periodKey, appaId, returnDetails)
          periodKey                          = returnDetails.identification.periodKey
          returnPeriodsAndTaxCodes           = getPeriodsAndTaxCodes(returnDetails, periodKey)
          ratePeriodsAndTaxCodes             = getRatePeriodsAndTaxCodes(returnPeriodsAndTaxCodes)
          ratePeriodsAndTaxCodesToRateBands <-
            if (ratePeriodsAndTaxCodes.nonEmpty) { calculatorConnector.rateBands(ratePeriodsAndTaxCodes) }
            else { Future.successful(Map.empty[(YearMonth, String), RateBand]) }
        } yield {
          val spirits          = if (returnPeriod.hasQuarterlySpirits) {
            viewModel.createSpiritsViewModels(returnDetails)
          } else {
            Seq.empty
          }
          val returnPeriodStr  = dateTimeHelper.formatMonthYear(returnPeriod.period)
          val submittedDate    = dateTimeHelper.instantToLocalDate(returnDetails.identification.submittedTime)
          val submittedDateStr = dateTimeHelper.formatDateMonthYear(submittedDate)
          val submittedTime    = dateTimeHelper.instantToLocalTime(returnDetails.identification.submittedTime)
          val submittedTimeStr = dateTimeHelper.formatHourMinuteMeridiem(submittedTime)

          Ok(
            view(
              period = returnPeriodStr,
              chargeReference = returnDetails.identification.chargeReference,
              submittedAtDate = submittedDateStr,
              submittedAtTime = submittedTimeStr,
              dutyToDeclare =
                viewModel.createAlcoholDeclaredViewModel(returnDetails, ratePeriodsAndTaxCodesToRateBands),
              adjustments = viewModel.createAdjustmentsViewModel(returnDetails, ratePeriodsAndTaxCodesToRateBands),
              totalDue = viewModel.createTotalDueSummaryList(returnDetails),
              netDutySuspension = viewModel.createNetDutySuspensionViewModel(returnDetails),
              spirits = spirits
            )
          )
        })
          .recover { case e =>
            logger.warn(s"Unable to fetch return $appaId $periodKey: ${e.getMessage}")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
      }
  }

  private def getRatePeriodsAndTaxCodes(returnPeriodsAndTaxCodes: Seq[(String, String)]) =
    returnPeriodsAndTaxCodes.flatMap { case (periodKey, taxCode) =>
      ReturnPeriod
        .fromPeriodKey(periodKey)
        .map(returnPeriod => (returnPeriod.period, taxCode))
    }

  private def getPeriodsAndTaxCodes(returnDetails: ReturnDetails, periodKey: String) =
    returnDetails.alcoholDeclared.taxCodes.map(
      (periodKey, _)
    ) ++ returnDetails.adjustments.returnPeriodsAndTaxCodes

  private def handleInvalidPeriodKey(periodKey: String, appaId: String, returnDetails: ReturnDetails) =
    if (returnDetails.identification.periodKey != periodKey) {
      val error =
        s"Period key on the return ${returnDetails.identification.periodKey} does not match the return $periodKey requested for $appaId"
      logger.warn(error)
      Future.failed(new RuntimeException(error))
    } else {
      Future.unit
    }
}
