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
import controllers.actions._
import models.UserAnswers
import models.checkAndSubmit.AdrReturnCreatedDetails
import pages.returns.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage}
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.checkAndSubmit.DutyDueForThisReturnHelper
import views.html.checkAndSubmit.DutyDueForThisReturnView

import java.time.{Instant, LocalDate}

class DutyDueForThisReturnController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: DutyDueForThisReturnView
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val currentDate          = LocalDate.now()
    val returnCreatedDetails = AdrReturnCreatedDetails(
      processingDate = Instant.now(),
      amount = BigDecimal(10.45),
      chargeReference = Some("XA1527404500736"),
      paymentDueDate = LocalDate.of(currentDate.getYear, currentDate.getMonth, 25)
    )
    val session              = request.session + (adrReturnCreatedDetails -> Json.toJson(returnCreatedDetails).toString())

    val result = for {
      totalValue <- calculationTotal(request.userAnswers)
      table      <- DutyDueForThisReturnHelper.dutyDueByRegime(request.userAnswers)
    } yield Ok(view(table, totalValue)).withSession(session)

    result.fold(
      error => {
        logger.error(error)
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      },
      identity
    )
  }

  // TODO: this method will be substituted by a call to the calculator in the next iteration
  private def calculationTotal(userAnswers: UserAnswers): Either[String, BigDecimal] =
    (userAnswers.get(DeclareAlcoholDutyQuestionPage), userAnswers.get(AlcoholDutyPage)) match {
      case (Some(false), _)                  => Right(0.00)
      case (Some(true), Some(alcoholDuties)) => Right(alcoholDuties.map(_._2.totalDuty).sum)
      case (_, _)                            => Left("No duty calculation found")

    }
}
