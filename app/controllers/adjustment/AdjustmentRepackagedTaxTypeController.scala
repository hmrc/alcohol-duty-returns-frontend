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

package controllers.adjustment

import controllers.actions._
import forms.adjustment.AdjustmentRepackagedTaxTypeFormProvider

import javax.inject.Inject
import models.{Mode, RateBand, UserAnswers}
import navigation.AdjustmentNavigator
import pages.adjustment.{AdjustmentRepackagedTaxTypePage, CurrentAdjustmentEntryPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import connectors.{AlcoholDutyCalculatorConnector, UserAnswersConnector}
import models.RateType.{DraughtAndSmallProducerRelief, DraughtRelief}
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import play.api.Logging
import play.api.data.Form
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustment.AdjustmentRepackagedTaxTypeView

import java.time.YearMonth
import scala.concurrent.{ExecutionContext, Future}

class AdjustmentRepackagedTaxTypeController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AdjustmentRepackagedTaxTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentRepackagedTaxTypeView,
  alcoholDutyCalculatorConnector: AlcoholDutyCalculatorConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(CurrentAdjustmentEntryPage) match {
      case Some(AdjustmentEntry(_, Some(adjustmentType), _, _, _, _, _, _, Some(repackagedRateBand), _, _, _, _)) =>
        Ok(
          view(
            form.fill(
              repackagedRateBand.taxTypeCode.toInt
            ),
            mode,
            adjustmentType
          )
        )
      case Some(AdjustmentEntry(_, Some(adjustmentType), _, _, _, _, _, _, _, _, _, _, _))                        =>
        Ok(
          view(
            form,
            mode,
            adjustmentType
          )
        )
      case _                                                                                                      =>
        logger.warn("Couldn't fetch the adjustmentType and repackagedRateBand in AdjustmentEntry from user answers")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => handleFormWithErrors(mode, request.userAnswers, formWithErrors),
          value =>
            request.userAnswers.get(CurrentAdjustmentEntryPage) match {
              case Some(currentAdjustmentEntry) =>
                val (updatedAdjustment, hasChanged) = updateTaxCode(currentAdjustmentEntry, value)
                (updatedAdjustment.adjustmentType, updatedAdjustment.period) match {
                  case (Some(adjustmentType), Some(period)) =>
                    fetchAdjustmentRateBand(value.toString, period).flatMap {
                      case Some(repackagedRateBand)
                          if repackagedRateBand.rateType == DraughtAndSmallProducerRelief || repackagedRateBand.rateType == DraughtRelief =>
                        rateBandResponseError(
                          mode,
                          value,
                          adjustmentType,
                          "adjustmentRepackagedTaxType.error.nonDraught"
                        )
                      case Some(repackagedRateBand) =>
                        for {
                          updatedAnswers <-
                            Future.fromTry(
                              request.userAnswers
                                .set(
                                  CurrentAdjustmentEntryPage,
                                  updatedAdjustment.copy(repackagedRateBand = Some(repackagedRateBand))
                                )
                            )
                          _              <- userAnswersConnector.set(updatedAnswers)
                        } yield Redirect(
                          navigator.nextPage(AdjustmentRepackagedTaxTypePage, mode, updatedAnswers, Some(hasChanged))
                        )
                      case None                     =>
                        rateBandResponseError(mode, value, adjustmentType, "adjustmentRepackagedTaxType.error.invalid")
                    }
                  case _                                    =>
                    logger.warn("Couldn't fetch the adjustmentType and period in AdjustmentEntry from user answers")
                    Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                }
              case None                         =>
                logger.warn("Couldn't fetch currentAdjustmentEntry from user answers")
                Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            }
        )
  }

  private def handleFormWithErrors(mode: Mode, userAnswers: UserAnswers, formWithErrors: Form[Int])(implicit
    request: Request[_]
  ): Future[Result] =
    userAnswers
      .get(CurrentAdjustmentEntryPage) match {
      case Some(AdjustmentEntry(_, Some(adjustmentType), _, _, _, _, _, _, _, _, _, _, _)) =>
        Future.successful(
          BadRequest(
            view(
              formWithErrors,
              mode,
              adjustmentType
            )
          )
        )
      case _                                                                               =>
        logger.warn("Couldn't fetch the adjustmentType in AdjustmentEntry from user answers")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

  def updateTaxCode(adjustmentEntry: AdjustmentEntry, currentValue: Int): (AdjustmentEntry, Boolean) =
    adjustmentEntry.repackagedRateBand.map(_.taxTypeCode) match {
      case Some(existingValue) if currentValue.toString == existingValue => (adjustmentEntry, false)
      case _                                                             =>
        (
          adjustmentEntry.copy(
            duty = None,
            repackagedDuty = None,
            repackagedSprDutyRate = None,
            newDuty = None
          ),
          true
        )
    }

  private def rateBandResponseError(
    mode: Mode,
    value: Int,
    adjustmentType: AdjustmentType,
    errorMessage: String
  )(implicit
    request: Request[_],
    messages: Messages
  ): Future[Result] =
    Future.successful(
      BadRequest(
        view(
          formProvider()
            .withError("new-tax-type-code", errorMessage)
            .fill(value),
          mode,
          adjustmentType
        )(request, messages)
      )
    )

  private def fetchAdjustmentRateBand(taxCode: String, period: YearMonth)(implicit
    hc: HeaderCarrier
  ): Future[Option[RateBand]] =
    alcoholDutyCalculatorConnector.rateBand(taxCode, period)
}
