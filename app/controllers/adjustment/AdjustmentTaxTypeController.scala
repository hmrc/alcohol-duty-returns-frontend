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
import forms.adjustment.AdjustmentTaxTypeFormProvider

import javax.inject.Inject
import models.{Mode, RateBand, UserAnswers}
import navigation.AdjustmentNavigator
import pages.adjustment.{AdjustmentTaxTypePage, CurrentAdjustmentEntryPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import connectors.{AlcoholDutyCalculatorConnector, UserAnswersConnector}
import models.RateType.{DraughtAndSmallProducerRelief, DraughtRelief}
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import models.adjustment.AdjustmentType.RepackagedDraughtProducts
import play.api.Logging
import play.api.data.Form
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.adjustment.AdjustmentTaxTypeHelper
import views.html.adjustment.AdjustmentTaxTypeView

import java.time.YearMonth
import scala.concurrent.{ExecutionContext, Future}

class AdjustmentTaxTypeController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifyWithEnrolmentAction,
  helper: AdjustmentTaxTypeHelper,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AdjustmentTaxTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentTaxTypeView,
  alcoholDutyCalculatorConnector: AlcoholDutyCalculatorConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(CurrentAdjustmentEntryPage) match {
      case Some(AdjustmentEntry(_, Some(adjustmentType), _, _, Some(rateBand), _, _, _, _, _, _, _, _)) =>
        Ok(
          view(
            form.fill(rateBand.taxTypeCode.toInt),
            mode,
            helper.createViewModel(adjustmentType)
          )
        )
      case Some(AdjustmentEntry(_, Some(adjustmentType), _, _, _, _, _, _, _, _, _, _, _))              =>
        Ok(
          view(
            form,
            mode,
            helper.createViewModel(adjustmentType)
          )
        )
      case _                                                                                            =>
        logger.warn("Couldn't fetch the adjustmentType and rateBand in AdjustmentEntry from user answers")
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
                      case Some(rateBand) =>
                        if (checkRepackagedDraughtReliefEligibility(adjustmentType, rateBand)) {
                          rateBandResponseError(mode, value, adjustmentType, "adjustmentTaxType.error.notDraught")
                        } else {
                          for {
                            updatedAnswers <-
                              Future.fromTry(
                                request.userAnswers
                                  .set(CurrentAdjustmentEntryPage, updatedAdjustment.copy(rateBand = Some(rateBand)))
                              )
                            _              <- userAnswersConnector.set(updatedAnswers)
                          } yield Redirect(
                            navigator.nextPage(AdjustmentTaxTypePage, mode, updatedAnswers, Some(hasChanged))
                          )
                        }
                      case None           =>
                        rateBandResponseError(mode, value, adjustmentType, "adjustmentTaxType.error.invalid")
                    }
                  case _                                    =>
                    logger.warn("Impossible to retrieve adjustmentType and period in currentAdjustmentEntry")
                    Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                }
              case None                         =>
                logger.warn("Couldn't fetch currentAdjustmentEntry from user answers")
                Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            }
        )
  }

  private def checkRepackagedDraughtReliefEligibility(adjustmentType: AdjustmentType, rateBand: RateBand): Boolean =
    adjustmentType.equals(
      RepackagedDraughtProducts
    ) && (rateBand.rateType != DraughtRelief) && (rateBand.rateType != DraughtAndSmallProducerRelief)

  private def handleFormWithErrors(mode: Mode, userAnswers: UserAnswers, formWithErrors: Form[Int])(implicit
    request: Request[_]
  ): Future[Result] =
    userAnswers.get(CurrentAdjustmentEntryPage) match {
      case Some(AdjustmentEntry(_, Some(adjustmentType), _, _, _, _, _, _, _, _, _, _, _)) =>
        Future.successful(
          BadRequest(
            view(
              formWithErrors,
              mode,
              helper.createViewModel(adjustmentType)
            )
          )
        )
      case _                                                                               =>
        logger.warn("Couldn't fetch the adjustmentType in AdjustmentEntry from user answers")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

  private def rateBandResponseError(mode: Mode, value: Int, adjustmentType: AdjustmentType, errorMessage: String)(
    implicit request: Request[_]
  ): Future[Result] =
    Future.successful(
      BadRequest(
        view(
          formProvider()
            .withError("adjustment-tax-type-input", errorMessage)
            .fill(value),
          mode,
          helper.createViewModel(adjustmentType)
        )
      )
    )

  private def fetchAdjustmentRateBand(taxTypeCode: String, period: YearMonth)(implicit
    hc: HeaderCarrier
  ): Future[Option[RateBand]] =
    alcoholDutyCalculatorConnector.rateBand(taxTypeCode, period)

  def updateTaxCode(adjustmentEntry: AdjustmentEntry, currentValue: Int): (AdjustmentEntry, Boolean) =
    adjustmentEntry.rateBand.map(_.taxTypeCode) match {
      case Some(existingValue) if currentValue.toString == existingValue => (adjustmentEntry, false)
      case _                                                             =>
        (
          adjustmentEntry.copy(
            totalLitresVolume = None,
            pureAlcoholVolume = None,
            sprDutyRate = None,
            duty = None,
            repackagedRateBand = None,
            repackagedDuty = None,
            repackagedSprDutyRate = None,
            newDuty = None
          ),
          true
        )
    }
}
