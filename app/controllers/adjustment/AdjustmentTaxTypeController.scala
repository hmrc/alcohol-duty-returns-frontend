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
import models.{Mode, RateBand, TaxType}
import navigation.AdjustmentNavigator
import pages.adjustment.{AdjustmentTaxTypePage, CurrentAdjustmentEntryPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.{AlcoholDutyCalculatorConnector, CacheConnector}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustment.AdjustmentTypeHelper
import views.html.adjustment.AdjustmentTaxTypeView

import java.time.YearMonth
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

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
          value =>
            fetchAdjustmentTaxType(TaxType(value.toString)).flatMap {
              case Some(rateBand) =>
                for {
                  adjustment     <- Future.fromTry(Try(request.userAnswers.get(CurrentAdjustmentEntryPage).get))
                  updatedAnswers <-
                    Future.fromTry(
                      request.userAnswers
                        .set(
                          CurrentAdjustmentEntryPage,
                          adjustment.copy(
                            taxCode = Some(value.toString),
                            taxRate = rateBand.rate,
                            regime = rateBand.alcoholRegime.headOption,
                            rateType = Some(rateBand.rateType)
                          )
                        )
                    )
                  _              <- cacheConnector.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(AdjustmentTaxTypePage, mode, updatedAnswers))
              case None           =>
                Future.successful(
                  BadRequest(
                    view(
                      formProvider()
                        .withError("adjustment-tax-type-input", "adjustmentTaxType.error.invalid")
                        .fill(value),
                      mode,
                      AdjustmentTypeHelper
                        .getAdjustmentTypeValue(request.userAnswers.get(CurrentAdjustmentEntryPage).get)
                    )
                  )
                )
            }
        )
  }

  def fetchAdjustmentTaxType(taxCode: TaxType)(implicit hc: HeaderCarrier): (Future[Option[RateBand]]) = {
    //hardcoded for now, will need to get this from obligation period
    val ratePeriod: YearMonth = YearMonth.of(2024, 1)
    alcoholDutyCalculatorConnector.adjustmentTaxType(taxCode, ratePeriod)
  }
}
