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
import models.{AlcoholRegimeName, Mode, RateBand, UserAnswers}
import navigation.AdjustmentNavigator
import pages.adjustment.{AdjustmentRepackagedTaxTypePage, CurrentAdjustmentEntryPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import connectors.{AlcoholDutyCalculatorConnector, CacheConnector}
import models.RateType.{DraughtAndSmallProducerRelief, DraughtRelief}
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import play.api.data.Form
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustment.AdjustmentRepackagedTaxTypeView

import java.time.YearMonth
import scala.concurrent.{ExecutionContext, Future}

class AdjustmentRepackagedTaxTypeController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AdjustmentRepackagedTaxTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentRepackagedTaxTypeView,
  alcoholDutyCalculatorConnector: AlcoholDutyCalculatorConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(CurrentAdjustmentEntryPage) match {
      case None                                              => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      case Some(value) if value.repackagedRateBand.isDefined =>
        Ok(
          view(
            form.fill(
              value.repackagedRateBand
                .map(_.taxType)
                .getOrElse(throw new RuntimeException("Couldn't fetch taxCode value from cache"))
                .toInt
            ),
            mode,
            value.adjustmentType.getOrElse(
              throw new RuntimeException("Couldn't fetch adjustment type value from cache")
            )
          )
        )
      case Some(value)                                       =>
        Ok(
          view(
            form,
            mode,
            value.adjustmentType.getOrElse(
              throw new RuntimeException("Couldn't fetch adjustment type value from cache")
            )
          )
        )
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => handleFormWithErrors(mode, request.userAnswers, formWithErrors),
          value => {
            val currentAdjustmentEntry          = request.userAnswers.get(CurrentAdjustmentEntryPage).get
            val (updatedAdjustment, hasChanged) = updateTaxCode(currentAdjustmentEntry, value)
            val adjustmentType                  = updatedAdjustment.adjustmentType.getOrElse(
              throw new RuntimeException("Couldn't fetch adjustment type value from cache")
            )
            fetchAdjustmentRateBand(
              value.toString,
              updatedAdjustment.period.getOrElse(throw new RuntimeException("Couldn't fetch period value from cache"))
            ).flatMap {
              case Some(rateBand) =>
                if (rateBand.rateType == DraughtAndSmallProducerRelief || rateBand.rateType == DraughtRelief) {
                  rateBandResponseError(
                    mode,
                    value,
                    adjustmentType,
                    "adjustmentRepackagedTaxType.error.nonDraught",
                    Some(s"return.regime.${rateBand.alcoholRegimes.map(_.name).head}")
                  )
                } else {
                  for {
                    updatedAnswers <-
                      Future.fromTry(
                        request.userAnswers
                          .set(
                            CurrentAdjustmentEntryPage,
                            updatedAdjustment.copy(repackagedRateBand = Some(rateBand))
                          )
                      )
                    _              <- cacheConnector.set(updatedAnswers)
                  } yield Redirect(
                    navigator.nextPage(AdjustmentRepackagedTaxTypePage, mode, updatedAnswers, hasChanged)
                  )
                }
              case None           =>
                rateBandResponseError(mode, value, adjustmentType, "adjustmentRepackagedTaxType.error.invalid", None)
            }
          }
        )
  }

  private def handleFormWithErrors(mode: Mode, userAnswers: UserAnswers, formWithErrors: Form[Int])(implicit
    request: Request[_]
  ): Future[Result] =
    userAnswers.get(CurrentAdjustmentEntryPage) match {
      case None        => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case Some(value) =>
        Future.successful(
          BadRequest(
            view(
              formWithErrors,
              mode,
              value.adjustmentType.getOrElse(
                throw new RuntimeException("Couldn't fetch adjustment type value from cache")
              )
            )
          )
        )
    }

  def updateTaxCode(adjustmentEntry: AdjustmentEntry, currentValue: Int): (AdjustmentEntry, Boolean) =
    adjustmentEntry.repackagedRateBand.map(_.taxType) match {
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
    errorMessage: String,
    args: Option[String]
  )(implicit
    request: Request[_],
    messages: Messages
  ): Future[Result] =
    Future.successful(
      BadRequest(
        view(
          formProvider()
            .withError("new-tax-type-code", errorMessage, messages(args.getOrElse(None).toString))
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
