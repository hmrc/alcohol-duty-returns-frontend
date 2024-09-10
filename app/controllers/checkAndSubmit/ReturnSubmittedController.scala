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

import config.Constants.{adrReturnCreatedDetails, periodKeySessionKey}
import config.FrontendAppConfig
import controllers.actions._
import models.ReturnPeriod
import models.checkAndSubmit.AdrReturnCreatedDetails
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.DateTimeHelper
import viewmodels.returns.ReturnPeriodViewModel
import views.html.checkAndSubmit.ReturnSubmittedView

class ReturnSubmittedController @Inject() (
  appConfig: FrontendAppConfig,
  override val messagesApi: MessagesApi,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  val controllerComponents: MessagesControllerComponents,
  view: ReturnSubmittedView,
  dateTimeHelper: DateTimeHelper
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = identify { implicit request =>
    val businessTaxAccountUrl = appConfig.businessTaxAccountUrl

    request.session.get(adrReturnCreatedDetails) match {
      case None                       =>
        logger.warn("return details not present in session")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      case Some(returnCreatedDetails) =>
        Json.fromJson[AdrReturnCreatedDetails](Json.parse(returnCreatedDetails)).asOpt match {
          case Some(returnDetails: AdrReturnCreatedDetails) =>
            val periodKey                          = request.session.get(periodKeySessionKey).get
            val returnPeriod                       = ReturnPeriod.fromPeriodKey(periodKey).get
            val returnPeriodViewModel              = ReturnPeriodViewModel(returnPeriod)
            val periodStartDate                    = returnPeriodViewModel.fromDate
            val periodEndDate                      = returnPeriodViewModel.toDate
            val formattedProcessingDateAsLocalDate = dateTimeHelper.instantToLocalDate(returnDetails.processingDate)
            val formattedProcessingDate            = dateTimeHelper.formatDateMonthYear(formattedProcessingDateAsLocalDate)
            val formattedPaymentDueDate            =
              returnDetails.paymentDueDate.map(dateTimeHelper.formatDateMonthYear).getOrElse("")

            Ok(
              view(
                returnDetails,
                periodStartDate,
                periodEndDate,
                formattedProcessingDate,
                formattedPaymentDueDate,
                periodKey,
                businessTaxAccountUrl
              )
            )
          case None                                         =>
            logger.warn("return details not valid")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  }
}
