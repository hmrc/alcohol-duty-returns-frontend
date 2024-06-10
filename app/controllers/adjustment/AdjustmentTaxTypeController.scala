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
import models.{Mode, RateBand}
import navigation.AdjustmentNavigator
import pages.adjustment.{AdjustmentTaxTypePage, CurrentAdjustmentEntryPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.{AlcoholDutyCalculatorConnector, CacheConnector}
import models.adjustment.AdjustmentEntry
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustment.AdjustmentTypeHelper
import views.html.adjustment.AdjustmentTaxTypeView

import java.time.YearMonth
import scala.concurrent.{ExecutionContext, Future}

class AdjustmentTaxTypeController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AdjustmentTaxTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentTaxTypeView,
  alcoholDutyCalculatorConnector: AlcoholDutyCalculatorConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(CurrentAdjustmentEntryPage) match {
      case None                                   => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      case Some(value) if value.taxCode.isDefined =>
        Ok(view(form.fill(value.taxCode.get.toInt), mode, AdjustmentTypeHelper.getAdjustmentTypeValue(value)))
      case Some(value)                            => Ok(view(form, mode, AdjustmentTypeHelper.getAdjustmentTypeValue(value)))
    }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            request.userAnswers.get(CurrentAdjustmentEntryPage) match {
              case None        => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
              case Some(value) =>
                Future.successful(
                  BadRequest(view(formWithErrors, mode, AdjustmentTypeHelper.getAdjustmentTypeValue(value)))
                )
            },
          value => {
            val currentAdjustmentEntry          = request.userAnswers.get(CurrentAdjustmentEntryPage).get
            val (updatedAdjustment, hasChanged) = updateTaxCode(currentAdjustmentEntry, value)
            fetchAdjustmentRateBand(value.toString, updatedAdjustment.period.getOrElse(YearMonth.of(1999, 1))).flatMap {
              case Some(rateBand) =>
                for {

                  updatedAnswers <-
                    Future.fromTry(
                      request.userAnswers
                        .set(
                          CurrentAdjustmentEntryPage,
                          updatedAdjustment.copy(
                            taxCode = Some(value.toString),
                            taxRate = rateBand.rate,
                            rateType = Some(rateBand.rateType)
                          )
                        )
                    )
                  _              <- cacheConnector.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(AdjustmentTaxTypePage, mode, updatedAnswers, hasChanged))
              case None           =>
                Future.successful(
                  BadRequest(
                    view(
                      formProvider()
                        .withError("adjustment-tax-type-input", "adjustmentTaxType.error.invalid")
                        .fill(value),
                      mode,
                      AdjustmentTypeHelper
                        .getAdjustmentTypeValue(updatedAdjustment)
                    )
                  )
                )
            }
          }
        )
  }

  private def fetchAdjustmentRateBand(taxCode: String, period: YearMonth)(implicit
    hc: HeaderCarrier
  ): Future[Option[RateBand]] =
    alcoholDutyCalculatorConnector.rateBand(taxCode, period)

  def updateTaxCode(adjustmentEntry: AdjustmentEntry, currentValue: Int): (AdjustmentEntry, Boolean) =
    adjustmentEntry.taxCode match {
      case Some(existingValue) if currentValue.toString == existingValue => (adjustmentEntry, false)
      case _                                                             =>
        (
          adjustmentEntry.copy(
            sprDutyRate = None,
            totalLitresVolume = None,
            pureAlcoholVolume = None,
            duty = None
          ),
          true
        )
    }
}
