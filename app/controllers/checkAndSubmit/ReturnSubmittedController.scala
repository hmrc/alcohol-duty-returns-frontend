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

import config.Constants.adrReturnCreatedDetails
import config.FrontendAppConfig
import controllers.actions._
import models.checkAndSubmit.AdrReturnCreatedDetails
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.DateTimeHelper
import views.html.checkAndSubmit.ReturnSubmittedView

import java.time.{Instant, LocalDate}

class ReturnSubmittedController @Inject() (
  appConfig: FrontendAppConfig,
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ReturnSubmittedView,
  dateTimeHelper: DateTimeHelper
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val localDateProcessingDate = dateTimeHelper.instantToLocalDate(Instant.now())
    val formattedProcessingDate = dateTimeHelper.formatDateMonthYear(localDateProcessingDate)

    val formattedPaymentDueDate = dateTimeHelper.formatDateMonthYear(LocalDate.of(2024, 8, 25))

    val periodStartDate = dateTimeHelper.formatDateMonthYear(LocalDate.of(2024, 7, 1))
    val periodEndDate   = dateTimeHelper.formatDateMonthYear(LocalDate.of(2024, 7, 31))

    val businessTaxAccountUrl = appConfig.businessTaxAccountUrl

    request.session.get(adrReturnCreatedDetails) match {
      case None                       =>
        logger.warn("return details not present in session")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      case Some(returnCreatedDetails) =>
        println("#####" + returnCreatedDetails)
        Json.fromJson[AdrReturnCreatedDetails](Json.parse(returnCreatedDetails)).asOpt match {
          case Some(returnDetails: AdrReturnCreatedDetails) =>
            Ok(
              view(
                returnDetails,
                periodStartDate,
                periodEndDate,
                formattedProcessingDate,
                formattedPaymentDueDate,
                request.returnPeriod.get.toPeriodKey,
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
